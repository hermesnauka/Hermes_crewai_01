# SecureVision 2026 — Java/Spring Boot + React (app01_react)

**This app is the contract of record.** It is one of twelve parallel course
implementations of the same product (Java×2, Python/Django, Scala/ZIO, Go,
Haskell, Rust, C++, C#, PHP/WordPress, Swift, Kotlin) built to compare
AI-assisted development across ecosystems. Every sibling with its own backend
(`app02_angular`, `app03_python_django`, `app05_go_react`, `app06_HASKELL_react`,
and eventually `app07`/`app08`/`app10`) is supposed to mirror the API this app
actually implements. **If a sibling's behavior disagrees with this app's Java
source, the sibling is wrong, not this one** — don't "fix" this backend to
match a sibling's README.

## Scope: this is Phase-1 parity, not the full vision

`PLAN.md`, `requirements.md`, `SDLC_analysis.md`, and `user_stories+tests.md`
describe a much larger 19-user-story / 9-phase end state (Cornucopia card
catalogue, i18n, JWT roles beyond ADMIN, async CSV/PDF export, admin CRUD,
cross-framework matrix, code samples in 5 languages). **None of that is built.**
What exists today (`backend/`, `frontend/`) is Phase-1 / milestone M1 only:
frameworks + threats (read-only), one hardcoded admin login, 4 seeded
frameworks, ~33 threats. Read `PLAN.md` for the destination; don't assume
anything past the API contract below is implemented. Later-phase tables
(`Mitigation`, `CodeSample`, `CrossReference`, `ThreatTranslation`,
`CornucopiaCard`, `ContentHash`) exist in the `V1` Flyway migration but are not
JPA-mapped or exposed via the API yet.

## API contract (source of truth: `backend/src/main/java/com/securevision/`)

```
POST /api/v1/auth/login        {username, password} -> {token, tokenType:"Bearer", role:"ADMIN"} | 401
GET  /api/v1/frameworks        -> Framework[]
GET  /api/v1/frameworks/:code  -> Framework | 404
GET  /api/v1/threats           ?frameworkCode&severity&stride&tag&q&page&size&sort -> Page<ThreatSummary>
GET  /api/v1/threats/:id       -> ThreatDetail | 404
GET  /health                   -> Spring Boot Actuator {"status":"UP"}
```
`Page<T> = {content, totalElements, totalPages, number, size}` — Spring Data's
native envelope (see `ThreatController` + repository/`ThreatSpecifications.java`
for the filter logic). Error body on 4xx: `{timestamp, status, error, message}`
(`ApiExceptionHandler`). Auth is JWT **HS256** via `JwtService`
(`Keys.hmacShaKeyFor`, single shared `JWT_SECRET`) — the RS256 key-pair design
in `PLAN.md`'s aspirational D-04 was never built; HS256 is the real contract.
Swagger UI is live at `/swagger-ui.html` (`OpenApiConfig`).

Entities: `Framework`, `Threat` (`entity/`). `stride`, `cveReferences`, `tags`
are stored as **comma-joined `TEXT`** via `StringListConverter` /
`StrideSetConverter`, not native arrays — a deliberate Phase-1 shortcut (see
the converter's own doc comment) that any sibling using a real array/JSON
column type must still serialize to the same `["S","T"]` JSON shape. Note:
`ThreatResponse.from()` guards `stride` against null but passes
`cveReferences`/`tags` straight from the entity with no null-guard — the
converter itself always returns `List.of()` rather than `null`, so this is
currently safe, but don't assume the guard exists if you touch that DTO.

## Known gaps / things to fix, not "mirror"

- **`docker-compose.yml` line 39 is broken**: `ADMIN_PASSWORD_HASH: ${}` should
  be `ADMIN_PASSWORD_HASH: ${ADMIN_PASSWORD_HASH}`. `docker compose up` as
  currently checked in will not pass the admin hash through to the backend
  container. Fix this before treating `docker compose up` as a working
  smoke-test for this app.
- No rate limiting, Redis usage, `AdminController`, or CRUD endpoints yet,
  despite `docker-compose.yml` provisioning a `redis` service — it's started
  but unused by Phase-1 code.

## Running locally

```bash
docker compose up --build   # frontend :8081, backend :8080, swagger-ui.html
```
This machine has no Docker installed, so day-to-day dev actually uses
`scripts/local-dev-up.sh` / `local-dev-down.sh` — portable Postgres/JDK/Maven
under `C:\Users\krish\tools\` instead of containers. **Postgres there is a
shared instance across sibling apps on this machine** (one server on `:5432`);
the script only ensures the `securevision` role/DB exist, it doesn't own the
server, and stopping it via `local-dev-down.sh` affects other sibling apps
currently using it. `frontend/dist` is a committed Vite build output — treat
it as generated, not hand-edited. Copy `.env.example` to `.env` and set real
values before running; the dev-only admin credentials
(`admin` / `changeme-dev-only`) live in `docker-compose.yml`/README, not in
source control as real secrets.
