# Vilicus — System Architecture

## Overview

Vilicus is a self-hosted personal assistant system for managing finances, tasks, and calendars on Kubernetes. This document describes the architecture of **Phase 1: Foundation** (Auth + Database).

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Layer                                │
│  (Angular Frontend @ http://localhost:4200)                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    HTTP/REST API (Bearer JWT)
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                  Spring Boot Backend                            │
│          (http://localhost:8080, port 8080)                     │
├─────────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────────────┐│
│ │  API Layer (Controllers)                                     ││
│ │  - AuthController (/auth/register, /auth/login)             ││
│ │  - Account endpoints (TODO: Phase 2)                         ││
│ │  - Transaction endpoints (TODO: Phase 2)                     ││
│ └──────────────────────────────────────────────────────────────┘│
│           │                                                     │
│           ▼                                                     │
│ ┌──────────────────────────────────────────────────────────────┐│
│ │  Service Layer (Business Logic)                              ││
│ │  - AuthService (register, login)                             ││
│ │  - JwtUtil (token generation & validation)                   ││
│ │  - AccountService (TODO: Phase 2)                            ││
│ └──────────────────────────────────────────────────────────────┘│
│           │                                                     │
│           ▼                                                     │
│ ┌──────────────────────────────────────────────────────────────┐│
│ │  Data Layer (Repositories)                                   ││
│ │  - UserRepository (Spring Data JPA)                          ││
│ │  - AccountRepository (TODO: Phase 2)                         ││
│ │  - TransactionRepository (TODO: Phase 2)                     ││
│ └──────────────────────────────────────────────────────────────┘│
│           │                                                     │
│           ▼                                                     │
│ ┌──────────────────────────────────────────────────────────────┐│
│ │  Security Layer                                              ││
│ │  - JwtAuthenticationFilter (OncePerRequestFilter)            ││
│ │  - SecurityConfig (CORS, CSRF, Authorization)               ││
│ │  - CustomUserDetailsService                                  ││
│ └──────────────────────────────────────────────────────────────┘│
└────────────────────────────┬────────────────────────────────────┘
                             │
                    JDBC / Hibernate ORM
                             │
┌────────────────────────────▼────────────────────────────────────┐
│            PostgreSQL 16 Database                               │
│         (Docker: port 5432)                                     │
├─────────────────────────────────────────────────────────────────┤
│  - users (id, email, password_hash, created_at, updated_at)    │
│  - accounts (id, user_id, name, created_at, updated_at)        │
│  - categories (15 predefined) + category_rules                 │
│  - transactions (account_id, amount, date, category_id, ...)   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Authentication Flow

### JWT-based Stateless Authentication

Vilicus uses **JWT (JSON Web Tokens)** for stateless, scalable authentication.

#### Registration Flow

```
POST /api/auth/register
├─ Input: RegisterRequest (email, password)
├─ Validation: Email format, password strength (8+ chars)
├─ Database Check: Ensure email is unique
├─ Hash Password: BCrypt with 10 rounds
├─ Save User: User entity persisted to DB
├─ Generate Tokens:
│  ├─ Access Token (15 min expiration)
│  └─ Refresh Token (7 days expiration)
└─ Return: AuthResponse {accessToken, refreshToken, expiresIn, user}
```

#### Login Flow

```
POST /api/auth/login
├─ Input: LoginRequest (email, password)
├─ Authenticate: AuthenticationManager.authenticate()
│  ├─ Load UserDetails by email
│  ├─ Compare password hash via PasswordEncoder
│  └─ Throw BadCredentialsException if mismatch
├─ Generate Tokens (same as registration)
└─ Return: AuthResponse
```

#### Authenticated Request Flow

```
GET /api/account (with Authorization header)
├─ Header: Authorization: Bearer <jwt_token>
├─ JwtAuthenticationFilter:
│  ├─ Extract token from "Bearer " prefix
│  ├─ Validate signature & expiration (JwtUtil.isTokenValid)
│  ├─ Extract email from token
│  ├─ Load UserDetails
│  ├─ Set SecurityContext with Authentication
│  └─ Pass to next filter
├─ SecurityFilterChain:
│  ├─ Check authorization rules (@PreAuthorize if needed)
│  └─ Allow or deny request
└─ Response
```

### JWT Token Structure (HS256)

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",    // Email (subject)
    "iat": 1691750000,             // Issued at
    "exp": 1691750900              // Expires at (15 min later)
  },
  "signature": "HMACSHA256(header.payload, secret)"
}
```

---

## Database Schema

### Entity Relationships

```
User (1:N) → Accounts (1:N) → Transactions
   │
   └──────────────────────────────────────┘
                                          │
                                          ▼
                              Category (N:1)
                                  │
                                  ▼
                          CategoryRule (1:N)
```

### Tables Overview

#### `users`
- Authentication & identity
- Email as unique identifier
- Password stored as BCrypt hash (never plain text)
- Timestamps for audit trail

#### `accounts`
- User's financial accounts (bank, credit card, etc.)
- Belongs to a single user
- Supports multi-account scenarios

#### `categories`
- Predefined transaction categories (15 total)
- Groceries, Utilities, Entertainment, Salary, Savings, Transport, Insurance, Healthcare, Dining, Shopping, Subscriptions, Transfers, Rent, Taxes, Other
- Each has a hex color for UI display
- Ordered for consistent presentation

#### `category_rules`
- Pattern matching for auto-categorization (Phase 2)
- Regex or keyword matching against transaction descriptions
- Ordered by precedence

#### `transactions`
- Financial transactions
- Amount, date, description, category
- Unique `tx_id` for idempotent imports
- Timestamps for audit trail

---

## Security Architecture

### Authorization Strategy

```
Public Endpoints (No Auth Required)
├─ POST /api/auth/register
├─ POST /api/auth/login
└─ GET /api/actuator/health

Private Endpoints (JWT Required)
├─ GET /api/accounts
├─ POST /api/accounts
├─ GET /api/transactions
└─ POST /api/transactions
```

### Password Security

- **Hashing Algorithm:** BCrypt with strength 10
- **Storage:** `password_hash` column (never plain text)
- **Comparison:** Spring Security's PasswordEncoder.matches()
- **Salting:** Automatic per BCrypt

### Token Security

- **Algorithm:** HMAC SHA256 (HS256)
- **Secret:** Minimum 256 bits (32 chars minimum)
- **Storage:** Environment variable `JWT_SECRET` (not hardcoded)
- **Expiration:** Access token 15 min, Refresh token 7 days
- **Transport:** HTTP Authorization header only (`Bearer <token>`)
- **Verification:** Every request validates signature & expiration

### CORS Configuration

- **Allowed Origins:** `http://localhost:4200` (frontend)
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers:** Any (*)
- **Credentials:** Allowed (cookies if needed)
- **Max Age:** 3600 seconds

### CSRF Protection

- **Status:** Disabled for stateless REST API
- **Reason:** JWT in Authorization header (not cookie), not vulnerable to CSRF
- **Alternative:** Frontend validates origin

---

## Module Breakdown (MVP)

### Phase 1: Foundation ✅ (Current)
- User authentication (JWT)
- Database schema & migrations
- API endpoints: /auth/register, /auth/login
- Security configuration (Spring Security)

### Phase 2: Account Setup (Aug 24-31)
- Account management endpoints
- Account types (bank, credit card, etc.)
- API endpoints: /accounts (CRUD)

### Phase 3: Transaction Import (Sept 1-15)
- CSV/OFX import pipelines
- Category auto-assignment
- Transaction endpoints: /transactions
- Validation & deduplication

### Phase 4: Reporting (Sept 16-30)
- Dashboard & reporting
- Category breakdown
- Trend analysis

### Phase 5: Task Management (Oct 1-15)
- Task creation & tracking
- Due dates & reminders
- Integration with finance data

### Phase 6: Calendar & Deployment (Oct 16-31)
- Calendar events
- Kubernetes deployment
- Backup & sync

---

## Technology Stack

### Backend
- **Framework:** Spring Boot 4.1 (latest)
- **Language:** Java 25
- **Build:** Maven 3.9+
- **JPA/ORM:** Hibernate 6.x
- **JSON Web Tokens:** jjwt 0.12.3 (io.jsonwebtoken)
- **Password Hashing:** Spring Security BCrypt

### Database
- **RDBMS:** PostgreSQL 16
- **Migrations:** Flyway 10.x
- **Connection Pooling:** HikariCP (bundled with Spring Boot)

### Testing
- **Framework:** JUnit 5 (Jupiter)
- **Mocking:** Mockito 5.x
- **Integration Testing:** TestContainers 1.20.0
- **Coverage:** JaCoCo

### Deployment
- **Containerization:** Docker & Docker Compose
- **Orchestration:** Kubernetes (Phase 6)
- **CI/CD:** GitHub Actions (configured)

---

## API Contract (Phase 1)

### Authentication Endpoints

#### `POST /api/auth/register`
**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (201 Created):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com"
  }
}
```

**Error (400 Bad Request):**
```json
{
  "email": "Email should be valid",
  "password": "Password should be at least 8 characters long"
}
```

#### `POST /api/auth/login`
**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com"
  }
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid email or password"
}
```

---

## Performance Considerations

### Database
- **Indexes:** Email (users), user_id (accounts, transactions)
- **Connection Pool:** HikariCP (10-20 connections)
- **Query Optimization:** Lazy loading, batch operations

### Caching (Future)
- JWT tokens cached in Redis (Phase 2+)
- Category list cached in memory

### Scalability
- **Stateless:** No server-side session state
- **Database:** Can be replicated/sharded by user
- **Load Balancing:** Multiple Spring Boot instances behind load balancer

---

## Deployment Architecture (Target)

```
┌──────────────────────────────────────────────────────┐
│             Kubernetes Cluster                       │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │  Ingress / LoadBalancer                     │   │
│  │  (API Gateway - Phase 6)                    │   │
│  └────────────────┬────────────────────────────┘   │
│                   │                                 │
│  ┌────────────────▼──────────────────────────┐    │
│  │  Spring Boot Service Pods (3 replicas)   │    │
│  │  - JVM heap: 512MB per pod                │    │
│  │  - CPU request: 0.5 core                  │    │
│  │  - Liveness/readiness probes              │    │
│  └────────────────┬──────────────────────────┘    │
│                   │                                 │
│  ┌────────────────▼──────────────────────────┐    │
│  │  StatefulSet: PostgreSQL (1 primary)      │    │
│  │  - PersistentVolume for data               │    │
│  │  - Backup sidecar                          │    │
│  └───────────────────────────────────────────┘    │
│                                                    │
│  ┌──────────────────────────────────────────┐    │
│  │  ConfigMap: application.yml               │    │
│  │  Secret: JWT_SECRET, DB credentials       │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
└──────────────────────────────────────────────────────┘
```

---

## References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io - JSON Web Token Introduction](https://jwt.io/introduction)
- [PostgreSQL 16 Docs](https://www.postgresql.org/docs/16/)
- [Flyway Documentation](https://flywaydb.org/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
