# SecureVision 2026 — Application Development Plan

## 1. Project Overview

**Name:** SecureVision 2026  
**Purpose:** An interactive reference and learning platform that maps security threats, vulnerabilities, and mitigations across four major frameworks — OWASP (Web + LLM Top 10), MITRE ATLAS (AI/ML adversarial threats), CompTIA Security+ SY0-701, and CompTIA SecAI+ — and presents each threat with working sample code countermeasures in five languages: Python, Java ecosystem (Spring Boot), Go, Scala, and Lua.

**Source context:** Based on the "Security Architects" board game analysis, STRIDE threat modeling, OWASP LLM Top 10 (2025/2026), MITRE ATLAS tactics/techniques, and CompTIA SecAI+ curriculum documented in `docs/Security Architects+ Comptia+OWASP LLM top10__v01b.md`.

An application have to have a switch for user to change the language from Polish to English and From English to Polish, and the application must be written in Polish ang English for the user.


---

## 2. Technology Stack

### Backend
| Layer | Technology | Version (2026) |
|---|---|---|
| Runtime | Java | 21 LTS |
| Framework | Spring Boot | 3.3.x |
| API style | REST + optional GraphQL | — |
| Security | Spring Security 6 | JWT / OAuth2 |
| Persistence | PostgreSQL 16 | via Spring Data JPA |
| Cache | Redis 7 | Spring Cache |
| Build | Maven 3.9 / Gradle 8 | — |
| Docs | SpringDoc OpenAPI 3 | Swagger UI |
| Testing | JUnit 5, Mockito, Testcontainers | — |

### Frontend
| Layer | Technology | Version (2026) |
|---|---|---|
| Runtime | Node 20 LTS | — |
| Framework | React | 18.x |
| Build | Vite | 5.x |
| State | Zustand or Redux Toolkit | — |
| Router | React Router v6 | — |
| UI library | Shadcn/UI + Tailwind CSS | — |
| Syntax highlight | Prism.js or Shiki | — |
| Charts | Recharts | — |
| Testing | Vitest + Testing Library | — |

### Infrastructure
- Docker Compose (dev)
- GitHub Actions CI/CD
- Nginx reverse proxy (static + API)

---

## 3. High-Level Architecture

```
Browser (React SPA)
        │
        │  HTTPS
        ▼
  Nginx (port 80/443)
   ├── /api/*   ──► Spring Boot (port 8080)
   │                    ├── SecurityFrameworkController
   │                    ├── ThreatController
   │                    ├── MitigationController
   │                    ├── CodeSampleController
   │                    └── SearchController
   └── /*       ──► React static bundle
                       └── src/
                            ├── pages/
                            ├── components/
                            └── store/
```

---

## 4. Data Model (Core Entities)

### 4.1 Framework
```
Framework {
  id: UUID
  code: String          // "OWASP_WEB", "OWASP_LLM", "MITRE_ATLAS", "COMPTIA_SY0701", "COMPTIA_SECAI"
  name: String
  version: String       // "2025/2026"
  description: String
  referenceUrl: String
}
```

### 4.2 Threat
```
Threat {
  id: UUID
  frameworkId: UUID     // FK → Framework
  code: String          // "LLM01", "AML.T0051", "A01:2021"
  title: String
  severity: Enum        // CRITICAL, HIGH, MEDIUM, LOW, INFO
  category: String      // "Injection", "Access Control", "AI/ML", etc.
  description: String
  attackVector: String
  attackSurface: String
  stride: Set<Enum>     // S, T, R, I, D, E  (nullable for non-STRIDE frameworks)
  cveReferences: List<String>
  tags: List<String>
}
```

### 4.3 Mitigation
```
Mitigation {
  id: UUID
  threatId: UUID        // FK → Threat
  title: String
  description: String
  mitigationType: Enum  // PREVENTIVE, DETECTIVE, CORRECTIVE, COMPENSATING
  implementationEffort: Enum  // LOW, MEDIUM, HIGH
  effectiveness: Enum   // PARTIAL, SIGNIFICANT, FULL
}
```

### 4.4 CodeSample
```
CodeSample {
  id: UUID
  mitigationId: UUID    // FK → Mitigation
  language: Enum        // PYTHON, JAVA, GO, SCALA, LUA
  title: String
  description: String
  codeSnippet: String   // full runnable example
  frameworkHint: String // "Spring Boot 3", "FastAPI", "Gin", "Akka", "OpenResty"
  version: String       // language/framework version
}
```

### 4.5 CrossReference
```
CrossReference {
  id: UUID
  sourceThreatId: UUID
  targetThreatId: UUID
  relationshipType: Enum  // EQUIVALENT, RELATED, PARENT_CHILD, MAPS_TO
  description: String
}
```

---

## 5. Development Phases

### Phase 1 — Foundation (Weeks 1–3)
- [ ] Project skeleton: Spring Boot 3 + React 18 + Vite mono-repo
- [ ] Docker Compose: PostgreSQL + Redis + app
- [ ] Database schema + Flyway migrations for all 5 entities
- [ ] Data seeding: populate all OWASP Web Top 10, OWASP LLM Top 10, MITRE ATLAS top tactics/techniques, CompTIA SY0-701 AI domains, CompTIA SecAI+ threats
- [ ] REST API: CRUD for Framework, Threat, Mitigation
- [ ] Spring Security: JWT auth (admin role for data management)
- [ ] OpenAPI / Swagger UI at `/swagger-ui.html`

### Phase 2 — Core Features (Weeks 4–6)
- [ ] `GET /api/threats` with filters: framework, severity, STRIDE letter, category, tag, text search
- [ ] `GET /api/threats/{id}/mitigations` with nested code samples
- [ ] `GET /api/cross-references` — mapping table across frameworks
- [ ] React: ThreatList page with filter sidebar
- [ ] React: ThreatDetail page (description, attack vector/surface, STRIDE badge)
- [ ] React: MitigationPanel with CodeTab component (tabbed per language)
- [ ] Syntax-highlighted code blocks (Shiki / Prism)
- [ ] Framework comparison matrix page (OWASP ↔ MITRE ATLAS ↔ CompTIA)

### Phase 3 — Content & Code Samples (Weeks 7–10)
- [ ] Write all code samples for every threat × 5 languages (Python, Java, Go, Scala, Lua)
- [ ] Each sample: attack demo snippet + mitigation snippet
- [ ] Separate "Attack Example" vs "Defense Example" tabs in UI
- [ ] STRIDE visual legend and interactive STRIDE wheel component
- [ ] MITRE ATLAS Kill-Chain timeline visualization (Recharts)
- [ ] Tag cloud for browsing by category

### Phase 4 — Advanced Features (Weeks 11–13)
- [ ] Full-text search across threats + mitigations + code samples (PostgreSQL `tsvector`)
- [ ] "Coverage Heatmap" — shows which framework covers which STRIDE category
- [ ] "Threat Mapping Table" — cross-references OWASP LLM ↔ MITRE ATLAS ↔ CompTIA in one table
- [ ] Export to PDF / CSV
- [ ] Bookmarks / favorites per user (localStorage fallback)
- [ ] Dark mode toggle

### Phase 5 — Polish & Testing (Weeks 14–15)
- [ ] Unit tests: all service classes (JUnit 5 + Mockito)
- [ ] Integration tests: all REST endpoints (Testcontainers + PostgreSQL)
- [ ] React component tests (Vitest + Testing Library)
- [ ] E2E smoke tests (Playwright)
- [ ] Performance: response time < 200 ms for list queries
- [ ] Accessibility audit (axe-core)
- [ ] Docker production build + Nginx config
- [ ] README with quickstart

---

## 6. API Endpoint Map

```
GET  /api/frameworks                     — list all frameworks
GET  /api/frameworks/{code}              — single framework detail

GET  /api/threats                        — list threats (filter: frameworkCode, severity, stride, tag, q)
GET  /api/threats/{id}                   — single threat with mitigations and code samples
GET  /api/threats/{id}/mitigations       — mitigations for a threat
GET  /api/threats/{id}/code-samples      — all code samples across all mitigations

GET  /api/mitigations/{id}               — mitigation detail
GET  /api/mitigations/{id}/code-samples  — code samples for a mitigation

GET  /api/code-samples?language=JAVA     — all samples for a language

GET  /api/cross-references               — full mapping table
GET  /api/cross-references?sourceCode=LLM01  — what maps to LLM01

GET  /api/search?q=sql+injection         — global full-text search

GET  /api/stats/coverage                 — JSON used by heatmap component
```

---

## 7. React Page Structure

```
/                              — Dashboard (stats cards, quick search)
/frameworks                    — Framework overview tiles
/frameworks/:code              — Framework detail + its threat list
/threats                       — Full threat browser (filters, sort, search)
/threats/:id                   — Threat detail
  └── tabs: Overview | Attack Vectors | Mitigations | Code Samples | Cross-References
/matrix                        — Cross-framework mapping table
/coverage                      — STRIDE coverage heatmap
/search?q=...                  — Global search results
/about                         — About / documentation sources
```

---

## 8. Code Sample Strategy

Each threat gets at least one mitigation. Each mitigation gets exactly 5 code samples, one per language. Structure per sample:

```
title: "Parameterized Queries — SQL Injection Defense"
attack_demo: |
  # BAD — vulnerable code
  ...
defense_code: |
  # GOOD — secure code
  ...
framework_hint: "Spring Data JPA 3 / Python asyncpg / Go pgx v5 / Scala Slick / Lua LuaSQL"
```

Languages and their primary frameworks used in samples:
| Language | Primary Framework/Library |
|---|---|
| Python | FastAPI, SQLAlchemy, Pydantic |
| Java | Spring Boot 3, Spring Security, Spring Data JPA |
| Go | Gin, pgx v5, net/http |
| Scala | Akka HTTP, Slick, ZIO |
| Lua | OpenResty / NGINX Lua, LuaSQL |

---

## 9. Security Data Coverage Plan

### OWASP Web Top 10 (2021, current for 2026)
A01 Broken Access Control → A10 SSRF — all 10 threats

### OWASP LLM Top 10 (2025/2026)
LLM01 Prompt Injection → LLM10 Unbounded Consumption — all 10 threats

### OWASP API Security Top 10
API1 BOLA → API10 Unsafe Consumption of APIs — all 10 threats

### MITRE ATLAS (AI/ML adversarial)
- Reconnaissance: Active Scanning (AML.T0000)
- ML Attack Staging: Data Poisoning (AML.T0020), Backdoor ML Model (AML.T0018)
- Initial Access: Supply Chain Compromise (AML.T0010), Prompt Injection (AML.T0051)
- Defense Evasion: Adversarial Perturbation (AML.T0043), Evade ML Model (AML.T0015)
- Exfiltration: Model Extraction (AML.T0024), Infer Training Data Membership (AML.T0024.000)
- Impact: Denial of ML Service (AML.T0029), Spamming ML System (AML.T0046)
Minimum 15 MITRE ATLAS techniques covered.

### CompTIA Security+ SY0-701 / SecAI+
- AI as attack surface (Prompt Injection, Data Poisoning, Membership Inference, Model Inversion, Model Theft, Adversarial ML)
- AI as weapon (Deepfakes, Polymorphic Malware, AI Phishing, CAPTCHA bypass, Zero-Day Discovery)
- Defensive AI (XDR/EDR, Zero Trust, AI TRiSM, NIST AI RMF, AI-BOM)
- GRC (NIS2/UKSC, NIST AI RMF, AUP for AI)
Minimum 20 CompTIA topics covered.

### STRIDE Mapping
All 6 STRIDE categories (S, T, R, I, D, E) must have at least 3 threats each mapped.

---

## 10. Risk & Mitigation for the Build

| Risk | Mitigation |
|---|---|
| Large code sample dataset is tedious | Seed via JSON files, not hardcoded Java |
| Cross-references become inconsistent | CrossReference table with enum relationship types |
| Code samples become outdated | Version field on CodeSample; admin UI to update |
| React bundle too large with all syntax highlighter | Lazy-load Shiki language packs |
| Search too slow at scale | PostgreSQL full-text indexes on `tsvector` columns |

---

## 11. Directory Layout (Proposed)

```
app01_react/
├── PLAN.md                    ← this file
├── requirements.md
├── backend/
│   ├── pom.xml (or build.gradle)
│   ├── src/main/java/com/securevision/
│   │   ├── SecureVisionApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── security/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/         ← Flyway
│   └── src/test/
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── pages/
│       ├── components/
│       ├── store/
│       ├── api/
│       └── types/
├── data/
│   ├── owasp_web_top10.json
│   ├── owasp_llm_top10.json
│   ├── mitre_atlas.json
│   ├── comptia_secai.json
│   └── code_samples/
│       ├── python/
│       ├── java/
│       ├── go/
│       ├── scala/
│       └── lua/
└── docker-compose.yml
```

---

## 12. Milestones & Acceptance Criteria

| Milestone | Deliverable | Done-When |
|---|---|---|
| M1 | Runnable skeleton | `docker compose up` shows React home + `/api/frameworks` returns JSON |
| M2 | Full data seeded | All frameworks, threats, mitigations in DB; list API returns correct counts |
| M3 | Code samples complete | Every threat has 5 language samples visible in ThreatDetail page |
| M4 | Matrix + heatmap | Cross-reference table renders; STRIDE heatmap shows coverage percentages |
| M5 | Search working | Full-text search returns results with highlighted excerpts |
| M6 | Tests pass | Backend >80% line coverage; no failing React tests; Playwright smoke passes |
| M7 | Production build | Docker image < 500 MB; Nginx serves SPA; `/health` returns 200 |


## 13. Use tables...


### Totalne Monitorowanie i Logowanie (Comprehensive Auditing)

Jeśli dojdzie do incydentu (np. agent zostanie oszukany i podejmie złą decyzję), musisz mieć możliwość odtworzenia jego procesu myślowego.

- **Logowanie "Śladu Myślowego" (Chain-of-Thought Logging):** Zapisuj w bezpiecznych, niezmiennych logach (np. w systemie SIEM) nie tylko ostateczną odpowiedź agenta, ale cały jego proces wnioskowania: jakie kroki planował, jakie narzędzia wywołał i jakie dane od nich otrzymał.

- **Detekcja anomalii (Rate Limiting dla AI):** Monitoruj częstotliwość wywoływania narzędzi przez agenta. Jeśli w ułamku sekundy agent zaczyna masowo odpytywać funkcje API (np. w pętli wywołanej błędem lub atakiem), system powinien automatycznie zamrozić jego działanie i podnieść alert bezpieczeństwa.

### Złota zasada dla Architekta Systemu:

**Traktuj każdą informację zwrotną i każdą decyzję wygenerowaną przez agenta AI jako "dane wejściowe od nieznanego użytkownika z internetu".** Zastosowanie klasycznej zasady *Zero Trust* (Nigdy nie ufaj, zawsze weryfikuj) w stosunku do samego agenta to najskuteczniejsza metoda na utrzymanie wysokiego poziomu bezpieczeństwa całej aplikacji.

### Przejście ze „Starego” do „Nowego” Security (SecAI+)

Zasada analogii polega na tym, że fundamentalne koncepcje bezpieczeństwa pozostają niezmienne, ale zmienia się sposób ich technicznej realizacji.

| Klasyczne Security | Nowe Security (SecAI+) | Jak to działa w praktyce? |
| :-: | :-: | :-: |
| **Zasada Najmniejszych Uprawnień (Least Privilege)** | **Nadmierna sprawczość (Excessive Agency Control)** | Kiedyś ograniczałeś dostęp użytkownika do bazy danych. Dziś musisz drastycznie ograniczyć dostęp *agenta AI (wtyczek) do systemów. AI nie może mieć prawa usuwania danych ani wykonywania przelewów bez ostatecznej autoryzacji człowieka (*Human-in-the-Loop). |
| **Uwierzytelnianie i autoryzacja (MFA, RBAC)** | **Izolacja kontekstu i RAG (Context Isolation)** | Kiedyś pilnowałeś, by użytkownik A nie widział plików użytkownika B. W systemach AI (np. z bazą RAG), jeśli wrzucisz dokumenty obu użytkowników do jednej bazy wektorowej, LLM może w odpowiedzi dla użytkownika A wykorzystać wiedzę z dokumentu użytkownika B. Musisz izolować dane na poziomie wektorów. |
| **Firewall / WAF (Zapora sieciowa)** | **LLM Firewalls & Gateway** | Klasyczny WAF blokuje podejrzane pakiety sieciowe IP. WAF dla AI (np. LLM Gateway) analizuje zapytania tekstowe i blokuje te, które próbują wymusić złośliwe zachowanie modeli chmurowych, optymalizując też zużycie tokenów (*Denial of Wallet). |
| **Sanityzacja kodu i SQL i XSS** | **Bezpieczne renderowanie odpowiedzi (Output Handling)** | Jeśli LLM pod wpływem manipulacji wygeneruje złośliwy skrypt JavaScript, a Twoja strona internetowa bezrefleksyjnie go wyświetli, dojdzie do klasycznego ataku XSS na przeglądarkę użytkownika. AI traktujemy jako całkowicie niezaufane źródło danych. |

### SDLC, DRP i ARP w świecie Sztucznej Inteligencji

#### SDLC (Software Development Life Cycle) -\> Przejście w Secure MLOps

Klasyczny cykl rozwoju oprogramowania (SDLC) zakłada mitygację ryzyk od fazy projektowania (*Shift Left). W SecAI+ proces ten rozszerza się do **Secure MLOps**, gdzie zabezpiecza się nie tylko kod aplikacji, ale cały cykl życia modelu:

- **Faza zbierania danych:** Weryfikacja pod kątem *Data Poisoning (zatruwania danych).

- **Faza wyboru modelu:** Generowanie dokumentacji **AI-BOM** (AI Bill of Materials), czyli cyfrowego paszportu modelu potwierdzającego jego pochodzenie i brak podatności w łańcuchu dostaw.

- **Faza testów:** Przeprowadzanie dedykowanego **AI Red Teaming** (prób złamania modelu przed wdrożeniem).

### Tabela mapowania podatności i zagrożeń AI

| Zagrożenie / Podatność | OWASP (LLM Top 10) | MITRE ATLAS (Taktyka) | CompTIA SecAI+ (Rola) | Szkolenie S.. (Pokrycie) |
| :-: | :-: | :-: | :-: | :-: |
| **Wstrzykiwanie promptów (Prompt Injection)** | LLM01: Prompt Injection | TA0001: Initial Access / ML Model Input Manipulation | **AI jako cel:** Wektor bezpośredni (czaty) i pośredni (RAG, złośliwe strony www). | Blok: Testowanie odporności modeli LLM, audytowanie danych wejściowych i walidacja promptów. |
| **Zatruwanie danych treningowych (Data Poisoning)** | LLM03: Training Data Poisoning | TA0006: Adversarial ML / Poison Data Supply Chain | **AI jako cel:** Wektor podaży (zanieczyszczenie potoków MLOps i hurtowni danych). | Blok: Bezpieczeństwo potoków danych (Data pipelines), weryfikacja integralności zbiorów treningowych. |
| **Kradzież / Ekstrakcja modelu (Model Theft)** | LLM10: Model Theft | TA0010: Exfiltration / Model Inversion & Extraction | **AI jako cel:** Wektor odpytywania API w celu sklonowania wag i IP firmy. | Blok: Ochrona produkcyjna modeli, wykrywanie anomalii w zapytaniach API i rate limiting. |
| **Nadmierna sprawczość agentów (Excessive Agency)** | LLM02: Insecure Output Handling / LLM07: Excessive Agency | TA0002: Execution / Execution via LLM Plugins | **AI jako cel:** Podatność aplikacji integrującej wtyczki AI z systemami operacyjnymi/bazami. | Blok: Projektowanie bezpiecznej architektury wokół AI, zasada minimalnych uprawnień dla wtyczek. |
| **Wnioskowanie o prywatności (Membership Inference / Inversion)** | LLM06: Sensitive Information Disclosure | TA0009: Discovery / ML Model Inference Attacks | **AI jako cel:** Atak na poufność danych poprzez masową analizę odpowiedzi (Confidence scores). | Blok: Zgodność wdrożeń z wymaganiami prawnymi (RODO/AI Act) oraz techniki prywatności różnicowej. |
| **Polimorficzne złośliwe oprogramowanie (AI-Mutating Malware)** | *Nie dotyczy (błąd infrastruktury, nie aplikacji LLM) | TA0003: Defense Evasion (generowanie kodu omijającego EDR) | **AI jako broń:** Użycie LLM przez hakera do dynamicznej zmiany sygnatur wirusów. | Blok: Reagowanie na incydenty i wykrywanie zaawansowanych zagrożeń nowej generacji. |
| **Zautomatyzowany Phishing i Deepfake** | *Nie dotyczy (atak socjotechniczny zewnętrzny) | TA0001: Initial Access / Reconnaissance (profilowanie ofiar) | **AI jako broń:** Generowanie masowych, bezbłędnych wiadomości i synteza głosu (Vishing). | Blok: Analiza zagrożeń z użyciem AI, modelowanie ryzyk organizacyjnych i procedury bezpieczeństwa. |
| **Uczenie adwersaryjne (Adversarial Examples / Evasion)** | LLM01: Prompt Injection (szerokie ujęcie manipulacji wejściem) | TA0003: Defense Evasion / Evasion via Adversarial Input | **AI jako cel:** Modyfikowanie danych wejściowych (szum w obrazie/audio) w celu oszukania klasyfikatora. | Blok: Metody obrony modeli ML przed złośliwymi modyfikacjami, audytowanie stabilności sieci neuronowych. |
