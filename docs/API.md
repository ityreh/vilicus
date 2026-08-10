# Vilicus — API Reference (Phase 1)

## Base URL

```
http://localhost:8080/api
```

---

## Authentication

### Header Format

All authenticated endpoints require an `Authorization` header with a JWT token:

```
Authorization: Bearer <access_token>
```

### Token Expiration

- **Access Token:** 15 minutes (900 seconds)
- **Refresh Token:** 7 days (604,800 seconds)

When access token expires, use the refresh token to request a new one (Phase 2).

---

## API Endpoints

### Authentication Endpoints

#### 1. Register User

**Endpoint:** `POST /auth/register`

**Description:** Create a new user account and receive JWT tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Request Validation:**
- `email`: Valid email format (must be unique)
- `password`: Minimum 8 characters

**Response: 201 Created**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com"
  }
}
```

**Error: 400 Bad Request**
```json
{
  "email": "Email should be valid",
  "password": "Password should be at least 8 characters long"
}
```

**Error: 400 Bad Request (Duplicate Email)**
```json
{
  "error": "User with this email already exists"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

---

#### 2. Login User

**Endpoint:** `POST /auth/login`

**Description:** Authenticate existing user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response: 200 OK**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "user@example.com"
  }
}
```

**Error: 401 Unauthorized**
```json
{
  "error": "Invalid email or password"
}
```

**Error: 400 Bad Request**
```json
{
  "email": "Email should be valid"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

---

### Health Check Endpoint

#### 3. Application Health

**Endpoint:** `GET /actuator/health`

**Description:** Check if backend is running (no authentication required).

**Response: 200 OK**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/actuator/health
```

---

## HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| **200** | OK | Successful GET/POST request |
| **201** | Created | Resource successfully created (POST /register) |
| **400** | Bad Request | Validation error, malformed request |
| **401** | Unauthorized | Invalid credentials, expired token |
| **403** | Forbidden | Token valid but insufficient permissions |
| **404** | Not Found | Resource not found |
| **500** | Internal Server Error | Unexpected backend error |

---

## Error Response Format

All errors follow a consistent format:

**Single Error:**
```json
{
  "error": "User with this email already exists"
}
```

**Validation Errors:**
```json
{
  "email": "Email should be valid",
  "password": "Password should be at least 8 characters long"
}
```

---

## Authentication Flow Examples

### Complete Registration & Login Cycle

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@example.com","password":"MySecurePass123!"}'

# Response:
# {
#   "accessToken": "eyJhbGc...",
#   "refreshToken": "eyJhbGc...",
#   "expiresIn": 900,
#   "user": {"id": 1, "email": "newuser@example.com"}
# }

# 2. Use access token to make authenticated request
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer eyJhbGc..."

# 3. (Later) Login again
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@example.com","password":"MySecurePass123!"}'
```

---

## Phase 1 Limitations

### Not Yet Implemented (Phase 2+)

- ❌ Refresh token endpoint (get new access token from refresh token)
- ❌ Logout endpoint (token blacklisting)
- ❌ Change password endpoint
- ❌ Account endpoints (CRUD for bank/credit card accounts)
- ❌ Transaction endpoints (CRUD for transactions)
- ❌ Category management endpoints
- ❌ Role-based access control (RBAC)
- ❌ Multi-factor authentication (MFA)

### Current Scope (Phase 1 ✅)

- ✅ User registration with password hashing
- ✅ User login with JWT token generation
- ✅ Stateless authentication (JWT validation)
- ✅ CORS configuration for frontend
- ✅ Input validation
- ✅ Error handling

---

## Rate Limiting

**Not implemented in Phase 1.** Planned for Phase 4+ (production hardening).

Recommended future settings:
- Authentication endpoints: 5 requests per minute per IP
- General API: 100 requests per minute per user
- File upload: 10 requests per hour per user

---

## CORS Configuration

### Allowed Origins
```
http://localhost:4200
```

### Allowed Methods
```
GET, POST, PUT, DELETE, OPTIONS
```

### Allowed Headers
```
*  (all headers)
```

### Credentials
```
true  (allow cookies if needed)
```

### Max Age
```
3600 seconds
```

---

## Testing the API

### Using Postman

1. **Create Collection:** "Vilicus Phase 1"
2. **Add Requests:**
   - Register: POST /auth/register
   - Login: POST /auth/login
   - Health: GET /actuator/health

3. **Use Postman Variables:**
   ```
   {{base_url}} = http://localhost:8080/api
   {{access_token}} = (saved from register/login response)
   ```

4. **Set Authorization Header:**
   - Type: Bearer Token
   - Token: {{access_token}}

### Using VSCode REST Client

Create `.rest` file:

```http
### Variables
@base_url = http://localhost:8080/api

### Register
POST {{base_url}}/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

### Login
POST {{base_url}}/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

### Health Check
GET {{base_url}}/actuator/health
```

---

## API Versioning

**Current Version:** v1 (implicit)

**Future Versioning Strategy (Phase 3+):**
- URL path versioning: `/api/v1/...`, `/api/v2/...`
- Header versioning: `X-API-Version: 1`
- Accept header: `application/vnd.vilicus.v1+json`

---

## OpenAPI/Swagger (Future)

**Planned for Phase 2:**

- Auto-generated OpenAPI 3.0 specification
- Swagger UI at `/api/swagger-ui.html`
- Springdoc-openapi integration

**Current workaround:** This manual API documentation

---

## Troubleshooting

### Error: "Invalid email or password"

- Check spelling of email
- Verify password is correct
- Ensure user was registered first
- Check database is running: `docker-compose ps`

### Error: "User with this email already exists"

- Email is already registered
- Use different email or login instead

### Error: "Email should be valid"

- Email must be in format: `user@domain.com`
- No spaces allowed
- Must include @ and domain

### Error: "Password should be at least 8 characters long"

- Password must be 8+ characters
- Include uppercase, lowercase, numbers for security

### Token Not Working

- Token may have expired (15 min)
- Check Authorization header format: `Bearer <token>`
- Verify token not truncated or corrupted

---

## Security Best Practices

1. **Never Commit Credentials:**
   - JWT_SECRET in environment variables only
   - Database passwords in .env.local (not in git)

2. **Use HTTPS in Production:**
   - All tokens must be sent over encrypted connection
   - Never send tokens over HTTP

3. **Store Tokens Securely:**
   - Browser: HttpOnly cookie (not localStorage)
   - Mobile: Secure storage framework

4. **Token Refresh:**
   - Implement /auth/refresh endpoint (Phase 2)
   - Use refresh token to get new access token before expiry

5. **Logout Implementation:**
   - Token blacklisting (Redis) (Phase 2)
   - Or: Implement JWT revocation endpoint

---

## Response Time Expectations

| Endpoint | Typical Response Time |
|----------|---------------------|
| POST /auth/register | 100-200ms |
| POST /auth/login | 50-150ms |
| GET /actuator/health | 10-50ms |

*Note: Times include database round-trip. May be slower under high load.*

---

## References

- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.0)
- [REST API Best Practices](https://restfulapi.net/)
