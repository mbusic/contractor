# Kricco – Field Service / Order Tracking (POC)

Web application for a repair/maintenance contracting business.  
Clients request work orders, dispatchers manage and assign them, servicers execute and record costs, and the office prints quotes, work orders, reports, and invoices.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.8+ |
| Node.js | 18+ |
| npm | 9+ |

No database installation required — H2 is embedded and runs automatically.

---

## Running the application

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

Starts on **http://localhost:8080**.  
The H2 database file is created at `backend/data/contractordb.mv.db` and survives restarts.

### 2. Frontend

```bash
cd frontend
npm install
npx @angular/cli@19 serve
# shorthand after first install: npx ng serve
```

Starts on **http://localhost:4200**.  
Open this URL in the browser — the backend must already be running.

---

## Seeded login credentials

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `admin` | ADMIN | Full access; manages users, branches, clients |
| `office` | `office` | OFFICE | Dispatcher; creates/assigns orders, generates documents |
| `servicer` | `servicer` | SERVICER | Field crew; accepts orders, records actual costs, adds notes/photos |
| `client` | `client` | CLIENT | Creates own orders, reads own order status |

---

## H2 database console

Available at **http://localhost:8080/h2-console** while the backend is running.

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/contractordb` |
| User name | `sa` |
| Password | *(leave empty)* |

---

## Seeded data

**Branches:** Kricco Zagreb, Kricco Split, Kricco Zadar, Kricco Osijek, Kricco Pula

**Clients:**
- `Petar Perić d.o.o.` (COMPANY) — two locations in Zagreb and Split
- `Ana Anić` (INDIVIDUAL) — address in Zagreb

**Orders:**

| # | Branch | Urgency | Status |
|---|--------|---------|--------|
| 001/25 | Kricco Zagreb | 1 dan | Riješen |
| 002/25 | Kricco Split | 1 tjedan | U tijeku |
| 003/25 | Kricco Zadar | 1 mjesec | Na čekanju |
| 004/25 | Kricco Osijek | Isti dan | Na čekanju |
| 005/25 | Kricco Pula | 6 mjeseci | Na čekanju |

---

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login, returns JWT |
| GET / POST | `/api/orders` | List (role-filtered) / create |
| GET / PUT / DELETE | `/api/orders/{id}` | Detail / update / delete |
| PATCH | `/api/orders/{id}/status` | Change status |
| POST | `/api/orders/{id}/notes` | Add note |
| POST / DELETE | `/api/orders/{id}/photos` | Upload / delete photo (max 6) |
| GET | `/api/orders/{id}/documents/{type}` | HTML document (`quote`, `workorder`, `report`, `invoice`) |
| GET | `/api/files/{filename}` | Serve uploaded photo |
| GET / POST / PUT / DELETE | `/api/branches` | Branch CRUD |
| GET / POST / PUT / DELETE | `/api/clients` | Client CRUD |
| GET / POST / PUT / DELETE | `/api/users` | User CRUD (ADMIN) |

---

## Project structure

```
Contractor/
├── backend/                        # Spring Boot 3.3.5, Java 21, Maven
│   ├── src/main/java/hr/qnr/contractor/
│   │   ├── config/         DataSeeder, SecurityConfig
│   │   ├── controller/     Auth, Order, File, Document, Branch, Client, User
│   │   ├── dto/            Request/response records
│   │   ├── entity/         Branch, Client, Location, User, Order, OrderNote, OrderPhoto, OrderSequence
│   │   ├── repository/     Spring Data JPA interfaces
│   │   ├── security/       JwtUtil, JwtAuthFilter, UserPrincipal, UserDetailsServiceImpl
│   │   └── service/        Order, Document, Branch, Client, User, FileStorage, OrderNumberGenerator
│   ├── src/main/resources/
│   │   └── application.properties
│   └── data/               H2 database files (git-ignored)
│
├── frontend/                       # Angular 19, standalone components, SCSS
│   └── src/app/
│       ├── core/
│       │   ├── guards/     authGuard, adminGuard
│       │   ├── interceptors/ authInterceptor (attaches Bearer token)
│       │   ├── models/     TypeScript interfaces
│       │   └── services/   AuthService, OrderService, BranchService, ClientService, UserService
│       └── features/
│           ├── auth/       LoginComponent
│           ├── orders/     OrderListComponent, OrderDetailComponent, OrderFormComponent
│           ├── servicer/   ServicerHomeComponent, TimesheetComponent
│           └── admin/      AdminComponent (Users / Branches / Clients tabs)
│
└── README.md
```

---

## Document printing

From the order detail screen, the OFFICE role can open any of four printable documents:

- **Ponuda** (Quote) — estimated costs, signature line
- **Radni nalog** (Work order) — on-site recording fields, photo thumbnails
- **Izvještaj** (Report) — actual costs, notes, photos, signature
- **Račun** (Invoice) — actual costs, VAT placeholder, signature

Each document opens in a new browser tab with an **"Ispis / PDF"** button.  
Use the browser's **File → Print** (or Ctrl+P) and select **"Save as PDF"** to generate a PDF.

---

## Order number format

Orders are numbered `NNN/YY` per calendar year (e.g. `006/25`).  
The sequence is stored in the `order_sequences` table and increments atomically.

---

## Role permissions summary

| Action | ADMIN | OFFICE | SERVICER | CLIENT |
|--------|:-----:|:------:|:--------:|:------:|
| See all orders | ✓ | ✓ | — | — |
| See assigned orders | — | — | ✓ | — |
| See own orders | — | — | — | ✓ |
| Create order | ✓ | ✓ | — | ✓ |
| Edit order / assign servicer | ✓ | ✓ | — | — |
| Delete order | ✓ | ✓ | — | — |
| Accept order / change status | — | ✓ | ✓ | — |
| Enter actual costs | — | ✓ | ✓ | — |
| Add notes / photos | — | ✓ | ✓ | — |
| Generate documents | — | ✓ | — | — |
| Manage users / branches / clients | ✓ | — | — | — |

---

## What is intentionally NOT in this POC

- **Rate engine** — labour, travel, and material rates are not calculated; all cost fields are entered manually
- **Approval workflow** — no multi-step approval process for orders or quotes
- **Itemised materials** — material cost is a single decimal field, not a line-item list
- **Notifications** — no email, SMS, or push notifications
- **Calendar / scheduling** — no calendar view or time slot booking
- **Aggregate reporting** — no dashboards, charts, or cross-order analytics
- **Cloud / object storage** — photos are stored on the local filesystem (`./uploads`)
- **Password reset / registration** — users are created by ADMIN only; no self-service auth flows
- **Multi-language** — UI is Croatian only; no i18n infrastructure
- **VAT / legal invoice layout** — the Račun document has a clearly marked placeholder only
- **Offline / PWA** — no service worker or offline support
