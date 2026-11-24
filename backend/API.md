# Frontend API contract

All successful responses use the existing `{ "success": true, "message": "...", "data": ... }` envelope. Authenticated requests use `Authorization: Bearer <token>`. Sign in with `POST /api/auth/login` using `{ "identifier": "email, phone, or citizen ID", "password": "..." }`; `email` remains accepted as an alias for `identifier`.

## Authentication and profile

- `POST /api/auth/register` creates a citizen account with `fullName`, `password` (8+ characters), `phone`, `citizenId` (optional; if provided, exactly 12 digits), `address`, `ward`, and `beneficiaryGroup` (up to 100 characters; free text is allowed). `email` is optional because the citizen form signs in with phone/CCCD. If omitted, the API creates an internal email identifier. `birthDate` and `situationDescription` are optional.
- `POST /api/auth/staff-registration` submits a pending staff application (`fullName`, `email`, `phone`, `department`, six-digit `authorizationPin`/`authCode`, `password`). The PIN must match the server-only `APP_STAFF_REGISTRATION_PIN`. An administrator reviews it at `GET /api/admin/staff-registrations` and approves/rejects with `POST /api/admin/staff-registrations/{id}/approve` or `/reject`.
- `GET /api/auth/me` returns the authenticated account and roles.
- `GET /api/profile` reads the signed-in user's profile. `PUT /api/profile` updates their personal/contact and beneficiary details. `PUT /api/profile/password` accepts `{ "currentPassword": "...", "newPassword": "..." }`.
- `POST /api/auth/login` accepts email, phone, or citizen ID as `identifier`.

## Citizen requests, donations, and feedback

- `GET /api/items`, `/api/categories`, and `/api/units` provide catalog IDs and labels.
- `POST /api/assistance-requests` accepts a request and its item lines in one transaction, for example:

  ```json
  {
    "title": "Nước uống cho gia đình",
    "description": "Nhà có 4 người, đang thiếu nước sạch",
    "deliveryAddress": "Phường Hòa Hải, Ngũ Hành Sơn",
    "priority": "HIGH",
    "items": [{ "itemId": 3, "requestedQuantity": 2, "note": "thùng" }]
  }
  ```

- `POST /api/donations` accepts `pickupAddress`, `donationType`, `note`, and optional `items` (`itemId`, `offeredQuantity`, `itemCondition`). Both create endpoints remain compatible with the older two-step flow that creates a header and then posts lines to `/api/request-items` or `/api/donation-items`.
- Citizens read their own data at `GET /api/assistance-requests/mine`, `/api/donations/mine`, and `/api/delivery-confirmations/mine`; cancel pending records at `/api/assistance-requests/{id}/cancel` and `/api/donations/{id}/cancel`.
- Coordinators approve/reject at `/api/assistance-requests/{id}/approve`, `/reject`, `/api/donations/{id}/approve`, and `/reject`. Partial request approval is `POST /api/assistance-requests/{id}/approve-partial` with `{ "items": [{ "requestItemId": 10, "approvedQuantity": 1 }], "reason": "..." }`.
- `GET /api/notifications/mine` returns derived status and delivery-confirmation notices.
- `POST /api/feedback` confirms a delivered shipment and stores rating/condition/comment together. Required: `shipmentId`, `receivedBy`; optional: `confirmationCode`, `imageUrl`, `deliveryCondition`, `rating` (1–5), and `comment`. `GET /api/feedback/mine` reads the signed-in user's confirmations and feedback.

## Coordination, services, and reporting

- `POST /api/allocations` reserves approved stock. `POST /api/shipments/from-allocation` creates a shipment from the remaining quantities in an allocation, using `allocationId`, optional `partnerId`, `pickupAddress`, `deliveryAddress`, and `note`.
- Coordinators update status with `POST /api/shipments/{id}/events`, body `{ "status": "IN_TRANSIT", "description": "...", "location": "..." }`. Citizens track their own shipments at `GET /api/shipments/mine` and `/api/shipments/{id}/events`.
- `GET /api/services` lists active social services. Authenticated citizens submit and list their registrations at `POST /api/service-registrations` (`serviceId`, optional `requestDetails`) and `GET /api/service-registrations/mine`. Admins manage `/api/admin/services`; coordinators/admins review `/api/admin/service-registrations` and update status through `PUT /api/admin/service-registrations/{id}/status?value=APPROVED|REJECTED|COMPLETED`.
- `GET /api/inventory/summary` returns each catalog item's accepted, reserved, delivered, and currently available quantities for the admin resources screen.
- `GET /api/dashboard/summary` returns personal or coordination-wide dashboard counts based on role. `GET /api/reports/summary` returns coordination-wide totals and status breakdowns for admins/coordinators. `GET /api/reports/export?dataset=requests|donations|allocations|shipments|beneficiaries|summary|inventory&format=csv` downloads UTF-8 CSV files that open in Excel.
- Existing `/api/users`, `/api/items`, `/api/categories`, `/api/units`, `/api/collection-points`, `/api/shipping-partners`, and related endpoints cover account, catalog, resource, and partner administration.
