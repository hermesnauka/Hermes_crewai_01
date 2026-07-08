# Frontend (Vite + React 18 + TypeScript + Tailwind)

Scope and API contract: see `../CLAUDE.md` first. This file is
command/layout reference for working in `frontend/` specifically.

```
npm run dev        # Vite dev server on :5173, proxies /api/v1 -> localhost:8080
npm run build       # tsc -b && vite build -- currently broken, see below
npm run test        # vitest
```

## Known pre-existing issues (not introduced by the Phase-1 parity work, not yet fixed)

- **`npm run build` / `tsc -b` fails** with `TS6310: Referenced project
  'tsconfig.node.json' may not disable emit.` — a project-references
  misconfiguration between `tsconfig.json` (`noEmit: true`, `references` to
  `tsconfig.node.json`) and `tsconfig.node.json` (`composite: true`). Plain
  `npx tsc --noEmit` (no `-b`) works fine and is what was used to verify
  changes in this pass. Fix the project-references setup before relying on
  `npm run build` for a real production bundle.
- **`npm run lint` / bare `eslint` fails**: `package.json` pins `eslint@^9`,
  which requires a flat `eslint.config.js` — none exists yet (only the
  now-unsupported legacy config style is implied by the eslint-plugin-*
  deps). Add one before relying on lint in CI.

## Pages

- `pages/Dashboard.tsx` — home; framework tiles + a quick-search box that
  now routes to `/threats?q=...` (previously routed to `/frameworks?q=...`,
  which never read that param — dead code, fixed as part of Phase-1 parity).
- `pages/Frameworks.tsx` — framework grid, no filtering.
- `pages/FrameworkDetail.tsx` — one framework's threats, paginated (20/page).
- `pages/Threats.tsx` — cross-framework threat browser: text search,
  severity filter, pagination. This is the only page that exercises
  `api.getThreats`'s `q`/`severity` params.

`api/client.ts` and `types/index.ts` are written directly against the
backend's JSON contract (see `../CLAUDE.md`) — if you change either side,
change both.
