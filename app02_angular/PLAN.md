# ThreatView 2026 — Application Development Plan

**Version:** 1.0  
**Date:** 2026-07-07  
**Status:** Living document — updated after each sprint planning session

---

## 1. Project Overview

**Name:** ThreatView 2026  
**Purpose:** An interactive security reference and learning platform that maps security threats, vulnerabilities, and mitigations across six major frameworks — OWASP Web Top 10 (2021), OWASP LLM Top 10 (2025), OWASP API Security Top 10, OWASP Agentic AI Top 10 (2026), MITRE ATLAS (AI/ML adversarial techniques), and CompTIA Security+ SY0-701 / SecAI+ — and presents each threat with working sample code countermeasures in five languages: Python, Java (Spring Boot), Go, Scala, and Lua.

**Cornucopia extension:** The platform additionally covers the full OWASP Cornucopia card catalogue — Website App Edition v3.0, Companion Edition v1.0 (LLM, AAI, FRE, DVO, BOT, CLD suits), Mobile App Edition v1.1, Microsoft STRIDE Elevation of Privilege v5.0, and Elevation of MLSec v1.0 — giving practitioners an interactive, card-based threat modeling reference.

**UI languages:** Polish (default) and English — switch is persisted in `localStorage` under key `tv_locale`.

**Key differentiator from SecureVision (app01):** ThreatView is built with **Angular 18 standalone components** and **Angular Material 18 (MDC-based)**, offering a Material Design UI, Angular-native state management with Signals and NgRx, and Angular's built-in security primitives (DomSanitizer, strict mode).

---

## 2. Technology Stack

### Backend
| Layer | Technology | Version |
|---|---|---|
| Runtime | Java | 21 LTS |
| Framework | Spring Boot | 3.3.x |
| API style | REST (JSON) | — |
| Security | Spring Security 6 | JWT / OAuth2 |
| Persistence | PostgreSQL 16 | Spring Data JPA |
| Cache | Redis 7 | Spring Cache |
| Build | Maven 3.9 | — |
| Docs | SpringDoc OpenAPI 3 | Swagger UI |
| DB migrations | Flyway | — |
| Testing | JUnit 5, Mockito, Testcontainers, RestAssured | — |
| Rate limiting | Bucket4j | — |
| Input sanitization | OWASP Java HTML Sanitizer | — |
| Integrity checks | Java MessageDigest (SHA-256) | — |

### Frontend
| Layer | Technology | Version |
|---|---|---|
| Framework | Angular | 18.x (standalone components) |
| UI Library | Angular Material | 18.x (MDC-based) |
| Language | TypeScript | 5.x (strict mode) |
| Build | Angular CLI | 18.x |
| State | Angular Signals + NgRx Signals | — |
| Router | Angular Router | 17+ (typed routes) |
| i18n | ngx-translate | 15.x |
| Charts | ngx-echarts (Apache ECharts) | — |
| SVG / Diagrams | D3.js v7 | — |
| Syntax highlight | Prism.js | (lazy-loaded per language) |
| HTTP | Angular HttpClient | — |
| Forms | Angular Reactive Forms | — |
| Testing unit | Jest 29 + Angular Testing Library | — |
| Testing E2E | Cypress 13 | — |
| Validation | class-validator + class-transformer | — |
| MSW | Mock Service Worker 2 | — |

### Infrastructure
| Component | Technology |
|---|---|
| Reverse proxy | Nginx |
| Container | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Monitoring | Grafana + Loki + Prometheus |
| Secrets | Docker Secrets / GitHub Secrets |
| SAST | SpotBugs + FindSecBugs + eslint-plugin-security |
| DAST | OWASP ZAP |
| SCA | OWASP Dependency Check + npm audit + Trivy |

---

## 3. High-Level Architecture

```
Browser (Angular SPA)
        │
        │  HTTPS
        ▼
  Nginx (port 443)
   ├── /api/v1/*  ─────► Spring Boot (port 8080)
   │                       ├── FrameworkController
   │                       ├── ThreatController          ← threats + Cornucopia cards
   │                       ├── CardSuitController        ← suit browsers (FRE/LLM/AAI…)
   │                       ├── MitigationController
   │                       ├── CodeSampleController
   │                       ├── MatrixController          ← cross-framework matrices
   │                       ├── SearchController
   │                       ├── ExportController          ← CSV/PDF
   │                       └── AdminController           ← JWT-gated CRUD
   │
   └── /*         ─────► Angular SPA bundle (ng build)
                           ├── AppShellComponent (mat-sidenav layout)
                           ├── Features (lazy-loaded modules)
                           │   ├── DashboardComponent
                           │   ├── FrameworkListComponent / FrameworkDetailComponent
                           │   ├── ThreatBrowserComponent / ThreatDetailComponent
                           │   ├── CardSuit feature components (7 pages)
                           │   ├── Matrix feature components (4 pages)
                           │   ├── StrideHeatmapComponent
                           │   ├── SearchResultsComponent
                           │   └── AboutComponent
                           └── Shared Components
                               ├── ThreatCardComponent
                               ├── CornucopiaCardComponent
                               ├── CodeSamplePanelComponent (mat-tab-group)
                               ├── BotWarningDialogComponent (MatDialog)
                               ├── StrideHeatmapComponent
                               ├── MatrixTableComponent
                               └── LanguageToggleComponent (mat-button-toggle-group)

PostgreSQL 16  ◄── Spring Data JPA
Redis 7        ◄── Spring Cache
```

---

## 4. Data Model

### 4.1 Framework
```
Framework {
  id:           UUID
  code:         String   // "OWASP_WEB", "OWASP_LLM", "OWASP_API", "OWASP_AGENTIC",
                         //  "MITRE_ATLAS", "COMPTIA_SY0701", "COMPTIA_SECAI",
                         //  "CORNUCOPIA_WEBAPP", "CORNUCOPIA_COMPANION",
                         //  "CORNUCOPIA_MOBILE", "STRIDE_EOP", "MLSEC"
  name:         String
  version:      String
  description:  String
  referenceUrl: String
}
```

### 4.2 Threat
```
Threat {
  id:            UUID
  frameworkId:   UUID
  code:          String   // "LLM01:2025", "A01:2021", "AML.T0051"
  title:         String
  severity:      Enum     // CRITICAL, HIGH, MEDIUM, LOW, INFO
  category:      String
  description:   String
  attackVector:  String
  attackSurface: String
  stride:        Set<Enum> // S, T, R, I, D, E
  cveReferences: List<String>
  tags:          List<String>
}
```

### 4.3 ThreatTranslation
```
ThreatTranslation {
  id:           UUID
  threatId:     UUID
  locale:       String   // "pl", "en"
  title:        String
  description:  String
  attackVector: String
  category:     String
}
```

### 4.4 CornucopiaCard
```
CornucopiaCard {
  id:            UUID
  cardId:        String   // "FRE4", "LLMX", "EPK", "EDRK", "NSX"
  suitCode:      String   // "FRE", "LLM", "AAI", "DVO", "BOT", "CLD",
                          //  "VE", "AT", "SM", "AZ", "CR",
                          //  "PC", "AA", "NS", "RS", "CRM",
                          //  "SP", "TA", "RE", "ID", "DS", "EP",
                          //  "EMR", "EIR", "EOR", "EDR"
  suitName:      String
  edition:       String   // "companion", "webapp", "mobileapp", "eop", "mlsec"
  value:         String   // "2"–"10", "J", "Q", "K", "A"
  isCritical:    Boolean  // true for J, Q, K
  descriptionEn: String
  descriptionPl: String
  owaspRefs:     List<String>
  mitreRefs:     List<String>
  mavsRefs:      List<String>
  cicdSecRefs:   List<String>
  oatRefs:       List<String>
  agentAiRefs:   List<String>
  contentHash:   String   // SHA-256 of descriptionEn
}
```

### 4.5 Mitigation
```
Mitigation {
  id:                   UUID
  threatId:             UUID
  title:                String
  description:          String
  mitigationType:       Enum  // PREVENTIVE, DETECTIVE, CORRECTIVE, COMPENSATING
  implementationEffort: Enum  // LOW, MEDIUM, HIGH
  effectiveness:        Enum  // PARTIAL, SIGNIFICANT, FULL
}
```

### 4.6 CodeSample
```
CodeSample {
  id:            UUID
  mitigationId:  UUID
  language:      Enum    // PYTHON, JAVA, GO, SCALA, LUA
  sampleType:    Enum    // ATTACK_DEMO, DEFENSE
  title:         String
  description:   String
  codeSnippet:   String
  frameworkHint: String  // "Spring Boot 3.3", "FastAPI 0.110", "Gin 1.9", "Akka HTTP", "OpenResty"
  version:       String
}
```

### 4.7 CrossReference
```
CrossReference {
  id:               UUID
  sourceThreatId:   UUID
  targetThreatId:   UUID
  relationshipType: Enum  // EQUIVALENT, RELATED, PARENT_CHILD, MAPS_TO
  description:      String
}
```

### 4.8 ContentHash
```
ContentHash {
  id:         UUID
  fileName:   String
  sha256Hash: String
  verifiedAt: Instant
  isValid:    Boolean
}
```

---

## 5. Development Phases

### Phase 1 — Foundation (Sprints 1–2, Weeks 1–4)
Pokrycie: US-01, US-02, US-03

- [ ] Scaffold Angular 18 SPA: `ng new threatview --standalone --routing --style=scss`
- [ ] Dodaj Angular Material 18: `ng add @angular/material`
- [ ] Scaffold Spring Boot 3.3 backend: Maven, Java 21, Spring Web, Security, Data JPA
- [ ] Docker Compose: PostgreSQL 16 + Redis 7 + backend + frontend + Nginx
- [ ] Schematy bazy danych + migracje Flyway V1–V8 (entities 4.1–4.7)
- [ ] Seedowanie danych: OWASP Web Top 10, LLM Top 10, MITRE ATLAS, CompTIA SecAI+
- [ ] REST API: `GET /api/v1/frameworks`, `GET /api/v1/threats` z paginacją
- [ ] Spring Security: JWT auth (rola ADMIN do CRUD)
- [ ] SpringDoc OpenAPI 3 pod `/swagger-ui.html`
- [ ] Angular: `AppShellComponent` z `mat-sidenav` + `mat-toolbar` + `LanguageToggleComponent`
- [ ] Angular: `DashboardComponent` — statystyki frameworków, szybkie wyszukiwanie

### Phase 2 — Core API + Angular Threat Browser (Sprints 3–4, Weeks 5–8)
Pokrycie: US-02, US-03, US-04, US-05

- [ ] `GET /api/v1/threats` z filtrami: framework, severity, STRIDE, category, tag, q
- [ ] `GET /api/v1/threats/{id}` ze zagnieżdżonymi mitigacjami i próbkami kodu
- [ ] `GET /api/v1/cross-references` — tabela mapowania między frameworkami
- [ ] Angular: `ThreatBrowserComponent` z `mat-table` + `mat-paginator` + panel filtrów (`mat-select`, `mat-chip-listbox`)
- [ ] Angular: `ThreatDetailComponent` z `mat-tab-group`: Przegląd | Mitigacje | Kod | Powiązania
- [ ] Angular: `ThreatCardComponent` — mat-card z STRIDE badge chips
- [ ] Angular: `MatrixComponent` — tabela mapowania OWASP ↔ MITRE ATLAS ↔ CompTIA
- [ ] Angular: `FrameworkListComponent` + `FrameworkDetailComponent`
- [ ] STRIDE badge (mat-chip) + legenda wizualna (mat-tooltip)

### Phase 3 — Code Samples + MITRE ATLAS Timeline (Sprints 5–6, Weeks 9–12)
Pokrycie: US-04, US-08, US-09, US-10

- [ ] `CodeSamplePanelComponent` — mat-tab-group z zakładkami Python/Java/Go/Scala/Lua
- [ ] Prism.js lazy load per język (LazyPrismDirective)
- [ ] Attack Demo tab: mat-card z czerwoną obwódką + PODATNY badge + BotWarningDialogComponent
- [ ] `BotWarningDialogComponent` — MatDialog "Rozumiem ryzyko" przy kopiowaniu attack demo
- [ ] MITRE ATLAS Kill-Chain timeline (ngx-echarts horizontal bar)
- [ ] `CoverageComponent` — heatmapa pokrycia STRIDE per framework (ECharts heatmap)
- [ ] Tag cloud — mat-chip-set do przeglądania po kategorii

### Phase 4 — Advanced Features (Sprints 6–7, Weeks 11–14)
Pokrycie: US-05, US-06, US-07, US-08

- [ ] Wyszukiwanie pełnotekstowe: `tsvector` PostgreSQL + `GET /api/v1/search?q=`
- [ ] `SearchResultsComponent` — wyniki z podświetlonymi fragmentami (Angular pipe)
- [ ] Global search w mat-toolbar (`SearchBarComponent`)
- [ ] Export do CSV / PDF: `GET /api/v1/export?format=csv&frameworkCode=LLM`
- [ ] Dark mode toggle — Angular Material theme switch (signal-based)
- [ ] Zakładki / ulubione — localStorage service
- [ ] `CrossReferenceComponent` — tabela mapowania między frameworkami

### Phase 5 — i18n Polish ↔ English (Sprint 8, Weeks 15–16)
Pokrycie: US-11

- [ ] `ThreatTranslation` entity + migracja Flyway
- [ ] `Accept-Language` header — `LocaleInterceptor` w Angular HttpClient
- [ ] `ngx-translate` z plikami `assets/i18n/pl.json` i `en.json`
- [ ] `LanguageToggleComponent` — `mat-button-toggle-group` w mat-toolbar
- [ ] Zapis wyboru w localStorage (klucz `tv_locale`)
- [ ] Próbki kodu NIGDY nie tłumaczone
- [ ] Build-time walidacja parzystości kluczy i18n (`i18n-keys-parity.spec.ts`)

### Phase 6 — Cornucopia: FRE + LLM + AAI (Sprint 9, Weeks 17–18)
Pokrycie: US-12, US-13, US-14

- [ ] `CornucopiaCard` entity + migracja Flyway V9
- [ ] `YamlCardLoader` — ładowanie kart z `data/cornucopia/*.yaml` przy starcie
- [ ] `ContentIntegrityVerifier` (@PostConstruct) — weryfikacja SHA-256 plików YAML
- [ ] `OwaspRefValidator` — allowlist identyfikatorów OWASP
- [ ] `CardSuitController` — `GET /api/v1/threats?suit=FRE` itp.
- [ ] Angular: `FrontendSecurityComponent` — przeglądarka kart FRE (US-12)
- [ ] Angular: `LlmSecurityComponent` + `LlmMatrixComponent` (US-13)
- [ ] Angular: `AgenticAiComponent` + `AgenticMatrixComponent` (US-14)
- [ ] `CornucopiaCardComponent` — mat-card z suit badge, value badge, OWASP ref chips
- [ ] Angular DomSanitizer — bezpieczne renderowanie descriptionPl/En
- [ ] `AUTONOMY RISK` mat-chip na kartach AAI (isCritical === true)
- [ ] Bucket4j rate limit 60 req/min per IP na wszystkich `/api/v1/threats?suit=*`

### Phase 7 — Cornucopia: STRIDE EoP + MLSec (Sprints 10–11, Weeks 19–22)
Pokrycie: US-15, US-16

- [ ] `MitreAtlasRefValidator` — allowlist technik ATLAS
- [ ] `GET /api/v1/threats/stride/categories`, `GET /api/v1/stride-heatmap` (JWT)
- [ ] `GET /api/v1/threats/mlsec/categories`, filtry po MITRE ATLAS
- [ ] Angular: `StrideCatalogueComponent` — 6 suit STRIDE × 13 kart, mat-expansion-panel per suit
- [ ] Angular: `StrideHeatmapComponent` — interaktywna heatmapa ECharts (AuthGuard)
- [ ] Diagramy STRIDE renderowane server-side jako SVG (D3.js safe rendering)
- [ ] Angular: `MlSecurityComponent` — 4 kategorie MLSec z MITRE ATLAS ref chips
- [ ] `ML-SPECIFIC` mat-chip na kartach EMR/EIR/EOR/EDR
- [ ] X-Frame-Options: DENY na `/stride-heatmap` (Spring Security header)

### Phase 8 — Cornucopia: Mobile + DevOps (Sprints 12–13, Weeks 23–26)
Pokrycie: US-17, US-18

- [ ] `MavsRefValidator`, `CicdSecRefValidator`, `OatRefValidator`
- [ ] API: mobile suits (PC, AA, NS, RS, CRM, CM) + `/api/v1/matrix/mobile-vs-web`
- [ ] Angular: `MobileSecurityComponent` + `MobileVsWebMatrixComponent` (US-17)
- [ ] API: DVO (DevOps) + BOT (Automated Threats) suits
- [ ] Angular: `DevOpsSecurityComponent` — sekcje DVO i BOT (US-18)
- [ ] `BotWarningDialogComponent` ulepszone — dialog "Rozumiem ryzyko" dla kart BOT credential enumeration
- [ ] CI job `yaml-content-integrity`: schema validation + injection scan + hash update

### Phase 9 — Integration, Testing & Hardening (Sprints 14–16, Weeks 27–31)
Pokrycie: integracja US-01–US-18

- [ ] Testy jednostkowe: JUnit 5 + Mockito (≥ 80% pokrycia), Jest + ATL dla Angular components
- [ ] Testy integracyjne: Testcontainers + PostgreSQL dla wszystkich endpointów REST
- [ ] Testy E2E: Cypress 13 — 18 user stories × 1+ scenariusz
- [ ] Abuse cases AC-01–AC-13 — wszystkie GREEN w CI
- [ ] DAST: OWASP ZAP full active scan
- [ ] Audit dostępności: axe-core (Angular CDK a11y) — WCAG 2.1 AA
- [ ] Wydajność: Lighthouse ≥ 85 mobile, API < 200 ms p95
- [ ] Produkcyjny Docker build + Nginx config
- [ ] ng build --configuration production: inicjalny bundle < 600 KB gzip
- [ ] Monitoring: Loki alert SEC-007 + SEC-008, metryka `content_integrity_check_ok`

---

## 6. API Endpoint Map

### Framework & Threat (bazowe)
```
GET  /api/v1/frameworks                         — lista wszystkich frameworków
GET  /api/v1/frameworks/{code}                  — szczegóły frameworku + lista zagrożeń

GET  /api/v1/threats                            — lista zagrożeń
                                                  filtry: frameworkCode, severity, stride,
                                                          tag, q, suit, owaspRef, mitreRef
GET  /api/v1/threats/{id}                       — pojedyncze zagrożenie z mitigacjami
GET  /api/v1/threats/{id}/mitigations           — mitigacje dla zagrożenia
GET  /api/v1/threats/{id}/code-samples          — próbki kodu (wszystkie języki)
```

### Cornucopia Card Suits (US-12–US-18)
```
GET  /api/v1/threats?suit=FRE                   — karty Frontend (US-12)
GET  /api/v1/threats?suit=LLM                   — karty LLM (US-13)
GET  /api/v1/threats?suit=AAI                   — karty Agentic AI (US-14)
GET  /api/v1/threats/stride/categories          — 6 kategorii STRIDE (US-15)
GET  /api/v1/threats?suit=SP                    — karty Spoofing (US-15, przykład)
GET  /api/v1/threats?suit=EMR                   — karty Model Risk (US-16)
GET  /api/v1/threats/mlsec/categories           — 4 kategorie MLSec (US-16)
GET  /api/v1/threats?suit=NS                    — karty Network & Storage (US-17)
GET  /api/v1/threats/mobile/suits               — 6 talii Mobile (US-17)
GET  /api/v1/threats?suit=DVO                   — karty DevOps (US-18)
GET  /api/v1/threats?suit=BOT                   — karty Automated Threats (US-18)
```

### Matrix & Visualization
```
GET  /api/v1/matrix/llm                         — macierz LLM Top 10 × karty Cornucopia
GET  /api/v1/matrix/agentic                     — macierz Agentic AI × LLM porównanie
GET  /api/v1/matrix/mobile-vs-web               — porównanie MASVS vs OWASP Web Top 10
GET  /api/v1/stride-heatmap                     — heatmapa STRIDE per komponent [JWT]
GET  /api/v1/cross-references                   — tabela mapowania między frameworkami
GET  /api/v1/cross-references?sourceCode=LLM01
GET  /api/v1/stats/coverage                     — dane JSON dla heatmapy pokrycia
```

### Search & Export
```
GET  /api/v1/search?q=prompt+injection
GET  /api/v1/export?format=csv&frameworkCode=LLM
GET  /api/v1/export?format=pdf&frameworkCode=LLM
```

### Mitigations & Code Samples
```
GET  /api/v1/mitigations/{id}
GET  /api/v1/mitigations/{id}/code-samples
GET  /api/v1/code-samples?language=JAVA
```

### Admin CRUD (JWT — rola ADMIN)
```
POST   /api/v1/admin/threats
PUT    /api/v1/admin/threats/{id}               — sanityzacja OWASP Java HTML Sanitizer
DELETE /api/v1/admin/threats/{id}
POST   /api/v1/admin/code-samples
PUT    /api/v1/admin/code-samples/{id}
```

### Health & Ops
```
GET  /api/v1/actuator/health
GET  /api/v1/actuator/metrics/content.integrity
```

---

## 7. Angular Component & Route Structure

```
AppShellComponent  (mat-sidenav-container)
│
├── mat-sidenav  (navigation)
│   ├── mat-nav-list: Dashboard, Frameworks, Threats, Matrix, Search, About
│   └── LanguageToggleComponent (mat-button-toggle-group PL | EN)
│
├── mat-toolbar  (top bar)
│   ├── SearchBarComponent (mat-form-field + mat-autocomplete)
│   ├── DarkModeToggleComponent (mat-slide-toggle)
│   └── LanguageToggleComponent
│
└── <router-outlet>

Routes (lazy-loaded):
  /                               → DashboardComponent
  /frameworks                     → FrameworkListComponent        (mat-grid-list kart)
  /frameworks/:code               → FrameworkDetailComponent      (mat-expansion-panel)
  /frameworks/frontend-security   → FrontendSecurityComponent     (US-12)
  /frameworks/llm-security        → LlmSecurityComponent          (US-13)
  /frameworks/agentic-ai          → AgenticAiComponent            (US-14)
  /frameworks/stride              → StrideCatalogueComponent      (US-15, mat-accordion)
  /frameworks/ml-security         → MlSecurityComponent           (US-16)
  /frameworks/mobile-security     → MobileSecurityComponent       (US-17)
  /frameworks/devops-security     → DevOpsSecurityComponent       (US-18)
  /threats                        → ThreatBrowserComponent        (mat-table + mat-paginator)
  /threats/:id                    → ThreatDetailComponent         (mat-tab-group 4 tabs)
  /matrix                         → MatrixComponent
  /matrix/llm                     → LlmMatrixComponent            (US-13)
  /matrix/agentic                 → AgenticMatrixComponent        (US-14)
  /matrix/mobile-vs-web           → MobileVsWebMatrixComponent    (US-17)
  /stride-heatmap                 → StrideHeatmapComponent        (AuthGuard, US-15)
  /coverage                       → CoverageComponent             (ECharts heatmap)
  /search                         → SearchResultsComponent        (US-06)
  /about                          → AboutComponent

Shared Components (src/app/shared/components/):
  ThreatCardComponent             — mat-card, severity color bar, STRIDE chips
  CornucopiaCardComponent         — mat-card, suit badge, value circle, OWASP ref chips
  CodeSamplePanelComponent        — mat-tab-group × 5 languages, Prism.js highlight
  BotWarningDialogComponent       — MatDialog confirmation przed skopiowaniem kodu ataku
  StrideHeatmapComponent          — ngx-echarts heatmap (reused in /coverage)
  MatrixTableComponent            — mat-table z sticky columns dla macierzy
  LlmMatrixComponent              — specjalizowana macierz LLM Top 10 × Cornucopia
  LanguageToggleComponent         — mat-button-toggle-group PL | EN
  SeverityBadgeComponent          — mat-chip colored by severity
  OwaspRefChipListComponent       — mat-chip-set z linkami do OWASP

Angular Services (src/app/core/services/):
  FrameworkService                — HttpClient calls to /api/v1/frameworks
  ThreatService                   — HttpClient calls to /api/v1/threats
  CardSuitService                 — HttpClient calls to /api/v1/threats?suit=*
  MatrixService                   — HttpClient calls to /api/v1/matrix/*
  SearchService                   — HttpClient calls to /api/v1/search
  ExportService                   — HttpClient calls to /api/v1/export
  LocaleService                   — ngx-translate + localStorage tv_locale
  AuthService                     — JWT management, login, refresh

Guards (src/app/core/guards/):
  AuthGuard                       — protects /stride-heatmap and /admin/**
  AdminGuard                      — protects /admin/**

Interceptors (src/app/core/interceptors/):
  AuthInterceptor                 — adds Authorization: Bearer <token>
  LocaleInterceptor               — adds Accept-Language: pl/en

NgRx Signals Store (src/app/store/):
  frameworksStore                 — frameworks signal slice
  threatsStore                    — threats, filters, pagination signal slice
  cardSuitsStore                  — cornucopia card suits signal slice
  searchStore                     — search query + results signal slice
  uiStore                         — darkMode, locale signals
```

---

## 8. Code Sample Strategy

Każde zagrożenie ma co najmniej jedną mitigację z 5 próbkami kodu (jedna na język). Karty Cornucopia mają co najmniej jedną próbkę pokazującą bezpieczny wzorzec.

```
CodeSamplePanelComponent — mat-tab-group:
  [Python]  [Java]  [Go]  [Scala]  [Lua]

Każda zakładka:
  mat-tab-group wewnętrzny:
    [Attack Demo]  — mat-card z border-red-600, badge PODATNY
    [Defense]      — mat-card z border-green-600, badge BEZPIECZNY
```

| Język | Główny framework |
|---|---|
| Python | FastAPI 0.110, SQLAlchemy 2.0, Pydantic v2 |
| Java | Spring Boot 3.3, Spring Security 6, Spring Data JPA |
| Go | Gin 1.9, pgx v5, net/http |
| Scala | Akka HTTP 10.5, Slick 3.5, ZIO 2 |
| Lua | OpenResty / NGINX Lua, LuaSQL |

---

## 9. Security Data Coverage Plan

### OWASP Web Top 10 (2021)
A01–A10 — wszystkie 10 zagrożeń  
Cornucopia: talie `AZ`, `AT`, `VE`, `CR`, `SM`, `C` (webapp-cards-3.0)

### OWASP LLM Top 10 (2025)
LLM01–LLM10 — wszystkie 10 zagrożeń  
Cornucopia: talia `LLM` (companion-cards-1.0)  
Macierz: `/matrix/llm`

### OWASP Agentic AI Top 10 (2026)
AgentAI01–AgentAI10 — wszystkie 10 zagrożeń  
Cornucopia: talia `AAI` (companion-cards-1.0)  
Macierz: `/matrix/agentic`

### OWASP API Security Top 10
API1–API10 — wszystkie 10 zagrożeń

### OWASP Top 10 Client-Side Security Risks
C01–C10 — wszystkie 10 zagrożeń  
Cornucopia: talia `FRE` (companion-cards-1.0)

### OWASP Top 10 CI/CD Security Risks
CICD-SEC-01–10 — wszystkie 10  
Cornucopia: talia `DVO`

### OWASP Automated Threats (OAT)
OAT-001–OAT-021 — minimum 13  
Cornucopia: talia `BOT`

### OWASP MASVS 2.0
MASVS-STORAGE, MASVS-CRYPTO, MASVS-AUTH, MASVS-NETWORK, MASVS-PLATFORM, MASVS-CODE, MASVS-RESILIENCE  
Cornucopia: talie `PC, AA, NS, RS, CRM, CM` (mobileapp-cards-1.1)

### STRIDE (Threat Modeling)
6 kategorii: Spoofing, Tampering, Repudiation, Information Disclosure, DoS, EoP  
Cornucopia: talie `SP, TA, RE, ID, DS, EP` (stride-eop-cards-5.0)  
Heatmapa: `/stride-heatmap`

### MITRE ATLAS
Minimum 15 technik: T0010, T0011, T0014, T0020, T0024, T0029, T0043, T0044, T0051, T0046  
Cornucopia: talie `EMR, EIR, EOR, EDR` (mlsec-cards-1.0)

### CompTIA Security+ SY0-701 / SecAI+
Minimum 20 tematów: Prompt Injection, Data Poisoning, Model Theft, Adversarial ML, Deepfakes, AI Red Teaming, Zero Trust, NIST AI RMF, NIS2/UKSC, AI-BOM

---

## 10. Cornucopia Content Pipeline

Sześć plików YAML kart (`data/cornucopia/*.yaml`) traktowane jako aktywa bezpieczeństwa — niemutowalne po załadowaniu, z weryfikacją SHA-256 przy każdym starcie.

```
data/cornucopia/
├── webapp-cards-3.0-en.yaml
├── companion-llm-cards-1.0-en.yaml
├── mobileapp-cards-1.1-en.yaml
├── stride-eop-cards-5.0-en.yaml
├── mlsec-cards-1.0-en.yaml
└── translations/
    ├── pl.cards.json
    └── en.cards.json

data/hashes.json                       ← SHA-256 każdego pliku YAML
data/mitre-atlas-allowlist.json
data/ref-allowlists.json
```

**Workflow:** PR → CI schema+injection scan+hash-generator → merge → `ContentIntegrityVerifier` @PostConstruct → Loki monitoring

---

## 11. Risk Register

| Ryzyko | Mitigacja |
|---|---|
| Angular bundle zbyt duży | Lazy loading per feature, Prism.js leniwe ładowanie, OnPush change detection |
| Cross-references niespójne | Encja `CrossReference` z enum typem relacji |
| Próbki kodu nieaktualne | Pole `version` na `CodeSample`; admin UI |
| Wyszukiwanie wolne | `tsvector` indeks PostgreSQL |
| YAML zmodyfikowany złośliwie | `ContentIntegrityVerifier` SHA-256 + CODEOWNERS |
| Fałszywe OWASP/MITRE ID | `*RefValidator` server-side allowlisty |
| XSS przez admin update | OWASP Java HTML Sanitizer + Angular DomSanitizer |
| Scraping bazy kart | Bucket4j 60 req/min per IP |
| Clickjacking heatmapy | X-Frame-Options: DENY + CSP frame-ancestors 'none' |
| SVG injection w diagramach | Server-side SVG rendering + CSP blok `<script>` |
| Angular strict mode naruszenia | `strict: true` w tsconfig.json, CI tsc --noEmit |

---

## 12. Directory Layout

```
app02_angular/
├── PLAN.md
├── requirements.md
├── user_stories+tests.md
├── SDLC_analysis.md
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/threatview/
│       │   ├── ThreatViewApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── RateLimitConfig.java
│       │   ├── controller/
│       │   │   ├── FrameworkController.java
│       │   │   ├── ThreatController.java
│       │   │   ├── CardSuitController.java
│       │   │   ├── MatrixController.java
│       │   │   ├── SearchController.java
│       │   │   ├── ExportController.java
│       │   │   └── AdminController.java
│       │   ├── service/
│       │   │   ├── ThreatService.java
│       │   │   ├── FrontendThreatService.java
│       │   │   ├── LlmThreatService.java
│       │   │   ├── AgenticThreatService.java
│       │   │   ├── StrideThreatService.java
│       │   │   ├── MlSecThreatService.java
│       │   │   ├── MobileSecThreatService.java
│       │   │   ├── DevOpsThreatService.java
│       │   │   └── LocalizationService.java
│       │   ├── integrity/
│       │   │   ├── ContentIntegrityVerifier.java
│       │   │   ├── YamlCardLoader.java
│       │   │   └── validator/
│       │   │       ├── OwaspRefValidator.java
│       │   │       ├── MitreAtlasRefValidator.java
│       │   │       ├── MavsRefValidator.java
│       │   │       ├── CicdSecRefValidator.java
│       │   │       └── OatRefValidator.java
│       │   ├── repository/
│       │   ├── entity/
│       │   │   ├── Framework.java
│       │   │   ├── Threat.java
│       │   │   ├── ThreatTranslation.java
│       │   │   ├── CornucopiaCard.java
│       │   │   ├── Mitigation.java
│       │   │   ├── CodeSample.java
│       │   │   ├── CrossReference.java
│       │   │   └── ContentHash.java
│       │   ├── dto/
│       │   └── security/
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/
│       └── test/
│
├── frontend/
│   ├── angular.json
│   ├── package.json
│   ├── tsconfig.json              ← strict: true
│   ├── jest.config.ts
│   └── src/
│       ├── main.ts
│       ├── app/
│       │   ├── app.config.ts
│       │   ├── app.routes.ts
│       │   ├── core/
│       │   │   ├── auth/
│       │   │   ├── guards/
│       │   │   │   ├── auth.guard.ts
│       │   │   │   └── admin.guard.ts
│       │   │   ├── interceptors/
│       │   │   │   ├── auth.interceptor.ts
│       │   │   │   └── locale.interceptor.ts
│       │   │   └── services/
│       │   │       ├── framework.service.ts
│       │   │       ├── threat.service.ts
│       │   │       ├── card-suit.service.ts
│       │   │       ├── matrix.service.ts
│       │   │       ├── search.service.ts
│       │   │       ├── export.service.ts
│       │   │       ├── locale.service.ts
│       │   │       └── auth.service.ts
│       │   ├── shared/
│       │   │   ├── components/
│       │   │   │   ├── threat-card/
│       │   │   │   ├── cornucopia-card/
│       │   │   │   ├── code-sample-panel/
│       │   │   │   ├── bot-warning-dialog/
│       │   │   │   ├── stride-heatmap/
│       │   │   │   ├── matrix-table/
│       │   │   │   ├── llm-matrix/
│       │   │   │   ├── language-toggle/
│       │   │   │   ├── severity-badge/
│       │   │   │   └── owasp-ref-chip-list/
│       │   │   ├── models/
│       │   │   │   ├── framework.model.ts
│       │   │   │   ├── threat.model.ts
│       │   │   │   ├── cornucopia-card.model.ts
│       │   │   │   ├── mitigation.model.ts
│       │   │   │   └── code-sample.model.ts
│       │   │   ├── pipes/
│       │   │   │   ├── truncate.pipe.ts
│       │   │   │   ├── severity-color.pipe.ts
│       │   │   │   └── oat-label.pipe.ts
│       │   │   └── directives/
│       │   │       ├── highlight.directive.ts
│       │   │       └── lazy-prism.directive.ts
│       │   ├── features/
│       │   │   ├── dashboard/
│       │   │   ├── frameworks/
│       │   │   ├── threats/
│       │   │   ├── suits/
│       │   │   │   ├── frontend-security/
│       │   │   │   ├── llm-security/
│       │   │   │   ├── agentic-ai/
│       │   │   │   ├── stride-catalogue/
│       │   │   │   ├── ml-security/
│       │   │   │   ├── mobile-security/
│       │   │   │   └── devops-security/
│       │   │   ├── matrix/
│       │   │   ├── stride-heatmap/
│       │   │   ├── coverage/
│       │   │   └── search/
│       │   └── store/
│       │       ├── frameworks.store.ts
│       │       ├── threats.store.ts
│       │       ├── card-suits.store.ts
│       │       ├── search.store.ts
│       │       └── ui.store.ts
│       ├── assets/
│       │   └── i18n/
│       │       ├── pl.json
│       │       └── en.json
│       └── styles/
│           ├── theme.scss
│           └── styles.scss
│
├── data/
│   ├── owasp_web_top10.json
│   ├── owasp_llm_top10.json
│   ├── owasp_agentic_top10.json
│   ├── mitre_atlas.json
│   ├── comptia_secai.json
│   ├── cornucopia/
│   │   ├── webapp-cards-3.0-en.yaml
│   │   ├── companion-llm-cards-1.0-en.yaml
│   │   ├── mobileapp-cards-1.1-en.yaml
│   │   ├── stride-eop-cards-5.0-en.yaml
│   │   ├── mlsec-cards-1.0-en.yaml
│   │   └── translations/
│   │       ├── pl.cards.json
│   │       └── en.cards.json
│   ├── hashes.json
│   ├── mitre-atlas-allowlist.json
│   ├── ref-allowlists.json
│   └── code_samples/
│       ├── python/
│       ├── java/
│       ├── go/
│       ├── scala/
│       └── lua/
│
├── e2e/
│   ├── cypress.config.ts
│   └── cypress/
│       └── e2e/
│           ├── us01-framework-browser.cy.ts
│           ├── us02-threat-filter.cy.ts
│           ├── ...
│           └── us18-devops-security.cy.ts
│
└── docker-compose.yml
```

---

## 13. User Stories — Kompletna Lista

| ID | Rola | Potrzeba | Cel |
|---|---|---|---|
| US-01 | security engineer | przeglądać katalog frameworków bezpieczeństwa | mieć jeden punkt dostępu do wszystkich standardów |
| US-02 | security engineer | filtrować zagrożenia według frameworku, severity, STRIDE, tagu | szybko znaleźć zagrożenia istotne dla projektu |
| US-03 | security engineer | widzieć szczegóły zagrożenia z mitigacjami i próbkami kodu | rozumieć jak wdrożyć ochronę |
| US-04 | CompTIA SecAI+ student | zobaczyć jak LLM01 Prompt Injection mapuje do MITRE ATLAS AML.T0051 | rozumieć zależności między frameworkami |
| US-05 | security trainer | wyświetlić heatmapę STRIDE na projektorze | wizualnie wyjaśnić pokrycie STRIDE na warsztatach |
| US-06 | pentester | wyszukać "deepfake" i znaleźć wszystkie powiązane zagrożenia | złożyć checklistę testów dla klienta |
| US-07 | team lead | wyeksportować przefiltrowaną listę zagrożeń do CSV | włączyć ją do rejestru ryzyk |
| US-08 | developer | zobaczyć timeline Kill Chain MITRE | rozumieć na jakiej fazie ataku działa każda technika |
| US-09 | Scala developer | znaleźć próbki kodu dla ataków na łańcuch dostaw w Scala | zaimplementować SCA w potoku Scala |
| US-10 | Lua/OpenResty developer | zobaczyć przykłady Lua dla rate limiting zapobiegającego LLM DoS | skonfigurować guardrails NGINX dla proxy LLM API |
| US-11 | Polish-speaking student | przełączyć całą aplikację z angielskiego na polski jednym kliknięciem | uczyć się wszystkich opisów zagrożeń w ojczystym języku |
| US-12 | React/frontend developer | przeglądać karty OWASP Cornucopia FRE z polskimi opisami i ref Client-Side Top 10 | mapować scenariusze ataków client-side na mitigacje |
| US-13 | ML engineer | eksplorować OWASP LLM Top 10 2025 przez karty Cornucopia LLM z macierzą interaktywną | rozumieć prompt injection, data poisoning, excessive agency |
| US-14 | agentic AI developer | studiować OWASP Agentic AI Top 10 2026 przez karty AAI | projektować safeguards human-in-the-loop dla agentów |
| US-15 | security architect | używać katalogu kart STRIDE EoP z interaktywną heatmapą per komponent | prowadzić ustrukturyzowaną sesję threat modelingu |
| US-16 | data scientist | przeglądać ryzyka ML (EMR/EIR/EOR/EDR) z referencjami MITRE ATLAS | identyfikować adversarial ML, model theft, data poisoning |
| US-17 | Android/iOS developer | zobaczyć zagrożenia OWASP MASVS przez karty Cornucopia Mobile App | rozumieć jak kontrolki mobile różnią się od web |
| US-18 | DevSecOps engineer | przeglądać ryzyka supply chain (DVO) i wzorce botów (BOT) | chronić CI/CD i bronić się przed automatycznymi atakami |

---

## 14. Milestones & Acceptance Criteria

| Kamień | Deliverable | Ukończone gdy |
|---|---|---|
| M1 | Działający szkielet | `docker compose up` → Angular home + `/api/v1/frameworks` zwraca JSON; `ng serve` działa |
| M2 | Pełne seedowanie danych | Wszystkie frameworki, zagrożenia, mitigacje w DB; API zwraca poprawne liczby |
| M3 | Próbki kodu kompletne | Każde zagrożenie ma 5 próbek kodu widocznych w `ThreatDetailComponent` |
| M4 | Macierz + heatmapa | MatrixComponent renderuje się; ECharts heatmapa STRIDE pokazuje procenty pokrycia |
| M5 | Wyszukiwanie działa | Full-text search zwraca wyniki z podświetlonymi fragmentami |
| M6 | i18n działa | Przełącznik PL/EN w mat-toolbar; cały UI w obu językach; kod nie tłumaczony |
| M7 | Produkcyjny build | `ng build --configuration production` bundle < 600 KB gzip; Nginx serwuje SPA; actuator/health 200 |
| M8 | Karty FRE + LLM + AAI | FrontendSecurityComponent, LlmSecurityComponent, AgenticAiComponent działają; macierze dostępne |
| M9 | STRIDE + MLSec | 78 kart STRIDE (6 suit); heatmapa STRIDE; 52 karty MLSec (4 suit) |
| M10 | Mobile + DevOps | MobileSecurityComponent i DevOpsSecurityComponent działają; tabela MASVS vs Web |
| M11 | Integralność treści | ContentIntegrityVerifier działa; CI job yaml-content-integrity GREEN |
| M12 | Testy przechodzą | ≥ 195 testów; abuse cases AC-01–AC-13 GREEN; ZAP 0 HIGH findings; Lighthouse ≥ 85 |
