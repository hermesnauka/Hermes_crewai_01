# SharpGuard 2026 — C#/.NET implementation (app10_csharp_react)

One of twelve parallel course implementations of the same product (SecureVision — a
threat-modeling reference app). This is the C#/.NET one, comparing .NET 9 against
Java/Spring (`app01_react`, the reference), Go, Rust, Haskell, Scala, C++, and others.
React/TS frontend, matching the pattern of most siblings.

## Scope: backend has NOT been started

**Only `frontend/` exists.** It is fully built (`node_modules/`, `dist/` present,
Vite + React 18 + TS). **There is no `backend/` directory, no `.csproj`, no C# code at
all yet.** `PLAN.md`, `requirements.md`, `SDLC_analysis.md`, and
`user_stories+tests.md` describe a large 19-user-story aspirational end state (six
Cornucopia card decks, i18n, Hangfire jobs, admin CRUD) — none of that is built either.
Do not assume any backend behavior described in those docs exists; treat this whole
directory's backend as greenfield. When you do start it, mirror `app01_react`'s Java
source for behavior, not this app's own `PLAN.md` prose, wherever the two disagree.

## Target API contract (Phase-1 parity — source of truth: `../app01_react/backend/src/main/java/com/securevision/`)

```
POST /api/v1/auth/login        {username, password} -> {token, tokenType:"Bearer", role:"ADMIN"} | 401
GET  /api/v1/frameworks        -> Framework[]
GET  /api/v1/frameworks/:code  -> Framework | 404
GET  /api/v1/threats           ?frameworkCode&severity&stride&tag&q&page&size&sort -> Page<ThreatSummary>
GET  /api/v1/threats/:id       -> ThreatDetail | 404
GET  /health                   -> {"status":"UP"}
```
`Page<T> = {content, totalElements, totalPages, number, size}` — the frontend's
`frontend/src/types/` is presumably written against this shape; verify before changing
it. Error body on 4xx: `{timestamp, status, error, message}`.

## Planned backend stack (per `PLAN.md` §2 — not yet implemented)

- **ASP.NET Core 9 Minimal APIs** (not MVC controllers) — `Program.cs` + `Endpoints/`
  group classes, C# 13, `<Nullable>enable</Nullable>` project-wide.
- **EF Core 9 + Npgsql** against Postgres 16 for data access (LINQ-to-SQL), *not*
  Dapper. PLAN.md states this positioning precisely (D-02): EF Core always
  parameterizes and type-checks the C# expression at compile time, but — unlike Go's
  `sqlc`, Haskell's `hasql-th`, or Rust's `sqlx::query!` — it does **not** verify the
  query's shape against the live DB schema at compile time. Don't oversell EF Core's
  guarantee past that when writing docs or comments.
- **`YamlDotNet`** for the OWASP Cornucopia YAML deck ingestion — its default
  `Deserializer` already rejects unrecognized properties unless
  `.IgnoreUnmatchedProperties()` is explicitly called (D-08). This is a genuine
  series-wide comparison point: every other sibling had to opt into or hand-roll strict
  deserialization; here it's the out-of-the-box default. Don't call
  `.IgnoreUnmatchedProperties()` without a documented reason.
- Native AOT publish (`dotnet publish -p:PublishAot=true`) is the intended deployment
  target (D-06) — a static, JIT-free executable, comparable to the Go/Rust siblings'
  static binaries. This constrains reflection-heavy library choices from day one.
- JWT auth is planned as RS256 (`PLAN.md` D-nothing-explicit beyond §2) — but note
  `app06_HASKELL_react/CLAUDE.md` flags that app01's *actual* `JwtService` implementation
  is HS256 with a shared secret, not the RS256 its own PLAN.md describes. Check app01's
  real code, not its PLAN.md, before committing to RS256 here.

## Framework-native tooling this plan deliberately leans on (instead of third-party)

- **Rate limiting:** ASP.NET Core's built-in `Microsoft.AspNetCore.RateLimiting`
  middleware (fixed/sliding-window, token-bucket) — no Redis Lua script, no hand-rolled
  bucket, unlike most siblings (D-03).
- **Vulnerable-package scanning:** `dotnet list package --vulnerable`, built into the
  .NET CLI against the GitHub Advisory DB — no separate SCA tool needed for NuGet deps.
- **SAST:** Roslyn analyzers (built into `dotnet build`) + `SecurityCodeScan` +
  `SonarAnalyzer.CSharp`, all running inside the normal compiler pipeline alongside
  nullable-reference-type warnings — no separate CI step to wire up.
- **Exhaustiveness checking:** sealed `CardKind` record hierarchy + exhaustive `switch`
  expressions (D-04); `CS8509` is promoted from warning to build-breaking error via
  `.editorconfig` + `<WarningsAsErrors>`. This is opt-in configuration, not a language
  guarantee — verify it's still present on every `.csproj` before trusting it.

## Current frontend state

`frontend/` is built and has `dist/`, but was built against an aspirational/mocked API,
not a real backend — do not assume its `api/` client already matches the contract
above without checking `frontend/src/api/` and `frontend/src/types/` directly. `nginx/`
has a `nginx.conf` proxying `/api/v1/*`; `scripts/local-dev-up.sh` /
`local-dev-down.sh` exist for local orchestration but currently have no backend service
to start.
