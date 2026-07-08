# HaskShield 2026 — Haskell/servant implementation (app06_HASKELL_react)

One of six parallel course implementations of the same product (Java×2, Python/Django,
Scala/ZIO, Go, **Haskell**). This is the Haskell one: `servant` + `hasql` backend,
React/TS frontend. See `backend/CLAUDE.md` and `frontend/CLAUDE.md` for stack-specific
detail.

## Scope: this is Phase-1 parity, not the full vision

`PLAN.md`, `requirements.md`, `SDLC_analysis.md`, and `user_stories+tests.md` in this
directory describe a much larger 19-user-story end state (6 threat-card decks, i18n,
JWT roles, async export jobs, admin CRUD, cross-framework matrix). **None of that is
built yet.** What exists today deliberately mirrors `../app01_react`'s backend, which is
itself only a Phase-1 skeleton: frameworks + threats (read-only), one hardcoded admin
login, 4 seeded frameworks, ~33 threats. Read `PLAN.md` for the destination, but don't
assume anything past the API contract below is implemented.

## API contract (source of truth: `../app01_react/backend/src/main/java/com/securevision/`)

```
POST /api/v1/auth/login        {username, password} -> {token, tokenType:"Bearer", role:"ADMIN"} | 401
GET  /api/v1/frameworks        -> Framework[]
GET  /api/v1/frameworks/:code  -> Framework | 404
GET  /api/v1/threats           ?frameworkCode&severity&stride&tag&q&page&size&sort -> Page<ThreatSummary>
GET  /api/v1/threats/:id       -> ThreatDetail | 404
GET  /health                   -> {"status":"UP"}
```
`Page<T> = {content, totalElements, totalPages, number, size}` (Spring Data's envelope —
the frontend's `types/index.ts` is written against this shape; don't change it without
updating the frontend). Error body on 4xx: `{timestamp, status, error, message}`.

If you're adding a field or endpoint, check `../app01_react`'s Java source first — it's
the contract of record for anything Phase-1 claims to mirror.

## Known deliberate deviations from app01 (don't "fix" these back)

- **`stride`/`tags`/`cve_references` are native Postgres `TEXT[]`**, not app01's
  comma-joined `TEXT` column — same JSON shape (`["S","T"]`), just a better-typed
  column on this side. app01's own code comments flag the comma-join as a shortcut.
  `cve_references`/`tags` serialize as `[]` when empty, never `null` (app01 sometimes
  emits `null` here since its DTO has no null-guard for that field).
- **Auth is JWT HS256 with a shared `JWT_SECRET`**, matching what app01's `JwtService`
  *actually* does (`Keys.hmacShaKeyFor`) — not the RS256 described in `PLAN.md`'s
  aspirational D-04, which assumes a key pair app01 never had.
- **Migrations run via a small custom runner**, not `hasql-th`/Flyway. `hasql-th`
  requires a live, already-migrated database to even type-check at compile time — a
  chicken-and-egg problem for a fresh checkout — so Phase-1 uses plain `hasql` with
  hand-written `Encoders`/`Decoders` instead. Revisit `hasql-th` once there's a CI step
  that can migrate a throwaway DB before `cabal build` runs.
- **No Redis, rate limiting, `odd-jobs`, or `servant-swagger`** — none of these exist in
  app01 either. Explicitly deferred to keep the first Haskell build low-risk, not
  silently dropped from the vision.

## Running the stack locally

No Docker on this machine. Use `scripts/local-dev-up.sh` / `scripts/local-dev-down.sh`
(portable installs under `C:\Users\krish\tools\` + `C:\ghcup\`, not containers — see
comments in those scripts). Postgres/Redis/GHCup paths are specific to this machine.

## Environment-specific gotcha (worth knowing before you fight it again)

This machine's Norton Antivirus does TLS interception (root cert:
`C:\Users\krish\tools\norton-root.cer` / `.pem`). MSYS2's `pacman`/`curl` (bundled with
GHCup) ships its own CA bundle and doesn't trust it by default, so any `pacman`-driven
install fails with `SSL certificate problem: unable to get local issuer certificate`
until that root is appended to `C:\ghcup\msys64\usr\ssl\certs\ca-bundle*.crt` (already
done as of this writing). `cabal`/GHC itself uses the Windows certificate store and
isn't affected. If MSYS2 is ever reinstalled, redo the cert append.
