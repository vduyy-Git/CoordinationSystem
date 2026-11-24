# Coordination System backend

Spring Boot REST API for coordinating donations, assistance requests, inventory allocation, shipments, citizen profiles, social-service registrations, and feedback. The repository includes the API and static citizen/admin frontend prototypes.

## Run locally

1. Install Java 17+ and SQL Server.
2. Run `sql/Coordination.sql` to recreate `CoordinationDB` and its complete schema (tables and indexes), then run `sql/CoordinationData.sql` to seed roles, catalog items, demo accounts, and sample operational data. The data script is repeatable. **Running `Coordination.sql` deletes the existing database and all its data first.**
3. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `APP_TOKEN_SECRET`. The token secret must contain at least 32 bytes; the app refuses to start without it. Set `APP_STAFF_REGISTRATION_PIN` to a private six-digit code if staff applications are enabled.
4. For the first administrator, also set `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD` (12+ characters). The bootstrap only creates a new account; it will not promote an existing user.
5. Start the API from `backend` with `mvn spring-boot:run` or `mvnw.cmd spring-boot:run`.

`backend/.env.example` lists the required local settings. Copy it to `backend/.env.local` and replace every placeholder. The app loads that file whether it is started from the repository root or from `backend`. `.env.local` is intentionally local configuration and should not be committed.

Hibernate validates the existing SQL schema and does not create or alter tables. If an existing account has a legacy plaintext password, a successful login upgrades it to BCrypt immediately.

Frontend API routes and example request bodies are documented in [backend/API.md](backend/API.md). Local static servers on ports 5500 and 5173 are allowed by default for browser CORS; configure `APP_SECURITY_CORS_ORIGINS` for another frontend origin.

### Sample database data

After creating the schema with `sql/Coordination.sql`, run `sql/CoordinationData.sql` in SQL Server Management Studio. The script is transactional and can be run again without duplicating its records. It seeds default roles, units, categories, catalog items, demo accounts, pending records for approval, approved requests ready for allocation, one completed donation-to-delivery history, and extended sample accounts, social services, registrations, and collection points, then prints summary database records. When running it with `sqlcmd`, use UTF-8 input (`-f 65001`) so Vietnamese catalog names match correctly.

All demo accounts use the password `DemoPass123!`:

- `demo.admin@coordination.local` — `ADMIN`
- `demo.coordinator@coordination.local` — `COORDINATOR`
- `demo.donor@coordination.local` — `USER`
- `demo.recipient@coordination.local` — `USER`

Additional seeded accounts in `sql/CoordinationData.sql` use the same password for active accounts. Examples: `demo.citizen.hieu@coordination.local`, `demo.citizen.hong@coordination.local`, and `demo.coordinator.an@coordination.local`. `demo.citizen.tam@coordination.local` is intentionally `LOCKED` for status-filter demonstrations.

After starting the API and serving `frontend` on an allowed local origin (port 5500 or 5173), sign in through the login form using one of the seeded accounts above.

Use `RQ-DEMO-READY-001` to test allocation creation. The core completed example uses `DN-NHS-2026-001`, `RQ-NHS-2026-001`, and `SH-NHS-2026-001`.

## Authentication

`POST /api/auth/register` creates a `USER` account. `POST /api/auth/login` returns a signed Bearer access token that expires after one hour. Send it as `Authorization: Bearer <token>`. `GET /api/auth/me` returns the current profile and roles. Tokens are stateless and cannot be individually revoked; changing account status or roles takes effect on the next request, while password changes do not invalidate already issued tokens before expiry.

All protected endpoints return JSON `401` or `403` responses. Catalog `GET` endpoints for items, categories, and units are public. Regular users can create their own donations and assistance requests, add item lines to those records, and list their own records at `/api/donations/mine` and `/api/assistance-requests/mine`. Administrative and coordination operations require `ADMIN` or `COORDINATOR`; user/role administration requires `ADMIN`.

## Try the API

Open `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`. Use `POST /api/auth/login` to get an access token, choose **Authorize**, and paste the token value. Swagger UI will send it as a Bearer token for protected operations. The OpenAPI JSON is at `/v3/api-docs`; set `OPENAPI_ENABLED=false` to disable Swagger UI and the OpenAPI document.

## Main workflow endpoints

- `POST /api/donations` and `POST /api/donation-items` create a pending donation and its offered items.
- `POST /api/donations/{id}/approve` or `/reject` records a coordinator decision. Approval accepts the offered quantities.
- A donor can cancel their own pending donation with `POST /api/donations/{id}/cancel`.
- `POST /api/assistance-requests` and `POST /api/request-items` create a pending request and requested items.
- `POST /api/assistance-requests/{id}/approve` or `/reject` records a coordinator decision. Approval initially approves the requested quantities.
- A recipient can cancel their own pending request with `POST /api/assistance-requests/{id}/cancel`.
- `POST /api/allocations` reserves approved donation stock against approved request lines. It checks matching item types, remaining requested quantity, and available stock in a transaction, then records supply movements.
- `POST /api/allocations/{id}/cancel` releases reserved stock and logs the movement. A partially delivered allocation cannot be cancelled.
- `POST /api/shipments/{id}/events` changes shipment status only along allowed transitions and appends an immutable event.

The create endpoints accept compact JSON bodies. For example, create a donation with `{"pickupAddress":"...","donationType":"DONATED","note":"..."}`, then add a line with `{"donationId":1,"itemId":1,"offeredQuantity":10,"itemCondition":"NEW"}`. A request uses `title`, `description`, `deliveryAddress`, and `priority`; its item line uses `requestId`, `itemId`, `requestedQuantity`, and `note`. Allocation creation takes `requestId`, optional `note`, and `items` containing `requestItemId`, `donationItemId`, and positive `quantity` values.

Most legacy CRUD endpoints are restricted to coordinators or administrators. Allocation and shipment history records cannot be changed through generic CRUD endpoints because doing so would bypass stock and audit rules.
