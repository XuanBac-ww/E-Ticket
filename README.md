# E-Ticket

Backend quan ly su kien va ve dien tu. README nay chi tap trung vao tech stack, cach trien khai, use case, flow nghiep vu va endpoint API.

## Tech Stack

### Backend

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Security + JWT cookie auth
- Spring Data JPA / Hibernate
- Jakarta Validation
- PostgreSQL 16
- Flyway migration
- MapStruct
- Lombok
- Spring Mail
- Spring Scheduling va Async executor
- Springdoc OpenAPI / Swagger UI
- ZXing QR Code
- Maven Wrapper

### Frontend

- Next.js 16.1.6
- React 19.2.3
- TypeScript 5
- Tailwind CSS 4
- ESLint 9

Frontend hien dang o muc scaffold, backend API la phan chinh cua repository.

### Database

Flyway migration hien co:

- `V1__Initial_Schema.sql`
- `V2__Add_admin_code_to_admins.sql`
- `V3__insert_default_admin.sql`
- `V4__add_version_to_ticket_types.sql`
- `V5__seed_users_staffs_events_ticket_types_tickets.sql`
- `V6__add_qr_payments.sql`
- `V7__add_ticket_order_integrity_constraints.sql`

Cac bang nghiep vu chinh:

- `users`, `admins`, `customers`, `staffs`
- `events`, `ticket_types`, `tickets`
- `orders`, `order_items`, `payments`

### Security

- Role: `ADMIN`, `STAFF`, `CUSTOMER`
- Access token va refresh token duoc luu bang HTTP-only cookie.
- CSRF token co endpoint rieng `GET /api/auth/csrf`.
- Public route chinh: auth, Swagger, `GET /api/events/**`, `GET /api/ticket-types/**`.
- Route `/api/admin/**` yeu cau `ADMIN`.
- Cac route con lai yeu cau dang nhap va duoc rang buoc tiep bang `@PreAuthorize`.

## Trien Khai

### Docker

Backend co Dockerfile multi-stage:

- Build stage: `eclipse-temurin:21-jdk-alpine`
- Runtime stage: `eclipse-temurin:21-jre-alpine`
- App expose port `8080`
- Artifact chay bang `java -jar /app/app.jar`

Docker Compose trong `backend/docker-compose.yml` gom:

- `postgres-db`: PostgreSQL 16 Alpine
- `backend`: Spring Boot app, profile `dev`
- `pgadmin`: tuy chon, chay qua profile `tools`

Chay backend va database:

```bash
cd backend
docker compose up --build
```

Chay them pgAdmin:

```bash
cd backend
docker compose --profile tools up -d
```

URL mac dinh:

- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- pgAdmin: `http://localhost:5050`

### Bien Moi Truong

Backend can cac bien sau khi chay profile `dev`:

```env
DB_URL=jdbc:postgresql://localhost:5432/eticket
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace_with_a_secret_key_at_least_32_chars
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
PAYMENT_QR_URL=https://example.com/payment-qr.png
PAYMENT_RECEIVER_NAME=Receiver Name
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
```

Docker Compose doc bien tu `backend/.env`, gom them:

```env
POSTGRES_DB=eticket
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432
APP_PORT=8080
TZ=Asia/Ho_Chi_Minh
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin123
PGADMIN_PORT=5050
```

### Chay Local

Khoi dong database:

```bash
cd backend
docker compose up -d postgres-db
```

Chay backend tren Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Chay backend tren macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Kiem tra compile/test:

```bash
cd backend
./mvnw test
```

## Use Case

### Customer

- Dang ky, dang nhap, refresh token, dang xuat.
- Xem danh sach event, tim kiem event, xem chi tiet event.
- Xem hang ve cua event va danh sach ghe/ticket theo ticket type.
- Tao order tu danh sach ticket.
- Them/xoa ticket trong order khi order con `PENDING`.
- Tao QR payment cho order va xem payment hien tai.
- Huy order truoc khi thanh toan.
- Xem danh sach ve da mua, xem chi tiet ve va lay QR cua ve.
- Cap nhat profile va doi mat khau.

### Admin

- Tao, sua, xoa event.
- Tao ticket type cho event, cap nhat ticket type, xoa ticket type.
- Tao ticket theo danh sach seat number, xoa ticket hop le.
- Quan ly user: xem danh sach, khoa/mo khoa tai khoan.
- Quan ly staff: tao staff, gan staff vao event.
- Xac nhan payment thu cong cho order.
- Cap nhat trang thai order.
- Xem bao cao doanh thu va bao cao ticket.

### Staff

- Validate QR truoc khi check-in.
- Check-in ticket bang `qrCodeHash`.
- Chi check-in duoc ticket thuoc event ma staff dang duoc gan.

### System

- Giu ve trong 15 phut khi customer tao order.
- Tu dong expire order `PENDING` khi het thoi gian giu ve.
- Tra ve ve ve `AVAILABLE` khi order bi huy hoac het han.
- Chuyen ticket sang `SOLD` khi order duoc xac nhan thanh toan.
- Chuyen ticket sang `USED` khi staff check-in thanh cong.
- Gui email xac nhan payment va email ve kem QR sau khi payment thanh cong neu mail duoc bat.

## Flow Nghiep Vu

### 1. Auth Flow

1. Client goi `GET /api/auth/csrf` de lay CSRF token.
2. Customer dang ky bang `POST /api/auth/register`.
3. User dang nhap bang `POST /api/auth/login`.
4. Backend tao access token va refresh token, sau do set vao HTTP-only cookie.
5. Client goi API protected bang cookie da co.
6. Khi access token het han, client goi `POST /api/auth/refresh`.
7. Dang xuat bang `POST /api/auth/logout`, backend clear auth cookies.

### 2. Event Va Inventory Flow

1. Admin tao event bang `POST /api/admin/events`.
2. Admin tao ticket type cho event bang `POST /api/events/{eventId}/ticket-types`.
3. Admin tao ticket/ghe bang `POST /api/ticket-types/{id}/tickets`.
4. Customer xem event public bang `GET /api/events`.
5. Customer xem hang ve bang `GET /api/events/{eventId}/ticket-types`.
6. Customer xem ticket trong hang ve bang `GET /api/ticket-types/{id}/tickets`.

### 3. Booking Va Hold Flow

1. Customer tao order bang `POST /api/orders` voi danh sach `ticketId`.
2. Backend kiem tra ticket co the mua:
   - `AVAILABLE`, hoac `HOLDING` nhung da het hold.
   - ticket type con `remainingQuantity`.
   - khong trung ticket trong cung order.
3. Ticket duoc chuyen sang `HOLDING`.
4. `holdExpiresAt` duoc set sau 15 phut.
5. `remainingQuantity` cua ticket type giam tuong ung.
6. Order duoc tao voi status `PENDING`.

### 4. Payment Flow

1. Customer tao payment bang `POST /api/orders/{orderId}/payments` hoac `POST /api/orders/{orderId}/payments/qr`.
2. Backend yeu cau order thuoc customer, dang `PENDING`, con item va chua het hold.
3. Backend tao payment method `PERSONAL_QR` voi `paymentCode`, `qrUrl`, `transferContent`, `expiredAt`.
4. Neu payment `PENDING` con han da ton tai, backend tra lai payment cu.
5. Admin xac nhan thu cong bang `PATCH /api/admin/orders/{orderId}/payments/confirm`.
6. Backend chuyen order sang `PAID`, payment sang `SUCCESS`, ticket tu `HOLDING` sang `SOLD`.
7. Sau commit, system phat event gui email xac nhan payment va email ticket QR neu `app.mail.enabled=true`.

### 5. Cancel Va Expiration Flow

1. Customer huy order bang `PATCH /api/orders/{orderId}/cancel` khi order con `PENDING`.
2. Backend cancel payment `PENDING`, release ticket ve `AVAILABLE`, tang lai `remainingQuantity`.
3. Scheduler `PendingOrderExpirationScheduler` chay theo delay cau hinh.
4. Order `PENDING` co hold het han se chuyen sang `EXPIRED`.
5. Payment `PENDING` cua order do chuyen sang `EXPIRED`.
6. Ticket dang `HOLDING` duoc release.

### 6. Check-In Flow

1. Customer lay QR ticket bang `GET /api/tickets/{ticketId}/qr`.
2. Staff scan QR de lay `qrCodeHash`.
3. Staff goi `POST /api/tickets/check-in/validate` de xem ticket co hop le khong.
4. Staff goi `POST /api/tickets/check-in` de check-in that.
5. Backend kiem tra:
   - ticket ton tai theo `qrCodeHash`.
   - staff duoc gan dung event.
   - ticket dang `SOLD`.
   - ticket chua `USED` va chua `checkedIn`.
6. Backend set `checkedIn=true`, `checkedInAt=now`, status `USED`.

## Endpoint

Base URL local: `http://localhost:8080`

Response mac dinh duoc boc bang:

```json
{
  "code": 200,
  "success": true,
  "message": "Success",
  "data": {}
}
```

Endpoint tra `PageResponse` co them `page`, `size`, `totalElements`, `totalPages`, `last`.

Endpoint `GET /api/tickets/{ticketId}/qr` tra raw `image/png`, khong boc `ApiResponse`.

### Auth

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| GET | `/api/auth/csrf` | Public | Lay CSRF token |
| POST | `/api/auth/register` | Public | Dang ky customer |
| POST | `/api/auth/login` | Public | Dang nhap va set token cookie |
| POST | `/api/auth/refresh` | Public | Tao access token moi tu refresh token |
| POST | `/api/auth/logout` | Public | Clear auth cookies |
| GET | `/api/auth/me` | ADMIN/STAFF/CUSTOMER | Lay thong tin user hien tai |

### Customer Profile

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| GET | `/api/users/me` | CUSTOMER | Lay profile customer |
| PUT | `/api/users/me` | CUSTOMER | Cap nhat full name, phone number |
| PUT | `/api/users/change-password` | CUSTOMER | Doi mat khau |

### Public Event Va Ticket Catalog

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| GET | `/api/events?page=&size=` | Public | Lay danh sach event |
| GET | `/api/events/search?page=&size=` | Public | Tim event theo `keyword` trong request body |
| GET | `/api/events/{eventId}` | Public | Lay chi tiet event |
| GET | `/api/events/{eventId}/ticket-types` | Public | Lay ticket type cua event |
| GET | `/api/ticket-types/{id}` | Public | Lay chi tiet ticket type |
| GET | `/api/ticket-types/{id}/tickets` | Public | Lay danh sach ticket/ghe cua ticket type |

### Admin Event Va Ticket Inventory

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| POST | `/api/admin/events` | ADMIN | Tao event |
| PUT | `/api/admin/events/{eventId}` | ADMIN | Cap nhat location/startTime/endTime |
| DELETE | `/api/admin/events/{eventId}` | ADMIN | Xoa event |
| POST | `/api/events/{eventId}/ticket-types` | ADMIN | Tao ticket type cho event |
| PUT | `/api/ticket-types/{id}` | ADMIN | Cap nhat price/totalQuantity |
| DELETE | `/api/ticket-types/{id}` | ADMIN | Xoa ticket type |
| POST | `/api/ticket-types/{id}/tickets` | ADMIN | Tao tickets theo danh sach `seatNumber` |
| DELETE | `/api/tickets/{ticketId}` | ADMIN | Xoa ticket neu con `AVAILABLE` va hop le |

### Order Va Payment

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| POST | `/api/orders` | CUSTOMER | Tao order va hold ticket |
| GET | `/api/orders?page=&size=` | CUSTOMER | Lay danh sach order cua customer |
| GET | `/api/orders/{orderId}` | CUSTOMER | Lay chi tiet order |
| PATCH | `/api/orders/{orderId}/status` | ADMIN | Cap nhat status order |
| PATCH | `/api/orders/{orderId}/cancel` | CUSTOMER | Huy order `PENDING` |
| DELETE | `/api/orders/{orderId}` | CUSTOMER | Xoa order chua thanh toan |
| GET | `/api/orders/{orderId}/items` | CUSTOMER | Lay order items |
| GET | `/api/orders/{orderId}/items/{itemId}` | CUSTOMER | Lay chi tiet order item |
| POST | `/api/orders/{orderId}/items` | CUSTOMER | Them ticket vao order `PENDING` |
| DELETE | `/api/orders/{orderId}/items/{itemId}` | CUSTOMER | Xoa ticket khoi order `PENDING` |
| POST | `/api/orders/{orderId}/payments` | CUSTOMER | Tao QR payment |
| POST | `/api/orders/{orderId}/payments/qr` | CUSTOMER | Alias tao QR payment |
| GET | `/api/orders/{orderId}/payments/current` | CUSTOMER | Lay payment hien tai cua order |
| PATCH | `/api/admin/orders/{orderId}/payments/confirm` | ADMIN | Xac nhan payment thu cong |

### Customer Ticket Va Staff Check-In

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| GET | `/api/tickets/my` | CUSTOMER | Lay danh sach ticket da mua/da dung |
| GET | `/api/tickets/{ticketId}` | CUSTOMER | Lay chi tiet ticket cua customer |
| GET | `/api/tickets/{ticketId}/qr` | CUSTOMER | Lay QR image cua ticket |
| POST | `/api/tickets/check-in/validate` | STAFF | Validate QR truoc khi check-in |
| POST | `/api/tickets/check-in` | STAFF | Check-in ticket |

### Admin User, Staff Va Report

| Method | Endpoint | Role | Muc dich |
| --- | --- | --- | --- |
| GET | `/api/admin/users?page=&size=` | ADMIN | Lay danh sach user |
| PATCH | `/api/admin/users/{userId}/active` | ADMIN | Khoa/mo khoa user |
| GET | `/api/admin/staffs?page=&size=` | ADMIN | Lay danh sach staff |
| POST | `/api/admin/staffs` | ADMIN | Tao staff |
| PATCH | `/api/admin/staffs/{staffId}/managed-event` | ADMIN | Gan staff vao event |
| GET | `/api/admin/reports/revenue?startDate=&endDate=` | ADMIN | Bao cao doanh thu |
| GET | `/api/admin/reports/tickets` | ADMIN | Bao cao ticket |
| GET | `/api/admin/ticket-management/summary/{ticketTypeId}` | ADMIN | Tong quan ticket type |
| GET | `/api/admin/ticket-management/sold-list/{ticketTypeId}` | ADMIN | Danh sach ticket da ban |

## Request Body Chinh

### Register

```json
{
  "email": "customer@example.com",
  "password": "123456",
  "confirmPassword": "123456",
  "fullName": "Nguyen Van A",
  "phoneNumber": "0912345678"
}
```

### Create Event

```json
{
  "title": "Music Night 2026",
  "description": "Mini concert",
  "location": "Ho Chi Minh City",
  "startTime": "2026-06-20T18:00:00.000+07:00",
  "endTime": "2026-06-20T22:00:00.000+07:00",
  "imageUrl": "https://example.com/banner.jpg"
}
```

### Create Ticket Type

```json
{
  "name": "VIP",
  "price": 1500000,
  "totalQuantity": 50
}
```

### Create Tickets

```json
{
  "seatNumber": ["A01", "A02", "A03"]
}
```

### Create Order

```json
{
  "items": [
    { "ticketId": 1 },
    { "ticketId": 2 }
  ]
}
```

### Check-In

```json
{
  "qrCodeHash": "ticket-qr-code-hash"
}
```

## Trang Thai Nghiep Vu

### OrderStatus

- `PENDING`
- `PAID`
- `CANCELLED`
- `EXPIRED`

### TicketStatus

- `AVAILABLE`
- `HOLDING`
- `SOLD`
- `USED`
- `CANCELLED`

### PaymentStatus

- `PENDING`
- `SUCCESS`
- `EXPIRED`
- `CANCELLED`

### PaymentMethod

- `PERSONAL_QR`
