# Vilicus — REST API Reference (Phases 1-4)

## Base URL

```
http://localhost:8080/api
```

## Authentication

### Header Format

All authenticated endpoints require an `Authorization` header with a JWT token:

```
Authorization: Bearer <access_token>
```

### Token Details

- **Algorithm:** HMAC SHA256 (HS256)
- **Access Token Expiration:** 15 minutes (900 seconds)
- **Refresh Token Expiration:** 7 days (604,800 seconds)
- **Storage:** HTTP Authorization header only (stateless)

### Public Endpoints (No Auth Required)
- `POST /auth/register`
- `POST /auth/login`
- `GET /actuator/health`

---

## API Endpoints by Category

### 1. Authentication

#### Register User
**Endpoint:** `POST /auth/register`

**Description:** Create a new user account and receive JWT tokens.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Validation:**
- `email`: Valid format, must be unique
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

---

#### Login User
**Endpoint:** `POST /auth/login`

**Description:** Authenticate and receive JWT tokens.

**Request:**
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

---

### 2. Accounts (Phase 2)

#### List All Accounts
**Endpoint:** `GET /accounts`

**Authentication:** Required

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "name": "Checking Account",
    "iban": "DE89370400440532013000",
    "balance": 5000.00,
    "type": "BANK",
    "lastImportDate": "2026-08-16T10:30:00Z",
    "createdAt": "2026-08-10T00:00:00Z",
    "updatedAt": "2026-08-16T10:30:00Z"
  }
]
```

---

#### Get Account Details
**Endpoint:** `GET /accounts/{id}`

**Authentication:** Required

**Response: 200 OK**
```json
{
  "id": 1,
  "name": "Checking Account",
  "iban": "DE89370400440532013000",
  "balance": 5000.00,
  "type": "BANK",
  "lastImportDate": "2026-08-16T10:30:00Z",
  "createdAt": "2026-08-10T00:00:00Z",
  "updatedAt": "2026-08-16T10:30:00Z"
}
```

---

#### Create Account
**Endpoint:** `POST /accounts`

**Authentication:** Required

**Request:**
```json
{
  "name": "My Savings",
  "iban": "DE89370400440532013001",
  "type": "SAVINGS",
  "balance": 10000.00
}
```

**Response: 201 Created**
```json
{
  "id": 2,
  "name": "My Savings",
  "iban": "DE89370400440532013001",
  "balance": 10000.00,
  "type": "SAVINGS",
  "lastImportDate": null,
  "createdAt": "2026-08-16T11:00:00Z",
  "updatedAt": "2026-08-16T11:00:00Z"
}
```

---

#### Update Account
**Endpoint:** `PUT /accounts/{id}`

**Authentication:** Required

**Request:**
```json
{
  "name": "Primary Checking",
  "iban": "DE89370400440532013000",
  "type": "BANK"
}
```

**Response: 200 OK**
```json
{
  "id": 1,
  "name": "Primary Checking",
  "iban": "DE89370400440532013000",
  "balance": 5000.00,
  "type": "BANK",
  "lastImportDate": "2026-08-16T10:30:00Z",
  "createdAt": "2026-08-10T00:00:00Z",
  "updatedAt": "2026-08-16T11:00:00Z"
}
```

---

#### Delete Account
**Endpoint:** `DELETE /accounts/{id}`

**Authentication:** Required

**Response: 204 No Content**

---

### 3. Transactions (Phase 3)

#### List Transactions
**Endpoint:** `GET /transactions`

**Authentication:** Required

**Query Parameters:**
```
?accountId=1
&search=grocery
&category=groceries
&direction=expense
&dateFrom=2026-07-16
&dateTo=2026-08-16
&page=0
&size=50
&sort=date,desc
```

**Response: 200 OK**
```json
{
  "content": [
    {
      "id": 1,
      "accountId": 1,
      "txId": "unique-tx-123",
      "txDate": "2026-08-15",
      "date": "2026-08-16T10:30:00Z",
      "description": "REWE MARKT",
      "counterparty": "REWE Markt GmbH",
      "amount": -45.67,
      "balance": 4954.33,
      "category": "Groceries",
      "categoryId": 1,
      "direction": "expense",
      "status": "categorized",
      "reference": "INV-2026-001",
      "notes": null,
      "importSource": "CAMT052",
      "createdAt": "2026-08-16T10:30:00Z",
      "updatedAt": "2026-08-16T10:30:00Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 50
}
```

---

#### Get Transaction Details
**Endpoint:** `GET /transactions/{id}?accountId={accountId}`

**Authentication:** Required

**Query Parameters:**
```
?accountId=1
```

**Response: 200 OK**
```json
{
  "id": 1,
  "accountId": 1,
  "txId": "unique-tx-123",
  "txDate": "2026-08-15",
  "date": "2026-08-16T10:30:00Z",
  "description": "REWE MARKT",
  "counterparty": "REWE Markt GmbH",
  "amount": -45.67,
  "balance": 4954.33,
  "category": "Groceries",
  "categoryId": 1,
  "direction": "expense",
  "status": "categorized",
  "reference": "INV-2026-001",
  "notes": "Weekly shopping",
  "importSource": "CAMT052",
  "createdAt": "2026-08-16T10:30:00Z",
  "updatedAt": "2026-08-16T10:30:00Z"
}
```

---

#### Update Transaction Category
**Endpoint:** `PUT /transactions/{id}/category?accountId={accountId}`

**Authentication:** Required

**Request:**
```json
{
  "categoryId": 3
}
```

**Response: 200 OK**
```json
{
  "id": 1,
  "accountId": 1,
  "categoryId": 3,
  "category": "Shopping",
  "status": "categorized",
  "updatedAt": "2026-08-16T11:00:00Z"
}
```

---

#### Bulk Recategorize
**Endpoint:** `POST /transactions/{accountId}/bulk-recategorize`

**Authentication:** Required

**Request:**
```json
{
  "transactionIds": [1, 2, 3, 4, 5],
  "categoryId": 3
}
```

**Response: 200 OK**
```json
{
  "successful": 5,
  "failed": 0,
  "timestamp": "2026-08-16T11:00:00Z"
}
```

---

#### Archive Transaction
**Endpoint:** `PUT /transactions/{id}/archive?accountId={accountId}`

**Authentication:** Required

**Response: 200 OK**
```json
{
  "id": 1,
  "status": "archived",
  "updatedAt": "2026-08-16T11:00:00Z"
}
```

---

### 4. Transaction Import (Phase 3)

#### Upload Import File
**Endpoint:** `POST /transactions/{accountId}/import`

**Authentication:** Required

**Content-Type:** `multipart/form-data`

**Request:**
```
file: <CAMT.052 XML file, max 25MB>
```

**Response: 200 OK**
```json
{
  "accountId": 1,
  "totalFound": 250,
  "duplicatesFound": 5,
  "sample": [
    {
      "date": "2026-08-15",
      "description": "REWE MARKT",
      "counterparty": "REWE Markt GmbH",
      "amount": -45.67,
      "isDuplicate": false
    }
  ],
  "errors": []
}
```

---

#### Confirm Import
**Endpoint:** `POST /transactions/{accountId}/confirm-import`

**Authentication:** Required

**Response: 200 OK**
```json
{
  "accountId": 1,
  "imported": 245,
  "duplicatesSkipped": 5,
  "balanceUpdated": 10245.33,
  "timestamp": "2026-08-16T11:00:00Z"
}
```

---

### 5. Categories (Phase 3)

#### List Categories
**Endpoint:** `GET /categories`

**Authentication:** Required

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "name": "Groceries",
    "color": "#FF6B6B",
    "createdAt": "2026-08-10T00:00:00Z"
  },
  {
    "id": 2,
    "name": "Dining",
    "color": "#4ECDC4",
    "createdAt": "2026-08-10T00:00:00Z"
  }
]
```

---

#### Create Category
**Endpoint:** `POST /categories`

**Authentication:** Required

**Request:**
```json
{
  "name": "Fitness",
  "color": "#95E1D3"
}
```

**Response: 201 Created**
```json
{
  "id": 16,
  "name": "Fitness",
  "color": "#95E1D3",
  "createdAt": "2026-08-16T11:00:00Z"
}
```

---

#### Delete Category
**Endpoint:** `DELETE /categories/{id}`

**Authentication:** Required

**Response: 204 No Content**

---

### 6. Dashboard (Phase 4)

#### Get Dashboard Data
**Endpoint:** `GET /dashboard`

**Authentication:** Required

**Query Parameters:**
```
?dateRange=last30d  // or: thisMonth, last3m
```

**Response: 200 OK**
```json
{
  "accounts": [
    {
      "id": 1,
      "name": "Checking",
      "balance": 5000.00,
      "type": "BANK"
    }
  ],
  "recentTransactions": [
    {
      "id": 1,
      "description": "REWE MARKT",
      "amount": -45.67,
      "category": "Groceries",
      "date": "2026-08-15"
    }
  ],
  "spendingByCategory": [
    {
      "category": "Groceries",
      "total": 250.00,
      "percentage": 35.7
    }
  ],
  "monthlyTrend": [
    {
      "month": "2026-06",
      "income": 3000.00,
      "expenses": 1200.00
    }
  ]
}
```

---

### 7. Analytics (Phase 4)

#### Get Analytics Data
**Endpoint:** `GET /analytics`

**Authentication:** Required

**Query Parameters:**
```
?dateRange=6m  // or: 3m, 12m
```

**Response: 200 OK**
```json
{
  "totalIncome": 9000.00,
  "totalExpenses": 3200.00,
  "avgMonthlyExpense": 1067.00,
  "spendingByCategory": [
    {
      "category": "Groceries",
      "total": 800.00,
      "percentage": 25.0
    }
  ],
  "topMerchants": [
    {
      "name": "REWE Markt",
      "total": 450.00
    }
  ],
  "monthlyData": [
    {
      "month": "2026-06",
      "income": 3000.00,
      "expenses": 1050.00
    }
  ]
}
```

---

#### Get Category Breakdown
**Endpoint:** `GET /analytics/categories`

**Authentication:** Required

**Query Parameters:**
```
?dateRange=6m
```

**Response: 200 OK**
```json
{
  "categories": [
    {
      "id": 1,
      "name": "Groceries",
      "total": 800.00,
      "percentage": 25.0,
      "transactionCount": 24
    }
  ]
}
```

---

#### Get Top Merchants
**Endpoint:** `GET /analytics/merchants`

**Authentication:** Required

**Query Parameters:**
```
?limit=10
```

**Response: 200 OK**
```json
{
  "merchants": [
    {
      "name": "REWE Markt GmbH",
      "total": 450.00,
      "count": 12
    }
  ]
}
```

---

#### Get Monthly Trend
**Endpoint:** `GET /analytics/monthly-trend`

**Authentication:** Required

**Query Parameters:**
```
?months=6
```

**Response: 200 OK**
```json
{
  "months": [
    {
      "month": "2026-06",
      "income": 3000.00,
      "expenses": 1050.00,
      "net": 1950.00
    }
  ]
}
```

---

## HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| **200** | OK | Successful GET/PUT/DELETE request |
| **201** | Created | Resource successfully created (POST) |
| **204** | No Content | Successful DELETE (no response body) |
| **400** | Bad Request | Validation error, malformed request |
| **401** | Unauthorized | Invalid credentials, expired token |
| **403** | Forbidden | Insufficient permissions |
| **404** | Not Found | Resource not found |
| **409** | Conflict | Email already exists, duplicate data |
| **500** | Internal Server Error | Unexpected backend error |

---

## Error Response Format

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

## Data Models

### Transaction
```typescript
{
  id: number;
  accountId: number;
  txId: string;              // Unique import ID
  txDate: string;            // Transaction date (YYYY-MM-DD)
  date: string;              // Processing date (ISO 8601)
  description: string;
  counterparty: string;
  amount: number;
  balance: number;
  category: string;
  categoryId: number;
  direction: "income" | "expense";
  status: "imported" | "categorized" | "archived";
  reference: string;
  notes: string | null;
  importSource: string;      // e.g., "CAMT052"
  createdAt: string;
  updatedAt: string;
}
```

### Account
```typescript
{
  id: number;
  name: string;
  iban: string;
  balance: number;
  type: "BANK" | "CREDIT_CARD" | "SAVINGS";
  lastImportDate: string | null;
  createdAt: string;
  updatedAt: string;
}
```

### Category
```typescript
{
  id: number;
  name: string;
  color: string;            // Hex color (e.g., "#FF6B6B")
  createdAt: string;
}
```

---

## Response Time Expectations

| Endpoint | Typical Time |
|----------|-------------|
| POST /auth/register | 100-200ms |
| POST /auth/login | 50-150ms |
| GET /accounts | 20-50ms |
| GET /transactions | 100-500ms (depends on page size) |
| POST /transactions/{id}/import | 2-5 seconds (CAMT parsing) |
| GET /dashboard | 200-500ms |
| GET /analytics | 300-800ms |

---

## Rate Limiting

**Current Status:** Not implemented

**Recommended Limits (production):**
- Authentication endpoints: 5 requests/minute per IP
- General API: 100 requests/minute per user
- File upload: 10 requests/hour per user

---

## CORS Configuration

**Allowed Origins:** `http://localhost:4200`
**Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
**Allowed Headers:** *
**Credentials:** true
**Max Age:** 3600 seconds

---

## Testing the API

### Using cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"MyPass123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"MyPass123"}'

# List accounts (with token)
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <access_token>"

# Upload import file
curl -X POST http://localhost:8080/api/transactions/1/import \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@transactions.xml"
```

### Using Postman

1. Create collection "Vilicus"
2. Set variable `{{base_url}}` = `http://localhost:8080/api`
3. Set variable `{{token}}` from login response
4. Add Authorization header: `Bearer {{token}}`
5. Test each endpoint

### Using VSCode REST Client

Create `requests.rest`:

```http
### Variables
@base_url = http://localhost:8080/api

### Register
POST {{base_url}}/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "MyPass123"
}

### Get Accounts
GET {{base_url}}/accounts
Authorization: Bearer <your_token>
```

---

## Security Best Practices

1. **HTTPS Only (Production):** Never send tokens over HTTP
2. **Token Storage:** Use HttpOnly cookies in production (not localStorage)
3. **Token Expiration:** Access tokens expire in 15 minutes
4. **No Credential Commits:** JWT_SECRET in environment variables only
5. **CORS Validation:** Verify allowed origins
6. **Input Validation:** All endpoints validate input
7. **Password Security:** BCrypt hashing (10 rounds), minimum 8 chars

---

## Troubleshooting

### "Invalid email or password"
- Check spelling and capitalization
- Verify user was registered first
- Ensure database is running

### "User with this email already exists"
- Email already registered
- Use different email or login instead

### "Token expired"
- Access tokens valid for 15 minutes only
- Implement token refresh (Phase 5)

### CORS Error
- Ensure frontend is on `http://localhost:4200`
- Check backend CORS configuration
- Verify `Authorization` header is allowed

### Import File Too Large
- Max file size: 25MB
- Reduce file size or split into multiple uploads

---

## References

- [JWT.io — JWT Introduction](https://jwt.io/introduction)
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [CAMT.052 Specification](https://www.iso20022.org/)
- [REST API Best Practices](https://restfulapi.net/)
