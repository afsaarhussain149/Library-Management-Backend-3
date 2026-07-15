# Library Management System — Spring Boot (converted from Node.js/Express)

Node.js backend (`lib-backend-master`) ka pura conversion, isi repo ke **file
structure/flow** par based: flat `controller -> service -> serviceImpl ->
generic DAO (raw SQL via JPA EntityManager)` pattern, PostgreSQL database.

## Run karne ke steps

1. **PostgreSQL** database banao:
   ```
   createdb lib_db
   psql -U postgres -d lib_db -f src/main/resources/db/init.sql
   ```
2. `src/main/resources/application.properties` me apna DB password /
   `jwt.secret` / `razorpay.key-id` / `razorpay.key-secret` daalo.
3. Build & run:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
   Server `http://localhost:8080` par chalega.

## Kya badla / kya same rakha

- **Same flow** reference project jaisa: `bean` (POJO DTOs) → `JavaConstant`
  (raw SQL query constants) → `dao/daoImpl` (generic EntityManager-based DAO,
  unchanged) → `services/servicesImpl` → `controller`. `ddl-auto=none`, tables
  manually via `init.sql` — reference project jaisa hi convention.
- Node ke ek route-file = ek Java controller (AuthController, PaymentController,
  SeatController, ShiftController, SelectionController, PlanController,
  ComplaintController, QueryController) — original `routes/*.js` split ko mirror
  karta hai.
- **Security improvements** (Node version me security issues the flagged):
  - Admin + User password ab **BCrypt** se hash hoti hai (Node me admin
    plaintext compare tha).
  - JWT secret ab `application.properties` se aata hai (hardcoded nahi).
- **MongoDB → PostgreSQL**: embedded documents (payment.plan, payment.shift)
  flatten karke normal columns bana diye; `seats` array ko comma-separated
  text me store kiya (e.g. `"12,13"`); `metadata` ko `jsonb` column me.
- **Cloudinary upload → local disk**: `FileStorageUtil` photo ko
  `uploads/` folder me save karta hai aur `/uploads/<file>` path return karta
  hai (`WebConfig` isko static resource ki tarah serve karta hai). Production
  ke liye isko S3/Cloudinary client se replace kar sakte ho.
- **Node cron job → `@Scheduled`**: `PlanExpiryScheduler` har minute chalke
  expired paid plans ko inactive + seat-block kar deta hai (same interval
  jaisa Node ka `sheduler/server.js`).
- Razorpay: `com.razorpay:razorpay-java` SDK use kiya hai (`create-order` /
  `verify` APIs). **Note:** Razorpay SDK ka exact method signature version ke
  hisaab se thoda alag ho sakta hai — `mvn clean install` ke baad agar
  `RazorpayClient`/`Order` par koi compile error aaye to unki javadoc/GitHub
  README dekh kar signature match kar lena (maine 1.4.3 assume kiya hai).

## Naye Postgres tables (`init.sql`)

| Table | Kaam |
|---|---|
| `admin_user` | Admin login/profile |
| `app_user` | Members/students |
| `payment` | Online + cash payments, plan/shift/seats flattened |
| `seat_selection` | Seat booking records |
| `shift_selection` | Shift booking records |
| `user_selection` | User ka plan+option pick (payment se pehle) |
| `plan_catalog` + `plan_option` | Subscription plans catalog (auto-seeded) |
| `complaint` | User complaints |
| `public_query` | Contact-us form queries |

## API endpoints (same URL paths jo Node me the)

- `/api/auth/**` → AuthController (register, login, admin, profile edit)
- `/api/payments/**` → PaymentController (Razorpay orders, cash requests, seat status, pagination)
- `/api/seats/**`, `/api/seat-selection/**` → SeatController
- `/api/shift-selection/**` → ShiftController
- `/api/selection/**` → SelectionController
- `/api/plans` → PlanController
- `/api/complaint/**` → ComplaintController
- `/api/query/**` → QueryController

Sab endpoints Node version ke exact same URL/method (GET/POST/PUT/PATCH/DELETE)
follow karte hain, taaki frontend me kam se kam changes karne pade.
