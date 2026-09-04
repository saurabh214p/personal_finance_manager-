# Personal Finance Manager

A Spring Boot 3 REST API for securely tracking personal income, expenses, categories, savings goals, and financial reports. Authentication is session-cookie based and financial data is isolated to the signed-in user.

- **Live URL:** [https://personal-finance-manager-hbrz.onrender.com/api](https://personal-finance-manager-hbrz.onrender.com/api)
- **Swagger UI:** [https://personal-finance-manager-hbrz.onrender.com/swagger-ui/index.html](https://personal-finance-manager-hbrz.onrender.com/swagger-ui/index.html)

## Technology

- Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA
- H2 in-memory database (with a PostgreSQL driver available for deployment)
- Maven, JUnit 5, Mockito, and JaCoCo

## Run locally

Requires Java 21+.

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080/api`. Run tests with `mvnw.cmd test`.

## Complete application flow

```text
Register → Login (session cookie issued) → Use protected API endpoints
                                            │
                                            ├─ Manage categories
                                            ├─ Manage transactions
                                            ├─ Manage savings goals
                                            └─ View reports
                                            │
                                      Logout (session invalidated)
```

1. Register with email, password, full name, and phone number via `POST /api/auth/register`.
2. Login via `POST /api/auth/login`; retain its session cookie for all subsequent requests.
3. Read `GET /api/categories`. The application seeds global defaults on startup: `Salary` (income), plus `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, and `Utilities` (expenses).
4. Optionally create a user-specific category with `POST /api/categories`. Names are unique per user.
5. Create transactions with a positive amount, non-future date, accessible category name, and optional description. The server derives transaction type from the category.
6. List transactions newest first, optionally filtering by dates, category ID, and type. Update every field except date. Deleting a transaction is a soft-delete, so it disappears from lists, reports, and goal calculations.
7. Create savings goals with a positive target and future target date. On every read, progress is calculated live as income minus expenses since the goal start date.
8. View monthly and yearly reports grouped by category. They include only active transactions owned by the authenticated user.
9. Logout with `POST /api/auth/logout`, which invalidates the server-side session.

All endpoints other than registration and login require a valid session. Known failures use a consistent JSON error response and an appropriate `400`, `401`, `403`, `404`, or `409` status.

## API overview

| Area | Endpoint | Purpose |
| --- | --- | --- |
| Authentication | `POST /api/auth/register` | Create an account |
| Authentication | `POST /api/auth/login` | Authenticate and establish a session |
| Authentication | `POST /api/auth/logout` | Invalidate the current session |
| Transactions | `POST /api/transactions` | Create a transaction |
| Transactions | `GET /api/transactions` | List/filter active transactions |
| Transactions | `PUT /api/transactions/{id}` | Update a transaction; date is immutable |
| Transactions | `DELETE /api/transactions/{id}` | Soft-delete a transaction |
| Categories | `GET /api/categories` | List default and owned custom categories |
| Categories | `POST /api/categories` | Create a custom category |
| Categories | `DELETE /api/categories/{name}` | Delete an unused custom category |
| Goals | `POST /api/goals` | Create a savings goal |
| Goals | `GET /api/goals`, `GET /api/goals/{id}` | View goals with live progress |
| Goals | `PUT /api/goals/{id}`, `DELETE /api/goals/{id}` | Update or remove a goal |
| Reports | `GET /api/reports/monthly/{year}/{month}` | Monthly income, expenses, net savings |
| Reports | `GET /api/reports/yearly/{year}` | Yearly income, expenses, net savings |

## Architecture

The application follows strict, feature-first layering:

```text
HTTP request
    ↓
Controller  →  Service interface / implementation  →  Repository  →  JPA entity
    ↓                 ↓
Request/response DTO  Business rules, ownership checks, calculations
    ↓
GlobalExceptionHandler → consistent JSON error response
```

```text
com.saurabh.personal_finance_manager
├── controllers/                  Auth, Transaction, Category, Goal, Report controllers
├── services/                     Service interfaces and their implementations
├── repositories/                 Spring Data JPA repositories
├── dtos/                         Request, response, report, and error DTOs
├── entities/                     User, Transaction, Category, SavingsGoal, CategoryType
├── mappers/                      Entity-to-DTO mapping helpers
├── exceptions/                   API exceptions and GlobalExceptionHandler
├── security/                     CurrentUserProvider
└── config/                       SecurityConfig and DataSeeder
```

Controllers are HTTP-only and use DTOs. Services own database-aware rules such as visibility, ownership, uniqueness, deletion conflicts, and financial calculations. Repositories are Spring Data JPA persistence interfaces. Entities never cross the HTTP boundary.

## Security and data isolation

- Spring Security protects every route except registration and login.
- `CurrentUserProvider` gets the user only from the authenticated session; clients cannot choose another user through payloads, query parameters, or headers.
- A user cannot read, update, or delete another user's transactions, goals, or custom categories.
- Custom categories belonging to another user are invalid transaction references.

## Design decisions

- **Soft-delete transactions:** deleted entries are excluded consistently from reports and goal progress.
- **Default category protection:** global defaults are immutable and reject deletion with `403 Forbidden`.
- **Live goal progress:** progress is not stored or clamped, so negative and over-target values remain accurate.
- **Per-user category names:** different users may create custom categories with the same name.
- **Money and dates:** money uses `BigDecimal`; domain dates use `LocalDate` and are server-validated.
