# Vilicus — System Architecture

## Overview

Vilicus is a self-hosted personal finance manager (MVP complete: Phases 1-4). This document describes the **full-stack architecture** of the Angular 18+ frontend and Spring Boot 4.1 backend, including authentication, transaction import (CAMT.052), and comprehensive analytics dashboard.

---

## High-Level Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                  Angular 18+ Frontend                             │
│     (http://localhost:4200, Standalone Components)               │
├───────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ Layout Shell (NavBar, Sidebar, RouterOutlet)               │  │
│ ├─────────────────────────────────────────────────────────────┤  │
│ │ Pages:                                                      │  │
│ │ ├─ Dashboard (account cards, donut chart, trends)          │  │
│ │ ├─ Transactions (advanced filter, sorting, bulk actions)   │  │
│ │ ├─ Import (3-step wizard, file preview, deduplication)     │  │
│ │ ├─ Accounts (list view, detail modal, balance history)     │  │
│ │ ├─ Analytics (category breakdown, merchants, trends)       │  │
│ │ └─ Settings (profile, categories, data export)             │  │
│ └─────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           ▼ (RxJS Observables, BehaviorSubject)                 │
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ Services:                                                   │  │
│ │ ├─ ApiService (30+ typed endpoints)                        │  │
│ │ ├─ AuthService (login, register, token management)         │  │
│ │ ├─ DashboardService (cached state)                         │  │
│ │ ├─ TransactionService (filtering, sorting, pagination)     │  │
│ │ ├─ ImportService (file upload, preview)                    │  │
│ │ └─ AnalyticsService (breakdown, merchants, trends)         │  │
│ └─────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           ▼ (HttpInterceptor for JWT injection)                 │
└───────────────────────────────────────────────────────────────────┘
                              │
                   HTTP/REST API (Bearer JWT)
                   Base URL: http://localhost:8080/api
                              │
┌───────────────────────────────────────────────────────────────────┐
│               Spring Boot 4.1 Backend (Java 25)                   │
│                    http://localhost:8080                          │
├───────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ REST Controllers (22+ endpoints)                            │  │
│ │ ├─ AuthController: /auth/{register,login}                  │  │
│ │ ├─ AccountController: /accounts (CRUD)                     │  │
│ │ ├─ TransactionController: /transactions (import, query)    │  │
│ │ ├─ TransactionQueryController: /transactions/query         │  │
│ │ ├─ TransactionCategoryController: /transactions/category   │  │
│ │ ├─ DashboardController: /dashboard                         │  │
│ │ └─ AnalyticsController: /analytics                         │  │
│ └─────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           ▼                                                       │
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ Service Layer (Business Logic)                              │  │
│ │ ├─ AuthService (JWT generation & validation)               │  │
│ │ ├─ AccountService (CRUD, balance calculations)             │  │
│ │ ├─ TransactionService (queries, filtering, categorization) │  │
│ │ ├─ ImportService (CAMT.052 parsing, deduplication)         │  │
│ │ ├─ CamtParser (XML parsing + validation)                   │  │
│ │ └─ AnalyticsService (aggregations, trends)                 │  │
│ └─────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           ▼                                                       │
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ Data Layer (Spring Data JPA)                                │  │
│ │ ├─ UserRepository                                           │  │
│ │ ├─ AccountRepository (custom queries)                       │  │
│ │ └─ TransactionRepository (filtering, sorting)               │  │
│ └─────────────────────────────────────────────────────────────┘  │
│           │                                                       │
│           ▼                                                       │
│ ┌─────────────────────────────────────────────────────────────┐  │
│ │ Security & Config                                           │  │
│ │ ├─ JwtAuthenticationFilter (token validation)               │  │
│ │ ├─ SecurityConfiguration (CORS, auth rules)                 │  │
│ │ ├─ JwtUtil (token generation, claims extraction)           │  │
│ │ └─ CustomUserDetailsService                                │  │
│ └─────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
                              │
                   Hibernate ORM / JDBC
                              │
┌───────────────────────────────────────────────────────────────────┐
│              PostgreSQL 18 Database (Docker)                      │
│                  localhost:5432/vilicus                           │
├───────────────────────────────────────────────────────────────────┤
│ Tables (4 core):                                                  │
│ ├─ users (id, email, password_hash, created_at, updated_at)      │
│ ├─ accounts (id, user_id, name, iban, type, balance, ...)        │
│ ├─ transactions (id, account_id, date, amount, category_id, ...) │
│ └─ categories (id, name, color, created_at)                      │
│                                                                   │
│ Indexes: email (users), user_id (accounts), account_id (txns)    │
└───────────────────────────────────────────────────────────────────┘
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

## Module Breakdown (MVP — All Phases Complete)

### Phase 1: Foundation ✅ (Complete)
- User authentication (JWT with 15min access, 7-day refresh tokens)
- PostgreSQL 18 schema & Liquibase migrations
- API endpoints: `/auth/register`, `/auth/login`
- Spring Security configuration with CORS, CSRF disabled
- Password hashing (BCrypt, 10 rounds)

### Phase 2: Account Management ✅ (Complete)
- Account CRUD endpoints: `/accounts` (GET, POST, PUT, DELETE)
- Account types: Bank, Credit Card, Savings
- Balance tracking and last import date
- Full test coverage (45+ tests)

### Phase 3: Transaction Import ✅ (Complete)
- CAMT.052 XML file upload (25MB max)
- Automatic deduplication (by unique `tx_id`)
- Category auto-assignment (placeholder logic)
- 3-step import workflow (upload → preview → confirm)
- Transaction query endpoints with filtering, sorting, pagination
- Bulk categorization endpoints
- Comprehensive validation & error handling
- 60+ backend tests

### Phase 4: Angular Frontend + API Integration ✅ (Complete)

#### FE-1: Layout Shell
- NavBar component (logo, account selector, user avatar, logout)
- Sidebar component (6 nav items, mobile hamburger < 880px)
- Responsive layout with RouterOutlet
- 18/20 unit tests

#### FE-2: Dashboard
- Account cards grid (clickable to filter)
- Category spending donut chart (CSS conic-gradient, top 5 + "Other")
- Monthly trend line chart (6 months, income solid/expense dashed SVG)
- Recent transactions table (date, description, category tag, amount)
- Date range segmented control (Last 30d, This month, Last 3m)
- Full responsive design

#### FE-3: Transactions
- Advanced filtering (search, account, category, date range, direction)
- Sortable table with ↑↓ indicators
- Bulk selection (header checkbox) and bulk actions (recategorize, archive)
- Pagination (50 rows/page)
- Transaction detail modal on row click

#### FE-4: Import Wizard
- 3-step state machine (upload → preview → result)
- Drag-drop file zone with validation (XML only, 25MB max)
- Preview table with duplicate highlighting
- Step indicator with progress visualization

#### FE-5: Accounts
- List view: Account table (name, IBAN, balance, last import)
- Detail view: Balance card, 6-month balance history chart, import log
- Add account modal with form validation

#### FE-6: Analytics
- Category spending breakdown (donut + table with %)
- Monthly trend chart (income vs expenses)
- Top 10 merchants bar chart
- Grouped income/expense column chart

#### FE-7: Settings
- Profile card (name, email)
- Category management (add, remove)
- Data export (CSV, JSON)
- Account list with delete

#### FE-8: API Integration
- 30+ typed REST endpoints in ApiService
- JwtInterceptor for automatic token injection
- 6 service classes (Auth, Dashboard, Transaction, Import, Analytics, Account)
- BehaviorSubject state management for dashboard, import, auth
- Proper error handling with 401 redirects
- 153+ tests with 96.2% pass rate

#### Frontend Features
- Nocturne dark theme (#161826 bg, #9184d9 accent)
- Compact spacing scale (0.7×)
- Mobile responsive (hamburger drawer, table optimization)
- No external CDN for charts (CSS conic-gradient, SVG polylines)
- TypeScript strict mode, standalone components
- CSS budget optimization (< 2.5KB per component)

### Phase 5+: Future Roadmap
- [ ] Task management (creation, tracking, reminders)
- [ ] Calendar integration
- [ ] Budget planning & alerts
- [ ] Kubernetes deployment
- [ ] Backup & sync features

---

## Technology Stack

### Backend (Java 25, Spring Boot 4.1)
- **Framework:** Spring Boot 4.1 (Spring Security, Spring Data JPA)
- **Language:** Java 25
- **Build:** Maven 3.9+
- **JPA/ORM:** Hibernate 6.x
- **JSON Web Tokens:** jjwt 0.12.3 (io.jsonwebtoken)
- **Password Hashing:** Spring Security BCrypt (10 rounds)
- **XML Parsing:** JAXB (for CAMT.052)
- **Validation:** Hibernate Validator 8.x

### Frontend (Angular 18+, TypeScript 5.2+)
- **Framework:** Angular 18+ (standalone components)
- **Language:** TypeScript 5.2+
- **HTTP:** Angular HttpClient with Interceptors
- **Routing:** Angular Router (6 routes)
- **State Management:** RxJS Observables, BehaviorSubject
- **Styling:** CSS (no-build design system)
- **Charts:** Chart.js + CSS conic-gradient + SVG polylines
- **Testing:** Jasmine + Karma
- **Build Tool:** Angular CLI 18+

### Database (PostgreSQL 18)
- **RDBMS:** PostgreSQL 18 (Docker image)
- **Migrations:** Liquibase 4.20+
- **Connection Pooling:** HikariCP (bundled with Spring Boot)
- **ORM Queries:** Spring Data JPA + custom JPQL

### Testing & Quality
- **Backend Tests:** JUnit 5 (Jupiter), Mockito 5.x, TestContainers 1.20.0
- **Frontend Tests:** Jasmine + Karma
- **Coverage:** JaCoCo (backend), Istanbul (frontend)
- **Total Tests:** 330+ (177 backend + 153+ frontend)
- **Pass Rate:** 99.7%

### Deployment & CI/CD
- **Containerization:** Docker & Docker Compose
- **CI/CD:** GitHub Actions
- **Linting:** wagoid/commitlint-github-action (conventional commits)
- **Orchestration:** Kubernetes-ready (Phase 6)
- **Monitoring:** Actuator endpoints configured

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
