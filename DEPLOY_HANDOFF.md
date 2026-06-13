# Cloud deployment handoff — Kricco Contractor POC

### What this is

A full-stack field service order tracking app:
- **Backend:** Spring Boot 3.3.5, Java 21, Maven, Spring Security (stateless JWT / HS256), Spring Data JPA
- **Frontend:** Angular 19, standalone components, lazy-loaded routes, SCSS
- **Repo root:** `Contractor/` with `backend/` and `frontend/` subdirectories
- **README** at repo root has full feature description, credentials, and API table

---

### Current state (development only — not production-ready)

| Concern | Current (dev) | Needs to change for prod |
|---------|--------------|--------------------------|
| Database | H2 embedded file (`./data/contractordb`) | External PostgreSQL |
| File storage | Local filesystem (`./uploads`) | Object storage (S3 / GCS / etc.) |
| Backend URL in frontend | Hardcoded `http://localhost:8080` in `frontend/src/app/core/services/*.ts` | Environment-based (`environment.ts`) |
| CORS allowed origin | Hardcoded `http://localhost:4200` in `SecurityConfig.java` | Configurable via env var |
| JWT secret | Plain string in `application.properties` | Env var / secret manager |
| Frontend served | Dev server (`ng serve`) | Static build served via CDN or same host |

---

### Key config files

**`backend/src/main/resources/application.properties`**
```properties
spring.datasource.url=jdbc:h2:file:./data/contractordb;AUTO_SERVER=TRUE
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
app.upload-dir=./uploads
app.jwt.secret=aim-contractor-poc-secret-key-minimum-256-bits-long-for-hs256
app.jwt.expiration-ms=86400000
server.port=8080
```

**`backend/pom.xml`** — groupId `hr.qnr`, artifactId `contractor`, Spring Boot 3.3.5, Java 21. Dependencies: `spring-boot-starter-web`, `data-jpa`, `h2`, `security`, `lombok`, `validation`, `jjwt-api/impl/jackson 0.12.6`.

**`backend/src/main/java/hr/qnr/contractor/config/SecurityConfig.java`** — CORS origin is `"http://localhost:4200"` hardcoded in `corsConfigurationSource()`.

**`backend/src/main/java/hr/qnr/contractor/service/FileStorageService.java`** — reads `app.upload-dir` property, stores files with UUID filenames. Serves them via `/api/files/{filename}` (public endpoint, no auth).

**`backend/src/main/java/hr/qnr/contractor/service/DocumentService.java`** — generated HTML documents contain `<img src="http://localhost:8080/api/files/...">` hardcoded. This needs to be the production base URL.

**Frontend API base URL** — every service file under `frontend/src/app/core/services/` has `const API = 'http://localhost:8080/api'` at the top. No Angular environment abstraction exists yet.

---

### Changes required before deploying

1. **Add PostgreSQL driver to pom.xml**, remove H2 runtime scope, add a `application-prod.properties` with the Postgres JDBC URL and credentials from env vars. Set `ddl-auto=validate` or use Flyway/Liquibase for migrations.

2. **File storage** - replace `FileStorageService` with an S3/GCS client (e.g. AWS SDK v2 or Spring Cloud AWS). The public `/api/files/` endpoint can proxy through the backend or redirect to signed/public object URLs.

3. **Frontend environment** - create `frontend/src/environments/environment.prod.ts` with the real API base URL. Replace the hardcoded string in each service with `environment.apiUrl`.

4. **CORS** - inject the allowed origin via `@Value("${app.cors.origin}")` in `SecurityConfig`, set it in env vars per deployment.

5. **JWT secret** - read from env var (`${APP_JWT_SECRET}`), ensure >= 256 bits.

6. **Document HTML base URL** - inject `@Value("${app.base-url}")` into `DocumentService` and replace the hardcoded `http://localhost:8080` prefix in photo `<img>` tags.

7. **H2 console** - disable in prod (`spring.h2.console.enabled=false`).

8. **Build the frontend** with `npx @angular/cli@19 build --configuration production` and serve `dist/frontend/browser/` as static files (S3 + CloudFront, Netlify, Vercel, Firebase Hosting, or the backend itself via `src/main/resources/static/`).

---

### Deployment options to consider

| Option | Backend | Frontend | DB | Files |
|--------|---------|----------|----|-------|
| Fly.io + Neon | Fly.io (JAR in Docker) | Fly.io static or Netlify | Neon (serverless PG) | Fly.io volume or S3 |
| Render | Render web service | Render static site | Render PG | S3 |
| AWS minimal | EC2 or App Runner | S3 + CloudFront | RDS or Aurora Serverless | S3 |
| GCP minimal | Cloud Run | Firebase Hosting | Cloud SQL | GCS |

For a POC, **Fly.io + Neon** or **Render** are the lowest-friction paths (no IAM, no VPC setup).

---

### Seed data note

`DataSeeder` is guarded by `if (branchRepo.count() > 0) return` — it only runs on an empty database. Safe to leave in for first deploy; disable or remove for production after initial setup.

---

### What the app does NOT have (intentional POC gaps)

Rate engine, approval workflow, itemised materials, notifications, calendar, aggregate reporting, cloud storage (done above), password reset, multi-language, real VAT invoice layout, offline/PWA.
