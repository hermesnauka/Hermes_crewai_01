# ThreatView 2026 — Angular implementation (app02_angular)

One of the Java×2 course implementations (Spring Boot backend on both). This one pairs
the same backend stack with **Angular 18** instead of app01's React; app01 remains the
contract of record for the API. Product is renamed here (`ThreatView`, package
`com.threatview`, artifact `threatview-backend`) — this is a genuinely separate Spring
Boot app, not a shared/forked copy of app01's code. It mirrors app01's structure closely
(same controller/service/repository/entity layering, same `StringListConverter`-style
comma-joined `TEXT` columns for `stride`/`tags`, same Flyway migration approach) but was
built independently against the contract below.

## Scope: this is Phase-1 parity, not the full vision

`PLAN.md`, `requirements.md`, `SDLC_analysis.md`, and `user_stories+tests.md` describe a
9-phase, 19-user-story end state (Cornucopia card catalogue across 6+ suits, i18n via
ngx-translate, NgRx Signals state, code samples in 5 languages, Redis caching, rate
limiting, admin CRUD, SVG diagram generation). **None of that is built.** What exists
today is `FrameworkController`/`ThreatController` (read-only), a JWT auth skeleton, 4
seeded frameworks, and a handful of seeded threats. Read `PLAN.md` for the destination,
but don't assume anything past the API contract below is implemented — and don't be
surprised the frontend doesn't yet have a login screen, `/frameworks` route, or i18n
toggle that actually translates (see below).

## API contract (source of truth: `../app01_react/backend/src/main/java/com/securevision/`)

```
POST /api/v1/auth/login        {username, password} -> {token, tokenType:"Bearer", role:"ADMIN"} | 401
GET  /api/v1/frameworks        -> Framework[]
GET  /api/v1/frameworks/:code  -> Framework | 404
GET  /api/v1/threats           ?frameworkCode&severity&stride&tag&q&page&size&sort -> Page<ThreatSummary>
GET  /api/v1/threats/:id       -> ThreatDetail | 404
GET  /health                   -> {"status":"UP"}
```
`Page<T> = {content, totalElements, totalPages, number, size}`. Error body on 4xx:
`{timestamp, status, error, message}`. `threat.model.ts` / `framework.model.ts` under
`frontend/src/app/shared/models/` are written against this exact shape — check app01's
Java DTOs before changing either side.

## What's actually built vs. what PLAN.md/README imply

- Backend: `FrameworkController`, `ThreatController` only (no `CardSuitController`,
  `MitigationController`, `MatrixController`, `SearchController`, `ExportController`,
  `AdminController` from the PLAN.md architecture diagram — those are all aspirational).
  Later-phase tables (`Mitigation`, `CodeSample`, `CrossReference`, `ThreatTranslation`,
  `CornucopiaCard`, `ContentHash`) exist in the `V1__init_schema.sql` migration but are
  **not** JPA-mapped or exposed.
- Frontend: no login component, no auth interceptor, no `/frameworks` or `/threats`
  route yet — just `AppComponent` shell + `DashboardComponent` (stat cards, client-side
  quick search over frameworks). `LanguageToggleComponent` persists a locale to
  `localStorage` (`tv_locale`) but doesn't translate anything — `ngx-translate` isn't
  wired up despite being in PLAN.md's stack table.

## Angular-specific things worth knowing before you touch this

- **Standalone components throughout**, no `NgModule` anywhere — bootstrapped via
  `app.config.ts` (`provideRouter`, `provideHttpClient`, `provideAnimations`,
  `provideZoneChangeDetection`). If you scaffold a new component, use
  `ng generate --standalone` (the CLI default in Angular 18) and wire it into
  `app.routes.ts`, not into a module.
- **`frontend/proxy.conf.json`** proxies `/api/v1` to `localhost:8080` for `ng serve`
  (`npm start`). This is dev-only — nginx (`nginx/nginx.conf`) does the equivalent
  reverse-proxying in the Docker Compose setup. If API calls 404 during local dev, check
  this file before suspecting the backend.
- Services (`ThreatService`, `FrameworkService` in `core/services/`) are
  `providedIn: 'root'`, inject `HttpClient` via `inject()` (not constructor injection),
  and return `Observable<T>` — don't reach for `async/await` + `firstValueFrom` unless
  there's a specific reason; keep it idiomatic RxJS/`| async` pipe in templates.
- `tsconfig.json` has `strict` + Angular's `strictTemplates` on. Expect template
  type-checking errors that React/JSX wouldn't catch — read the actual compiler error,
  it's usually a real type mismatch against the model in `shared/models/`, not noise.
- All components declared with `ChangeDetectionStrategy.OnPush` per PLAN.md's D-09 —
  if a template isn't updating after a state change, check whether a signal/input was
  mutated in place instead of reassigned before assuming OnPush is the culprit.

## Running the stack locally

No Docker on this machine. Use `scripts/local-dev-up.sh` / `scripts/local-dev-down.sh`
(portable Postgres/Maven/Node under `C:\Users\krish\tools\`, not containers). Elsewhere,
`docker compose up --build` brings up Postgres 16, backend, frontend+nginx per
`docker-compose.yml`. Dev admin login is `admin` / `changeme-dev-only` (see `.env`) —
never commit real `ADMIN_PASSWORD_HASH`/`JWT_SECRET` values.
