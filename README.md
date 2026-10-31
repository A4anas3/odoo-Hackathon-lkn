# 🌐 Brightloop ERP

> **Modern, High-Performance Odoo-Inspired Subscription Management & Automated Recurring Billing Enterprise Platform**  
> Built with **Spring Boot 3.4.2 (Java 21 LTS)**, **React 19 (Vite + Tailwind CSS)**, and **PostgreSQL 17 / Supabase**.

---

## 🚀 Key Highlights & Architectural Strengths

- **Enterprise Modular Monolith**: Distinct, loosely coupled feature domains (`auth`, `customer`, `product`, `plan`, `tax`, `discount`, `quotation`, `subscription`, `invoice`, `payment`, `billing`, `report`, `audit`).
- **Push-to-Database Computing**: 100% of filtering, searching, sorting, and pagination occurs inside PostgreSQL queries (`PageResponse<T>` / `Page<T>`) — eliminating in-memory bottlenecks.
- **Robust Anti-Duplicate Billing Engine**: Idempotent recurring billing engine backed by unique database constraints on `(subscription_id, billing_period_start)`. Scheduled cron job automatically processes due subscriptions.
- **Indian GST Compliance**: Automated intra-state (CGST @ 9% + SGST @ 9%) vs inter-state (IGST @ 18%) tax evaluation based on customer state vs company state.
- **Dynamic PDF Invoice Generation**: OpenPDF-powered A4 tax invoices with QR, GST breakdown, payment status badges, and itemized billing lines.
- **FIFO Multi-Invoice Payment Allocation**: Transactional payment recording with automatic waterfall allocation across oldest outstanding invoices, or custom line-item allocation.
- **Pure JavaScript Frontend**: Strictly plain JavaScript (`.jsx` / `.js`) with React Router, Lucide icons, Recharts interactive metrics, and floating toast notifications.
- **Strict Light Theme**: High-contrast pure black typography (`text-black` / `text-zinc-950`) with Odoo signature purple accents (`#714B67`).

---

## 🏗 System Architecture & Technology Stack

```
hackathon/
├── backend/                       # Spring Boot 3.4.2 + Java 21 LTS
│   ├── src/main/java/com/brightloop/
│   │   ├── audit/                 # Audit logging of critical enterprise events
│   │   ├── billing/               # Automated recurring billing engine & scheduler
│   │   ├── common/                # Shared base entities, DTOs, GlobalExceptionHandler
│   │   ├── customer/              # Customer CRM & tax registration master
│   │   ├── discount/              # Percentage & fixed discount coupons with limits
│   │   ├── invoice/               # Invoices, PDF generator & tax breakdown
│   │   ├── payment/               # Multi-method payments & FIFO allocation
│   │   ├── plan/                  # Subscription plans & recurring pricing intervals
│   │   ├── product/               # Product catalog & multi-attribute variants
│   │   ├── quotation/             # Quotation templates & 1-click subscription conversion
│   │   ├── report/                # MRR, ARR, revenue metrics & CSV exports
│   │   ├── security/              # Spring Security 6 & JWT stateless filter chain
│   │   ├── subscription/          # Subscription lifecycle state machine
│   │   └── tax/                   # GST tax engine (CGST/SGST/IGST)
│   ├── src/main/resources/
│   │   ├── db/migration/          # Flyway SQL migrations (V1 schema, V2 seed, V3 alignment)
│   │   └── application.yml        # Enterprise configuration
│   └── Dockerfile                 # Multi-stage JDK 21 build
│
├── frontend/                      # React 19 + Vite + Tailwind CSS
│   ├── src/
│   │   ├── api/                   # Centralized Axios client with JWT interceptor
│   │   ├── components/            # Reusable UI components (Sidebar, Navbar, Modal, StatCard)
│   │   ├── context/               # AuthContext, ToastContext
│   │   ├── pages/                 # Full feature views for all 12 modules
│   │   ├── App.jsx                # Protected route tree
│   │   └── main.jsx               # React DOM root mounting
│   ├── nginx.conf                 # Nginx reverse proxy configuration
│   └── Dockerfile                 # Multi-stage build with Nginx Alpine
│
├── docker-compose.yml             # Single-command orchestration (Postgres 17, Backend, Frontend)
└── .env.example                   # Environment configuration template
```

---

## 🔑 Default Seed Credentials

Brightloop ERP comes pre-seeded with sample enterprise data, customers, plans, products, subscriptions, and administrative user accounts:

| Role | Email Address | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin@brightloop.local` | `Admin@1234` | Full access to all ERP modules, settings, and audits |
| **Billing Staff** | `staff@brightloop.local` | `Staff@1234` | Day-to-day operations (customers, subscriptions, invoices, payments) |
| **Customer Portal** | `customer@brightloop.local` | `Customer@1234` | Self-service subscription viewing and invoice receipts |

*(1-click login buttons are also provided directly on the Login page for rapid evaluation)*

---

## ⚡️ Quick Start & Running

### Option A: Running with Docker Compose (Recommended)

1. Clone and navigate to the project directory:
   ```bash
   cd hackathon
   ```
2. Start all services:
   ```bash
   docker-compose up --build -d
   ```
3. Open your browser:
   - **Frontend Web Suite**: `http://localhost:3000`
   - **Backend REST API**: `http://localhost:8080`
   - **API Health Check**: `http://localhost:8080/actuator/health`

---

### Option B: Running Natively for Development

#### 1. Start PostgreSQL 17
```bash
docker run -d --name brightloop-postgres -e POSTGRES_DB=brightloop -e POSTGRES_USER=brightloop_user -e POSTGRES_PASSWORD=brightloop_password -p 5432:5432 postgres:17-alpine
```

#### 2. Run the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*(Flyway will automatically initialize all 21 tables and seed records)*

#### 3. Run the Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## ☁️ Connecting to Cloud Supabase

To connect Brightloop ERP to your Supabase PostgreSQL database:

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Set your Supabase JDBC connection string in `.env`:
   ```env
   SPRING_DATASOURCE_URL=jdbc:postgresql://db.<YOUR-PROJECT-REF>.supabase.co:5432/postgres?sslmode=require&stringtype=unspecified
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=<YOUR-SUPABASE-PASSWORD>
   ```
3. Run `docker-compose up -d` or start the backend JAR — Flyway will migrate the Supabase database automatically!

---

## 📡 API Reference Overview

All API endpoints are documented and protected via JWT Bearer authentication:

- `POST /api/v1/auth/login` — Authenticate and receive JWT access token
- `POST /api/v1/auth/register` — Register new staff or customer account
- `GET /api/v1/reports/dashboard` — High-level executive dashboard (MRR, ARR, revenue trends)
- `GET /api/v1/reports/export/{subscriptions|invoices|payments}` — Instant CSV audit export
- `GET /api/v1/subscriptions` — Filtered, paginated subscription listing
- `POST /api/v1/subscriptions` — Create new subscription contract
- `POST /api/v1/subscriptions/{id}/{pause|resume|close|renew}` — Lifecycle state transitions
- `GET /api/v1/quotations` — Quotation management
- `POST /api/v1/quotations/{id}/convert` — 1-click convert accepted quotation to active subscription
- `GET /api/v1/invoices` — List customer invoices with balance tracking
- `GET /api/v1/invoices/{id}/pdf` — Download tax-compliant OpenPDF invoice
- `POST /api/v1/payments` — Record payment with automatic FIFO invoice balance deduction
- `GET /api/v1/billing` — Recurring billing audit records & execution history
- `POST /api/v1/billing/run-now` — Trigger on-demand recurring billing run
- `GET /api/v1/customers` — Customer CRM master
- `GET /api/v1/plans` — Subscription recurring plans
- `GET /api/v1/products` — Product catalog with variant pricing
- `GET /api/v1/taxes` — Tax configuration with GST simulator
- `GET /api/v1/discounts` — Promotion codes & usage tracking
- `GET /api/v1/audit-logs` — Immutable enterprise audit trail

---

## 🛡️ Enterprise Security & Integrity

- **Stateless JWT Security**: HS-512 signing with strict role-based access control.
- **SQL Injection Immune**: 100% parameter-bound queries and Spring Data JPA specifications.
- **CORS Configured**: Strict allow-list configured for single-page applications.
- **Audit Logging**: Every sensitive action records user ID, IP address, timestamp, and before/after JSON diffs.

---

## 📄 License
Brightloop ERP is released under the **MIT License**.
