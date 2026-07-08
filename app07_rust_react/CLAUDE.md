# RustBastion 2026 — Rust/axum implementation (app07_rust_react)

One of twelve parallel course implementations of the same product ("SecureVision" /
here "RustBastion"). This is the Rust one: intended `axum` + `sqlx` + PostgreSQL
backend, React/TS frontend. See sibling `app01_react` (Java/Spring Boot) for the
reference implementation and `app06_HASKELL_react/CLAUDE.md` for a worked example of
this same file once its backend exists.

## Current state: backend not started

**Only `frontend/` exists.** It is a built React 18 + TypeScript + Vite app
(`node_modules/`, `dist/`, `tsconfig*.tsbuildinfo` are all present — it has been run).
There is **no `backend/` directory, no `Cargo.toml`, no Rust code, no database, no
`docker-compose.yml` wiring a backend service.** `nginx/nginx.conf` and
`scripts/local-dev-up.sh`/`local-dev-down.sh` exist but — check before trusting them —
likely only stand up the frontend/proxy, not an API, since none has been written yet.

`PLAN.md`, `requirements.md`, `SDLC_analysis.md`, and `user_stories+tests.md` describe a
large 19-user-story end state (6 card decks, i18n, RS256 JWT roles, `apalis` background
jobs, admin CRUD, `utoipa`/Swagger). **None of it is built.** Treat this whole app as
"frontend-only, backend at zero," not as a partially-built backend — there is nothing to
extend yet, only a plan to start from.

## Target Phase-1 API contract (source of truth: `../app01_react/backend/src/main/java/com/securevision/`)

```
POST /api/v1/auth/login        {username, password} -> {token, tokenType:"Bearer", role:"ADMIN"} | 401
GET  /api/v1/frameworks        -> Framework[]
GET  /api/v1/frameworks/:code  -> Framework | 404
GET  /api/v1/threats           ?frameworkCode&severity&stride&tag&q&page&size&sort -> Page<ThreatSummary>
GET  /api/v1/threats/:id       -> ThreatDetail | 404
GET  /health                   -> {"status":"UP"}
```
`Page<T> = {content, totalElements, totalPages, number, size}`. Error body on 4xx:
`{timestamp, status, error, message}`. `frontend/src/types/index.ts` is already written
against this shape — read it before designing Rust response DTOs so the two don't drift.
Before adding any field or endpoint, check app01's Java source; it's the contract of
record for anything Phase-1 claims to mirror, not this app's own `PLAN.md`.

## What PLAN.md commits to, once backend work starts

| Layer | Choice | Version |
|---|---|---|
| Web framework | `axum` (Tokio + Tower) | 0.8.x |
| DB access | `sqlx` (`query!`/`query_as!`, compile-time-checked against a live schema) | 0.8.x |
| Database | PostgreSQL | 16 |
| Auth | `jsonwebtoken`, **RS256** per PLAN.md | — |
| Password hashing | `argon2` (Argon2id) | — |
| Testing | `#[tokio::test]` + `proptest` + `axum-test`/`reqwest` | — |
| Lint/SCA | `clippy -D warnings`, `cargo-geiger`, `cargo audit`, `cargo-deny` | — |

**Likely deviation to watch for, before you build it:** app06's real backend found that
app01's actual `JwtService` uses HS256 (`Keys.hmacShaKeyFor`), not the RS256 PLAN.md's
D-04 assumes — app01 never had an RS256 key pair. If Phase-1 here is meant to mirror
app01 (per this app's own stated pattern), expect the same correction: HS256 with a
shared `JWT_SECRET`, not RS256, unless a deliberate decision to diverge from app01 is
made and recorded here. Don't implement RS256 against PLAN.md without checking app01's
actual `JwtService` first — same for `sqlx migrate` vs. app01's Flyway migrations, and
for `TEXT[]` vs. app01's comma-joined `TEXT` columns (`stride`/`tags`/`cve_references`) —
these are exactly the kind of "aspirational plan vs. what app01 actually does" gaps
`app06_HASKELL_react/CLAUDE.md` had to document after the fact.

## Before writing the first route

1. Read `app01_react/backend/src/main/java/com/securevision/` end to end for the real
   contract (entities, DTOs, `JwtService`, error-handling advice) — not PLAN.md's vision.
2. Read `frontend/src/types/index.ts` and `frontend/src/api/*` in this app to see what
   shape the already-built frontend expects on the wire.
3. Decide, and record here, whether Phase-1 follows app01 exactly (HS256, comma-joined
   text columns) or deliberately diverges (RS256, native `TEXT[]`) — don't let PLAN.md's
   aspirational D-04 silently become the implementation without that decision being made
   explicitly, the way app06 had to correct for.
