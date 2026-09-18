# API guide

Base URL: `http://localhost:8080/api`. Interactive OpenAPI: `http://localhost:8080/swagger-ui.html`.

All `/api` endpoints except registration and login require `Authorization: Bearer <token>`. Obtain a token first, then use **Authorize** in Swagger UI. Do not paste real tokens into issues or screenshots.

## Authentication

### Register — `POST /auth/register`

```json
{
  "name": "Alex",
  "email": "alex@example.com",
  "password": "choose-a-unique-password"
}
```

Returns **201** with `{ "token": "...", "user": { "userId": 1, "name": "Alex", "email": "alex@example.com" } }`.

Email is normalized to lowercase. Passwords require 8–64 characters and at most 72 UTF-8 bytes. The database stores a BCrypt hash; responses exclude it. Each new account gets a new household.

### Sign in — `POST /auth/login`

```json
{ "email": "alex@example.com", "password": "choose-a-unique-password" }
```

Returns **200** with the same response shape. Tokens expire after one hour. Invalid credentials return 401; duplicate registration returns 409. Signing out clears the browser’s token; server-side token revocation is not implemented.

### Current profile — `GET /users/me`

Returns the authenticated member’s ID, name, and email. There are no arbitrary-user listing, creation, or deletion endpoints.

## Households

| Method and route | Response / behavior |
| --- | --- |
| `GET /groups/me` | `{ groupId, groupName, inviteCode }` for the current household |
| `GET /groups/members` | Member profiles ordered by user ID |
| `POST /groups/join` | Accepts `{ "inviteCode": "the-private-household-code" }`; returns the joined household |

A join from a home with existing records or multiple members returns 409. An unknown invite returns 404. Joining your current household is a successful no-op.

## Resource CRUD

Expenses, chores, groceries, and maintenance use the same collection/item conventions:

| Method | Route pattern | Success |
| --- | --- | --- |
| GET | `/{resource}` | 200; array, newest record IDs first |
| GET | `/{resource}/{id}` | 200; single record |
| POST | `/{resource}` | 201; created record |
| PUT | `/{resource}/{id}` | 200; complete editable-field replacement |
| DELETE | `/{resource}/{id}` | 204; no response body |

Resource names are `expenses`, `chores`, `groceries`, and `maintenance`. List endpoints are not paginated yet. Item reads, edits, and deletions are scoped to the current home; a foreign or missing record returns 404.

### Expense payload

```json
{
  "title": "Internet bill",
  "amount": 60.00,
  "date": "2026-09-18",
  "category": "Utilities",
  "notes": "September connection",
  "splitType": "EQUAL"
}
```

Title: required, maximum 120 characters. Amount: `0.01`–`9999999999.99`, at most two decimal places. Date: required ISO calendar date. Category: required, maximum 40 characters. Notes: optional, maximum 1,000 characters. Only `EQUAL` is accepted.

A response adds `expenseId`, `paidBy: { userId, name, email }`, and `shares`, a map from participant IDs to exact decimal shares. The payer is always the creator; clients cannot set payer, household, or share amounts. An update preserves payer and original participants.

### Chore payload

```json
{
  "choreName": "Clean kitchen",
  "dueDate": "2026-09-20",
  "recurrence": "WEEKLY",
  "status": "TODO",
  "assigneeId": null
}
```

`choreName` is required, maximum 120 characters. `dueDate` is required. Recurrence is `NONE`, `DAILY`, `WEEKLY`, or `MONTHLY`. Status is `TODO`, `IN_PROGRESS`, or `DONE`. Assignee may be null or a member of the current home; a foreign or missing user ID returns 400. Recurrence is stored but does not create scheduled instances.

A response adds `choreId` and an optional `assignee` member object. Send `assigneeId`, not the response’s `assignee` object, in update payloads.

### Grocery payload

```json
{ "itemName": "Oat milk", "quantity": 2, "purchased": false }
```

Name: required, maximum 120 characters. Quantity: integer from 1 to 999. Purchased: required boolean. Response ID: `groceryId`.

### Maintenance payload

```json
{
  "title": "Leaky tap",
  "description": "The kitchen faucet keeps dripping.",
  "status": "OPEN"
}
```

Title: required, maximum 120 characters. Description: required, maximum 2,000 characters. Status: `OPEN`, `IN_PROGRESS`, or `RESOLVED`. Response ID: `issueId`.

## Calculated balances — `GET /balances`

```json
[
  { "userId": 1, "name": "Alex", "netAmount": 30.00 },
  { "userId": 2, "name": "Sam", "netAmount": -30.00 }
]
```

Balances are calculated from all recorded expenses, including each member’s own share. Positive is receivable, negative is payable. No settlement/payment endpoint exists yet.

## Errors

Validation and application errors use Spring Problem Details (`application/problem+json`). Example:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Check the highlighted fields.",
  "errors": { "amount": "must be greater than or equal to 0.01" }
}
```

| Status | Meaning |
| --- | --- |
| 400 | Invalid or missing fields, malformed JSON, unsupported split/status, or invalid assignee |
| 401 | Missing/invalid/expired authentication or incorrect login credentials |
| 403 | Browser origin rejected by CORS |
| 404 | Resource unavailable to the household, or unknown invite |
| 409 | Duplicate account, conflicting data, or attempted move from an active home |

## cURL walkthrough

Use Git Bash, Bash, or a terminal with `curl`. On older Windows PowerShell, invoke `curl.exe` explicitly.

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alex","email":"alex@example.com","password":"choose-a-unique-password"}'
```

Copy the returned token into a local shell variable; do not commit it:

```bash
TOKEN='paste-returned-token-here'
curl http://localhost:8080/api/groups/me -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Internet","amount":60.00,"date":"2026-09-18","category":"Utilities","splitType":"EQUAL"}'
curl http://localhost:8080/api/balances -H "Authorization: Bearer $TOKEN"
```

## Health and generated documentation

`GET /actuator/health` returns status without database details. `/v3/api-docs` serves the generated OpenAPI document. In the Docker stack, the API and Swagger UI are reachable on port 8080; the app’s Nginx proxy forwards `/api` and the health endpoint on port 3000.
