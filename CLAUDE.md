# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

This is a monorepo with two independently-built projects:

- `Backend/JobPortal/` — Spring Boot 3.5 (Java 17) REST API, Maven build, PostgreSQL.
- `Frontend/JobPortalFront/` — React 19 + Vite SPA, Tailwind CSS v4.

There is no root-level build tool tying them together; commands must be run from inside each project directory.

## Current focus

Backend only — ignore the frontend unless the user says otherwise.

## Common commands

### Backend (`Backend/JobPortal/`)

```
./mvnw spring-boot:run          # run the API locally (defaults to port 8080)
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName    # run a single test class
./mvnw clean package            # build the jar (target/)
```

Local setup requires a `src/main/resources/application.properties` (gitignored; copy from `application.properties.example` and fill in real values) with a PostgreSQL connection, a `JWT_SECRET` env var, Cloudinary credentials, and SMTP credentials — the app fails to start without them. `application-dev.properties` configures an in-memory H2 database and is meant for the `dev` Spring profile (e.g. tests).

### Frontend (`Frontend/JobPortalFront/`)

```
npm install
npm run dev        # Vite dev server
npm run build       # production build
npm run lint         # ESLint
npm run preview      # preview a production build
```

The frontend reads the API base URL from `VITE_API_URL` (see `.env` / `.env.production`), falling back to `http://localhost:8080`. There is no configured test runner in this project.

## Architecture

### Backend: layered Spring Boot app

`Controller → Service (interface in `service/interfaces`, impl in `service/impl`) → Repository (Spring Data JPA) → Entity`. Controllers only talk to service interfaces, never repositories directly. Request/response payloads are always DTOs (`dto/request`, `dto/response`) — entities are never returned from controllers.

Core entities (`entity/`): `User` (base, with `Role` = `JOB_SEEKER` or `EMPLOYER`) → `JobSeeker` / `Employer`, `Job`, `Application`, `Notification`. Enums live in `enums/` (`JobType`, `WorkMode`, `ApplicationStatus`, `Role`).

**Auth**: stateless JWT. `security/JwtAuthFilter` validates the `Authorization: Bearer <token>` header and populates the `SecurityContext` on every request; `security/JwtUtil` issues/parses tokens. `config/SecurityConfig` wires the filter chain — `/auth/**` and `GET /jobs/**` (except `/jobs/my`, which requires `EMPLOYER`) are public, everything else requires authentication, and endpoint/role checks are enforced with `@PreAuthorize`/`hasRole` in addition to the filter chain. `filter/AuthRateLimitFilter` (Bucket4j + Caffeine) throttles auth endpoints and runs before the JWT filter.

**Notifications**: implemented with an Observer pattern (`designpatterns/Observer/`). `ApplicationObserver` is notified on application status changes and persists a `Notification` (see `JobSeekerNotificationObserver`). Combined with `config/AsyncConfig` (`@EnableAsync`) for non-blocking notification delivery, and `service/impl/EmailServiceImpl` for outbound email (Brevo SMTP).

**File uploads**: profile pictures and resumes go through `service/impl/CloudinaryService` (multipart upload to Cloudinary), not local disk.

**Errors**: `exception/GlobalExceptionHandler` centralizes error responses; custom exceptions (`BadRequestException`, `ResourceNotFoundException`, `DuplicateResourceException`, `UnauthorizedException`) map to appropriate HTTP statuses.

**Pagination**: job listing and search endpoints return `PageResponseDTO`-wrapped, paged results.

Consult `Backend/JobPortal/README.md` for the full REST API surface (auth, jobs, saved jobs, applications, notifications, profile endpoints) and the roles/permissions matrix.

### Frontend: React SPA

Routing is centralized in `src/App.jsx` using `react-router-dom`. Role-gated routes are wrapped in `<RequireRole role="JOB_SEEKER|EMPLOYER">` (`src/components/RequireRole.jsx`), which reads auth state from `AuthContext`.

**Auth state**: `src/context/AuthContext.jsx` holds the current user and JWT in `localStorage`, decodes the token (`jwt-decode`) to schedule an automatic logout at expiry, and exposes `login`/`logout`/`updateUser`.

**API layer**: all HTTP calls go through `src/api/client.js`, an Axios instance that attaches the bearer token from `localStorage` on every request and force-redirects to `/login` on a 401 response. `src/api/config.js` resolves the API base URL from `VITE_API_URL`. New API calls should go through this shared `apiClient`, not raw `axios` or `fetch`.

**Notifications**: `src/context/NotificationContext.jsx` plus `src/components/NotificationToastManager.jsx` / `NotificationBell.jsx` poll/display backend notifications as toasts and a navbar dropdown.

**Structure**: `pages/` are route-level screens (separate Employer and Job Seeker page sets, e.g. `EmployerDashboard.jsx` vs `JobSeekerProfilePage.jsx`); `components/` are reusable pieces shared across pages; `modals/` hold the sign-up modals (`JobSeekerModal`, `EmployerModal`); `utils/` has small helpers (e.g. `toastUtils.js`).

Errors are caught app-wide by `src/components/ErrorBoundary.jsx`, wrapping the router's `<Routes>`.

## Deployment

- Backend: Docker image (`Backend/JobPortal/Dockerfile`) deployed on DigitalOcean App Platform, which auto-deploys on every push to `main`.
- Frontend: Vercel (`Frontend/JobPortalFront/vercel.json`).
- Every push to `main` is a production release.
- `nixpacks.toml` and `railway.toml` are leftovers from an earlier Railway setup and are likely unused. Don't rely on them, and ask before removing them.
- CORS is restricted via `cors.allowed-origins` (backend) to the deployed frontend origin(s).

## Working rules

- NEVER run `git commit`, `git push`, `git add`, or any command that changes git history or the index. I review and commit myself.
- When a task is done, run `./mvnw test`, then summarize what changed and suggest a conventional commit message (e.g. `fix(backend): ...`). Don't commit.
- Never read, print, or commit `application.properties` or any secrets. Use `application.properties.example` to see which settings exist.
- One topic per task, so each task maps to one commit. Don't mix refactoring with behavior changes.
- Ask before changing the DB schema or public API contracts.
- Ask before adding new dependencies.
- Keep the layering: controllers use service interfaces, payloads are DTOs, entities never leave the service layer.
- Ignore the frontend unless I say otherwise.
