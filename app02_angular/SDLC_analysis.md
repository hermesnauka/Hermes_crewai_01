# ThreatView 2026 — Analiza SSDLC

**Wersja:** 1.0  
**Data:** 2026-07-07  
**Metodologia:** Agile (Scrum + Kanban) z integracją SSDLC  
**Projekt:** ThreatView 2026 — Angular 18 + Spring Boot 3.3 platforma referencyjna cyberbezpieczeństwa

---

## 1. Przegląd metodologii

### 1.1 Scrum + Security

ThreatView 2026 jest rozwijany w metodyce **Scrum** z wbudowaną warstwą bezpieczeństwa na każdym poziomie procesu. Bezpieczeństwo nie jest dodatkiem na końcu projektu — jest elementem **Definition of Done (DoD)** każdej historyjki użytkownika.

**Kluczowe praktyki:**

- **2-tygodniowe sprinty** (Sprints 1–16, łącznie 31 tygodni, 16 miesięcy)
- **Daily standup** — każde spotkanie zawiera pytanie: *"Czy jest jakiś bloker bezpieczeństwa?"*
- **Sprint Planning** — każda US (User Story) ma DoD uwzględniający sprawdzenie bezpieczeństwa:
  - SAST przeszedł GREEN
  - Testy jednostkowe napisane PRZED kodem (TDD)
  - Abuse cases zidentyfikowane i przetestowane
  - Code review przez osobę inną niż autor
- **Sprint Review** — demo funkcjonalności + prezentacja wyników skanów SAST z mijającego sprintu
- **Sprint Retrospective** — omówienie incydentów bezpieczeństwa, false positives w skanach, długu technicznego bezpieczeństwa

**Definition of Done (DoD) — poziom bezpieczeństwa:**

| Kryterium | Narzędzie weryfikacji |
|---|---|
| Kod przeszedł SAST | SpotBugs + FindSecBugs (backend), ESLint security plugin (frontend) |
| Brak High/Critical CVE w zależnościach | OWASP Dependency Check + npm audit |
| Testy jednostkowe pokrycie ≥ 80% (backend) | JaCoCo |
| Testy komponentów pokrycie ≥ 75% (frontend) | Jest coverage |
| Abuse cases objęte testami integracyjnymi | JUnit 5 + Testcontainers |
| Code review zatwierdzony (minimum 1 reviewer) | GitHub Pull Request |
| Dokumentacja zagrożeń zaktualizowana | SDLC_analysis.md + requirements.md |

### 1.2 Kanban Security Board

Tablica Kanban uzupełnia Scrum — służy do śledzenia zadań bezpieczeństwa, które pojawiają się asynchronicznie (np. nowe CVE w zależnościach, znalezione podatności podczas code review).

```
KOLUMNY TABLICY KANBAN BEZPIECZEŃSTWA:
┌──────────┬──────────────────┬─────────────┬────────────┬──────────────┬──────────┬──────┐
│ Backlog  │ Security Review  │ In Progress │ SAST Check │ Code Review  │ Testing  │ Done │
└──────────┴──────────────────┴─────────────┴────────────┴──────────────┴──────────┴──────┘

Typy kart Kanban:
  [SEC]  — zadanie bezpieczeństwa (nowe zabezpieczenie, hardening)
  [CVE]  — nowa podatność CVE w zależnościach do patchowania
  [AC]   — abuse case do przetestowania
  [DEBT] — dług techniczny bezpieczeństwa
  [FIND] — znalezisko z SAST/DAST/pentest
```

**Limity WIP (Work In Progress):**
- Security Review: max 3 karty jednocześnie
- SAST Check: max 5 kart (automatyczne przez CI)
- Code Review: max 4 karty (reviewer nie może reviewować więcej niż 4 PR naraz)

### 1.3 Mapa sprintów

| Sprint | Tygodnie | Faza | User Stories | Główne deliverables |
|---|---|---|---|---|
| 1–2 | 1–4 | Faza 1: Fundament | US-01 | Angular 18 scaffold, Spring Boot 3.3, Docker Compose, Flyway V1, SecurityConfig JWT, podstawowe API |
| 3–4 | 5–8 | Faza 2: Rdzeń | US-02, US-03 | Przeglądarka zagrożeń, filtry, szczegóły zagrożenia, mitigacje |
| 5–6 | 9–12 | Faza 3: Próbki kodu | US-04, US-08, US-09, US-10 | Próbki kodu 5 języków, MITRE Kill-Chain timeline, ATTACK_DEMO badge |
| 6–7 | 11–14 | Faza 4: Zaawansowane | US-05, US-06, US-07 | Full-text search, export CSV/PDF, heatmapa pokrycia STRIDE |
| 8 | 15–16 | Faza 5: i18n | US-11 | ngx-translate PL/EN, LanguageToggle, LocaleInterceptor |
| 9 | 17–18 | Faza 6: Cornucopia FRE+LLM+AAI | US-12, US-13, US-14 | CornucopiaCard entity, ContentIntegrityVerifier, suit browsery FRE/LLM/AAI |
| 10–11 | 19–22 | Faza 7: STRIDE + MLSec | US-15, US-16 | Katalog 78 kart STRIDE, ECharts heatmapa, 52 karty MLSec |
| 12–13 | 23–26 | Faza 8: Mobile + DevOps | US-17, US-18 | MASVS browser, DVO+BOT browser, BotWarningDialog |
| 14–16 | 27–31 | Faza 9: Hardening | US-01–US-18 | Testy integracyjne, ZAP full scan, audit a11y, monitoring |

---

## 2. Faza 0 — Planowanie i modelowanie zagrożeń

### 2.1 Wymagania bezpieczeństwa — zbieranie

#### Warsztaty z interesariuszami

Przed rozpoczęciem implementacji przeprowadzane są warsztaty z interesariuszami projektu w celu zebrania **Security Acceptance Criteria (SAC)** dla każdej historyjki użytkownika. Warsztaty opierają się na metodzie **OWASP SAMM (Software Assurance Maturity Model)** — ocena dojrzałości procesów bezpieczeństwa w projekcie.

**Uczestnicy:**
- Security Lead / Security Champion (1 osoba z zespołu deweloperskigo)
- Backend Developer (Java/Spring Boot)
- Frontend Developer (Angular)
- DevOps Engineer (CI/CD, Docker)
- Product Owner (właściciel wymagań biznesowych)

**Wyniki warsztatów:**
1. Zainicjowany **Risk Register** — lista zidentyfikowanych ryzyk z oceną prawdopodobieństwa i wpływu
2. Zdefiniowane **SAC** (Security Acceptance Criteria) — konkretne, mierzalne kryteria bezpieczeństwa włączone do DoD każdej US
3. Wstępny **STRIDE threat model** dla ThreatView 2026 jako systemu
4. Zidentyfikowane aktywa do ochrony: YAML pliki kart Cornucopia, tokeny JWT adminów, baza danych zagrożeń

#### Ocena OWASP SAMM

| Domena SAMM | Praktyka | Docelowy poziom dojrzałości |
|---|---|---|
| Governance | Policy & Compliance | Poziom 1 |
| Design | Threat Assessment | Poziom 2 |
| Design | Security Requirements | Poziom 2 |
| Implementation | Secure Build | Poziom 2 |
| Implementation | Secure Deployment | Poziom 1 |
| Verification | Security Testing | Poziom 2 |
| Verification | Requirements-driven Testing | Poziom 2 |
| Operations | Incident Management | Poziom 1 |

#### Meta-refleksja: "Dogfooding" SSDLC

ThreatView 2026 jest aplikacją uczącą o bezpieczeństwie — zatem jej własny process SSDLC jest demonstracją treści, którą prezentuje. Szczególnie istotne jest:

- Używamy własnych kart **STRIDE EoP** (US-15) do modelowania zagrożeń samej aplikacji ThreatView
- Używamy kart **BOT** (US-18) jako przykładu — i jednocześnie sami implementujemy ochronę BOT (Bucket4j)
- Używamy kart **DVO** (US-18) jako przykładu zagrożeń CI/CD — i jednocześnie zabezpieczamy własny pipeline CI/CD

### 2.2 Modelowanie zagrożeń — STRIDE dla ThreatView 2026

Poniższa tabela zawiera model zagrożeń **dla samej aplikacji ThreatView 2026** (nie dla treści, które prezentuje), oparty na metodologii STRIDE.

| Kategoria STRIDE | Komponent | Zagrożenie | Ryzyko | Mitigacja |
|---|---|---|---|---|
| **S** — Spoofing | `POST /api/v1/admin/*` | Nieuprawniony użytkownik podszywa się pod admina | HIGH | Spring Security 6 JWT z ADMIN role claim, RS256 podpis tokenu |
| **S** — Spoofing | Angular JWT handling | Kradzież tokenu przez XSS → session hijacking | HIGH | HttpOnly cookies dla refresh token, access token 15 min TTL |
| **T** — Tampering | Pliki YAML kart Cornucopia | Modyfikacja opisów kart bezpośrednio w repozytorium | HIGH | ContentIntegrityVerifier SHA-256 + CODEOWNERS (@security-team, 2 approvals) |
| **T** — Tampering | Angular templates | XSS przez niebezpieczne użycie innerHTML / bypassSecurityTrustHtml | HIGH | Angular DomSanitizer, strict mode, CSP `script-src 'self'` |
| **T** — Tampering | Flyway migracje bazy | Modyfikacja skryptów migracyjnych w repozytorium | MEDIUM | Flyway checksums + CI weryfikacja skryptów migracyjnych |
| **R** — Repudiation | `PUT /api/v1/admin/threats/{id}` | Admin twierdzi że nie zmodyfikował opisu zagrożenia | MEDIUM | Structured audit log (Loki): kto, co, kiedy, jaki był stary vs nowy stan |
| **R** — Repudiation | Spring Security auth events | Brak dowodów na próby nieudanego logowania | LOW | `AuthenticationFailureEvent` → Loki log + SEC-009 alert |
| **I** — Info Disclosure | `GET /api/v1/threats` error responses | Wyciek stack trace / struktury bazy przez verbose error messages | MEDIUM | `GlobalExceptionHandler` — generyczne komunikaty 400/500 w produkcji |
| **I** — Info Disclosure | `/api/v1/actuator/*` | Ujawnienie środowiska, beanów, konfiguracji przez Actuator | HIGH | Actuator: expose only `/health` i `/metrics`, blokada `/env`, `/beans`, `/configprops` |
| **I** — Info Disclosure | YAML card content w git history | Ujawnienie nieopublikowanych lub usuniętych opisów kart przez `git log` | LOW | Secret scanning w CI (gitleaks), git-crypt dla wrażliwych danych |
| **D** — DoS | `GET /api/v1/threats?suit=BOT` | Bot scraping całego katalogu kart w pętli | HIGH | Bucket4j 60 req/min per IP → HTTP 429 + Loki SEC-007 alert |
| **D** — DoS | ECharts STRIDE heatmap rendering | Duże SVG heatmapy generowane per-request dla wielu użytkowników | MEDIUM | Server-side SVG cache w Redis (TTL 5 min), odpowiedź cachowana per komponent |
| **D** — DoS | Full-text search PostgreSQL tsvector | Bardzo złożone zapytania wyszukiwania blokujące DB | MEDIUM | Limit długości query (max 200 znaków), timeout dla zapytań DB (5s) |
| **E** — Elevation | `DELETE /api/v1/admin/threats/{id}` | Użytkownik z rolą USER próbuje usunąć zagrożenie | HIGH | `@PreAuthorize("hasRole('ADMIN')")` na metodach serwisu + SecurityConfig |
| **E** — Elevation | Angular route access | Użytkownik bez JWT wchodzi na chronione trasy (np. /admin) | HIGH | `AuthGuard` (Angular CanActivateFn) weryfikuje token przed nawigacją |

### 2.3 Kryteria akceptacji bezpieczeństwa (Security Acceptance Criteria — SAC)

Poniższe kryteria są włączone do **DoD każdego sprintu** i muszą być spełnione przed zamknięciem historyjki.

| ID | Kryterium | Sposób weryfikacji |
|---|---|---|
| SAC-01 | Wszystkie endpointy `/api/v1/admin/*` wymagają JWT z rolą ADMIN | Test: `AdminControllerIT` — 401 bez tokenu, 403 bez roli ADMIN, 200 z poprawnym tokenem |
| SAC-02 | SAST (SpotBugs + FindSecBugs + ESLint security) musi przejść GREEN przed merge do main | CI job `lint-and-sast` zablokuje merge jeśli fail |
| SAC-03 | Brak High/Critical findings w OWASP Dependency Check i `npm audit` | CI job `dependency-check` zablokuje merge przy CVSS ≥ 7.0 |
| SAC-04 | `ContentIntegrityVerifier` weryfikuje SHA-256 wszystkich plików YAML przy starcie — fail-secure | IT: `YamlIntegrityVerifierTest` — zmodyfikowany plik YAML blokuje start aplikacji |
| SAC-05 | Rate limiter Bucket4j zwraca HTTP 429 po przekroczeniu 60 req/min per IP | IT: `RateLimitIntegrationTest` — 61 żądań → ostatnie otrzymuje 429 |
| SAC-06 | CSP header blokuje inline scripts i `eval` | ZAP headerscan: `Content-Security-Policy: default-src 'self'; script-src 'self'` |
| SAC-07 | Angular `DomSanitizer` — zabronienie `bypassSecurityTrustHtml` dla opisów kart Cornucopia | Jest test: `CornucopiaCardComponent.spec.ts` — brak wywołań `bypassSecurityTrustHtml` |
| SAC-08 | Angular strict mode (`strictTemplates: true`) — żadnych niebezpiecznych rzutowań typów | `ng build --configuration production` bez błędów TypeScript strict |
| SAC-09 | Wszystkie próbki kodu `ATTACK_DEMO` mają widoczną etykietę "PODATNY" w UI | Jest test: `CodeSamplePanelComponent.spec.ts` — badge 'PODATNY' obecny dla ATTACK_DEMO |
| SAC-10 | `BotWarningDialog` wyświetla się przed pokazaniem kart BOT — wymaga kliknięcia "Rozumiem" | Cypress E2E: `us18-devops-security.cy.ts` — dialog widoczny, bez kliknięcia brak kart |
| SAC-11 | `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'` dla `/stride-heatmap` | ZAP headerscan + Cypress: strona `/stride-heatmap` zwraca poprawne nagłówki |
| SAC-12 | WCAG 2.1 AA — axe-core bez Critical/Serious findings | cypress-axe: `cy.checkA11y()` na każdej stronie w E2E suite |
| SAC-13 | Testy E2E Cypress — wszystkie 18 plików `*.cy.ts` przechodzą GREEN | CI job `e2e-tests` przeciw staging environment |
| SAC-14 | Backend coverage ≥ 80% (JaCoCo), frontend coverage ≥ 75% (Jest) | CI job `unit-tests` sprawdza progi pokrycia |
| SAC-15 | DAST ZAP full active scan — zero High/Critical alerts na staging | CI job `dast-scan` — raport jako artifact, fail przy High/Critical |

---

## 3. Faza 1 — Fundament (Sprint 1–2, Tygodnie 1–4)

### 3.1 Architektura bezpieczeństwa — decyzje projektowe

| ID | Decyzja | Uzasadnienie bezpieczeństwa |
|---|---|---|
| D-01 | Angular 18 **strict mode** (`strict: true`, `strictTemplates: true`) | Eliminuje całą klasę błędów XSS w czasie kompilacji — niebezpieczne bindowania `[innerHTML]` do niezaufanych źródeł stają się błędem kompilacji |
| D-02 | `DomSanitizer` jako jedyna droga renderowania HTML z API | Zakaz użycia `innerHTML` bezpośrednio — każde renderowanie treści kart musi przejść przez `sanitizer.sanitize(SecurityContext.HTML, value)` |
| D-03 | Spring Security 6 **stateless JWT** (`SessionCreationPolicy.STATELESS`) | Eliminuje CSRF dla REST API — brak sesji serwerowych = brak CSRF token management |
| D-04 | Wyłącznie **JPA parameterized queries** — zakaz String concatenation w zapytaniach | Eliminuje SQL Injection — wszelkie filtry API używają `@Query("... WHERE t.code = :code")` z named parameters |
| D-05 | **Docker Compose secrets** dla haseł DB i JWT signing key (RSA private key) | Hasła nigdy w zmiennych środowiskowych ani w plikach `.env` w repozytorium |
| D-06 | **PostgreSQL Row-Level Security** dla danych użytkownika (zakładki) | Użytkownik widzi tylko własne zakładki — na poziomie bazy, nie tylko aplikacji |
| D-07 | **Redis** dla caching (TTL max 5 min) — nie przechowuje danych wrażliwych | Cache tylko dla publicznych danych (listy zagrożeń, heatmapa STRIDE) — nigdy tokenów ani danych osobowych |
| D-08 | **Nginx** reverse proxy z HTTPS tylko zewnętrznie — HTTP wewnątrz Docker network | TLS termination na Nginx, backend nie zarządza certyfikatami SSL |

### 3.2 Konfiguracja środowiska deweloperskiego

#### Git hooks (pre-commit)

Zainstalowane przez `husky` + `lint-staged`:

```
pre-commit:
  1. ESLint (ng lint) — security rules: no-eval, no-new-func, no-innerHTML
  2. SpotBugs check (Maven verify -DskipTests) — tylko backend
  3. gitleaks — skanowanie pod kątem sekretów w diff (klucze API, hasła, tokeny)
  4. markdownlint — walidacja plików .md
  5. check-yaml — walidacja plików YAML kart Cornucopia

pre-push:
  1. ng build --configuration production (weryfikacja że build nie jest zepsuty)
  2. mvn test -q (szybkie testy jednostkowe)
```

#### CODEOWNERS

```
# Pliki kart Cornucopia wymagają 2 zatwierdzeń od @security-team
/data/cornucopia/           @security-team @content-lead
/data/hashes.json           @security-team
/data/mitre-atlas-allowlist.json  @security-team
/data/ref-allowlists.json   @security-team

# Konfiguracja bezpieczeństwa wymaga zatwierdzenia Security Lead
/backend/src/main/java/com/threatview/config/SecurityConfig.java  @security-lead
/backend/src/main/java/com/threatview/integrity/              @security-lead
```

#### Branch protection rules

- **main**: brak direct push, wymagane PR + 1 approved review + CI GREEN + 0 unresolved comments
- **develop**: brak direct push, wymagane PR + CI GREEN
- **feature/***: swobodne push; merge do develop przez PR
- **release/***: wymagane PR do main + security-lead approval

#### .editorconfig

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 2
trim_trailing_whitespace = true
insert_final_newline = true

[*.java]
indent_size = 4

[*.md]
trim_trailing_whitespace = false
```

### 3.3 Zabezpieczenia CI/CD — pipeline bazowy (GitHub Actions)

Pipeline zdefiniowany w `.github/workflows/ci.yml`:

```
Trigger: push/PR do main i develop

JOB 1: lint-and-sast
  Uruchamia się na: ubuntu-latest
  Kroki:
    - checkout
    - setup Java 21, Node 20
    - ng lint (ESLint z regułami security)
    - mvn spotbugs:check (SpotBugs + FindSecBugs plugin)
    - eslint --plugin security --plugin no-unsanitized
    - gitleaks detect (skanowanie historii git pod kątem sekretów)
  Fail criteria: SAST HIGH/CRITICAL finding lub secret znaleziony

JOB 2: unit-tests (zależy od lint-and-sast)
  Kroki:
    - ng test --code-coverage --watch=false (Jest 29)
    - mvn test (JUnit 5 + Mockito)
    - upload JaCoCo + Jest coverage reports
    - check coverage thresholds: backend ≥ 80%, frontend ≥ 75%
  Fail criteria: pokrycie poniżej progu lub test fail

JOB 3: dependency-check (zależy od lint-and-sast)
  Kroki:
    - mvn org.owasp:dependency-check-maven:check
      --failBuildOnCVSS=7 (fail dla High+)
    - npm audit --audit-level=high
    - upload raport HTML jako artifact
  Fail criteria: CVSS ≥ 7.0 (High/Critical)

JOB 4: build (zależy od unit-tests)
  Kroki:
    - ng build --configuration production (Angular bundle)
    - mvn package -DskipTests (Spring Boot JAR)
    - upload artifacts

JOB 5: integration-tests (zależy od build)
  Kroki:
    - mvn verify (Testcontainers — spins PostgreSQL 16 + Redis 7)
    - upload Surefire/Failsafe reports
  Fail criteria: IT fail

JOB 6: docker-scan (zależy od build)
  Kroki:
    - docker build (backend image, frontend image)
    - trivy image --exit-code 1 --severity CRITICAL (backend)
    - trivy image --exit-code 1 --severity CRITICAL (frontend Nginx)
  Fail criteria: CRITICAL CVE w obrazach Docker

JOB 7: yaml-content-integrity (tylko dla PRów dotykających /data/cornucopia/)
  Kroki:
    - npm run validate-yaml-schema (ajv schema check)
    - grep injection patterns (script tags, JS event handlers w YAML)
    - mvn test -Dtest=YamlIntegrityVerifierTest
    - node scripts/hash-generator.js --verify (weryfikacja hashes.json)
  Fail criteria: schema error, injection pattern, hash mismatch
```

### 3.4 Zabezpieczenia Spring Boot — konfiguracja bazowa

#### SecurityConfig.java — kluczowe elementy

```java
// Stateless JWT — brak sesji serwerowych
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

// CORS — tylko dozwolone originy
.cors(c -> c.configurationSource(corsConfigurationSource()))

// CSRF wyłączone dla REST API (stateless JWT)
.csrf(AbstractHttpConfigurer::disable)

// Endpointy publiczne vs chronione
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/threats/**", "/api/v1/frameworks/**").permitAll()
    .requestMatchers("/api/v1/search").permitAll()
    .requestMatchers("/api/v1/stride-heatmap").authenticated()
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)

// Security headers
.headers(h -> h
    .frameOptions(f -> f.deny())  // X-Frame-Options: DENY
    .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
    .contentSecurityPolicy(csp -> csp.policyDirectives(
        "default-src 'self'; " +
        "script-src 'self'; " +
        "style-src 'self' 'unsafe-inline'; " +  // Angular Material wymaga inline styles
        "img-src 'self' data:; " +
        "frame-ancestors 'none'"
    ))
)
```

#### Spring Actuator — ograniczenie ekspozycji

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
      base-path: /api/v1/actuator
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
```

#### Externalizacja sekretów

```yaml
# application.yml (w repozytorium — bez sekretów)
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  private-key: ${JWT_PRIVATE_KEY_PATH}
  public-key: ${JWT_PUBLIC_KEY_PATH}
  expiration-ms: 900000  # 15 minut
```

Sekrety przekazywane przez **Docker Compose secrets** (`/run/secrets/db_password`) lub **GitHub Secrets** w CI.

---

## 4. Faza 2 — Rdzeń funkcjonalności (Sprint 3–4, Tygodnie 5–8)

### 4.1 Bezpieczne kodowanie — backend (US-02, US-03)

#### Filtrowanie zagrożeń — zapobieganie SQL Injection

Wszystkie filtry API używają **named parameters** w JPA:

```java
// ThreatRepository.java
@Query("""
    SELECT t FROM Threat t
    WHERE (:frameworkCode IS NULL OR t.framework.code = :frameworkCode)
    AND (:severity IS NULL OR t.severity = :severity)
    AND (:strideCategory IS NULL OR :strideCategory MEMBER OF t.stride)
    AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
Page<Threat> findAllFiltered(
    @Param("frameworkCode") String frameworkCode,
    @Param("severity") Severity severity,
    @Param("strideCategory") StrideCategory strideCategory,
    @Param("q") String q,
    Pageable pageable
);
```

Zakazane jest użycie `EntityManager.createQuery("... WHERE code = '" + code + "'")`.

#### Bean Validation na DTO

```java
// ThreatFilterRequest.java (DTO)
public record ThreatFilterRequest(
    @Pattern(regexp = "^[A-Z0-9_]{1,30}$", message = "Invalid framework code format")
    String frameworkCode,

    @Pattern(regexp = "^(CRITICAL|HIGH|MEDIUM|LOW|INFO)$", message = "Invalid severity")
    String severity,

    @Size(max = 200, message = "Search query too long")
    String q,

    @Min(0) @Max(100)
    int page
) {}
```

#### StrideCategoryValidator — allowlist

```java
// StrideCategoryValidator.java
@Component
public class StrideCategoryValidator implements ConstraintValidator<ValidStrideCategory, String> {
    private static final Set<String> ALLOWED = Set.of("S", "T", "R", "I", "D", "E");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || ALLOWED.contains(value.toUpperCase());
    }
}
```

#### GlobalExceptionHandler — brak stack traces w produkcji

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", "Invalid request parameters"));
        // NIE zwracamy ex.getMessage() — zawiera nazwy pól i reguły walidacji
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex); // Stack trace tylko w logach serwera
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", "An error occurred"));
    }
}
```

#### Paginacja — zabezpieczenie przed DDoS

```java
// Wszystkie endpointy listowe
@GetMapping
public Page<ThreatDto> getThreats(
    @PageableDefault(size = 20, max = 50) Pageable pageable,
    ThreatFilterRequest filter
) { ... }
```

### 4.2 Bezpieczne kodowanie — Angular (US-02, US-03)

#### ThreatService — obsługa błędów HTTP

```typescript
// threat.service.ts
getThreats(filter: ThreatFilter): Observable<Page<Threat>> {
  return this.http.get<Page<Threat>>('/api/v1/threats', { params: filter }).pipe(
    timeout(10_000),
    catchError((error: HttpErrorResponse) => {
      if (error.status === 429) {
        return throwError(() => new RateLimitError('Przekroczono limit zapytań'));
      }
      return throwError(() => new ApiError(error.status, 'Błąd pobierania zagrożeń'));
    })
  );
}
```

#### AuthGuard — Signal-based

```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

// AuthService — Signal
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _isAuthenticated = signal(false);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
}
```

#### mat-table — bezpieczny trackBy

```typescript
// threat-browser.component.ts
trackByThreatId = (index: number, threat: Threat): string => threat.id;
```

```html
<!-- threat-browser.component.html -->
<mat-table [dataSource]="threats" [trackBy]="trackByThreatId">
  <!-- Interpolacja Angular: {{ threat.title }} — bezpieczna, auto-escaped -->
  <!-- Nigdy: <td [innerHTML]="threat.description"> -->
</mat-table>
```

### 4.3 Abuse Cases AC-01 – AC-05

| ID | Scenariusz ataku | Wektor | Mitigacja | Test |
|---|---|---|---|---|
| AC-01 | SQL Injection przez parametr `q` w filtrze zagrożeń | `q='; DROP TABLE threats; --` | Spring Data JPA named parameters — brak string concatenation | `ThreatFilterSQLInjectionTest.java` |
| AC-02 | Manipulacja payload JWT (zmiana `role: USER` na `role: ADMIN`) | JWT bez weryfikacji sygnatury | RS256 asymetryczny — backend ma tylko klucz publiczny | `JwtValidationTest.java` |
| AC-03 | Mass enumeration API kart przez bota | 200 req/min na `/api/v1/threats` | Bucket4j 60 req/min per IP → HTTP 429 | `RateLimitIntegrationTest.java` |
| AC-04 | XSS przez Angular template binding | `threat.title = "<script>alert(1)</script>"` | Angular interpolation `{{ }}` auto-escapes — `<script>` staje się tekstem | `ThreatCardXSSTest.spec.ts` |
| AC-05 | IDOR — użytkownik A odczytuje zakładki użytkownika B przez `/api/v1/bookmarks/{userId}` | Zmiana userId w URL | Zapytanie filtruje po `principal.name` (nie po parametrze URL) | `BookmarkAuthorizationIT.java` |

---

## 5. Faza 3 — Próbki kodu (Sprint 5–6, Tygodnie 9–12)

### 5.1 Wymagania bezpieczeństwa dla próbek kodu

| ID | Wymaganie | Implementacja |
|---|---|---|
| SR-CODE-01 | Każda próbka `ATTACK_DEMO` musi być oznaczona etykietą "PODATNY" | Angular Material `mat-chip` z kolorem `warn` + czerwone obramowanie `mat-card` |
| SR-CODE-02 | Próbki `ATTACK_DEMO` nigdy nie są wykonywane server-side | `CodeSampleService` zwraca tylko tekst — brak `Runtime.exec()`, `ScriptEngine` |
| SR-CODE-03 | Próbki kodu nie są tłumaczone (i18n wyłączone dla `codeSnippet`) | `codeSnippet` wyłączony z `ngx-translate` — renderowany zawsze po angielsku |
| SR-CODE-04 | Prism.js / Shiki ładowany lazy per język — zmniejszenie attack surface | Angular lazy loading: `import('prismjs/components/prism-python')` dopiero gdy tab Python wybrany |
| SR-CODE-05 | Copy button dla `ATTACK_DEMO` wyświetla dialog ostrzeżenia przed skopiowaniem | `MatDialog` z komunikatem bezpieczeństwa — kliknięcie OK wymagane |

### 5.2 Testy bezpieczeństwa próbek kodu

```
CodeSamplePanelComponent.spec.ts — testy jednostkowe:

it('powinien wyświetlić badge PODATNY dla ATTACK_DEMO')
  → sprawdza obecność elementu .danger-badge z tekstem 'PODATNY'

it('nie powinien wyświetlić badge PODATNY dla DEFENSE')
  → sprawdza brak elementu .danger-badge

it('powinien otworzyć dialog ostrzeżenia przed skopiowaniem ATTACK_DEMO')
  → symuluje kliknięcie Copy → weryfikuje MatDialog.open() wywołane

it('nie powinien otwierać dialogu dla DEFENSE próbki')
  → symuluje kliknięcie Copy → MatDialog.open() NIE wywołane

it('nie powinien używać bypassSecurityTrustHtml dla codeSnippet')
  → weryfikuje że DomSanitizer.bypassSecurityTrustHtml nie jest wywołany

it('powinien renderować kod przez pre/code z klasą language-X')
  → Prism.js renderuje w <code class="language-python"> — nie przez innerHTML
```

---

## 6. Faza 4 — Zaawansowane funkcje (Sprint 6–7, Tygodnie 11–14)

### 6.1 Wyszukiwanie — bezpieczeństwo

#### PostgreSQL full-text search — konfiguracja bezpieczna

```sql
-- Flyway V5__add_fts_index.sql
CREATE INDEX threats_fts_idx ON threats
USING GIN (to_tsvector('english', title || ' ' || description));
```

Zapytanie zawsze przez JPA z named parameters — brak `to_tsquery` na surowym inputcie użytkownika:

```java
// Bezpieczna konwersja do plainto_tsquery (nie tsquery — unika ReDoS)
@Query("SELECT t FROM Threat t WHERE to_tsvector('english', t.title || ' ' || t.description) " +
       "@@ plainto_tsquery('english', :q)")
List<Threat> fullTextSearch(@Param("q") String q, Pageable pageable);
```

`plainto_tsquery` automatycznie escapes'uje specjalne znaki — nie ma ryzyka injection w `tsquery`.

| ID | Abuse Case | Mitigacja |
|---|---|---|
| AC-06 | ReDoS przez złożone query w wyszukiwarce | `@Size(max=200)` na parametrze `q` + `plainto_tsquery` (nie `tsquery`) |
| AC-07 | Enumeracja treści przez masowe wyszukiwanie | Rate limit Bucket4j 30 req/min per IP na `/api/v1/search` |

### 6.2 Eksport — bezpieczeństwo

#### CSV Export — zabezpieczenie przed CSV Injection

```java
// ExportService.java — Apache Commons CSV z quote-all
try (CSVPrinter printer = new CSVPrinter(writer,
        CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL))) {

    for (ThreatExportDto t : threats) {
        // Prefix formuły znakiem apostrofu — Excel/LibreOffice traktuje jako tekst
        String safeTitle = sanitizeCsvCell(t.getTitle());
        printer.printRecord(safeTitle, t.getSeverity(), t.getFrameworkCode());
    }
}

private String sanitizeCsvCell(String value) {
    if (value == null) return "";
    // Neutralizacja formuł CSV Injection: =, +, -, @, TAB, CR
    if (value.matches("^[=+\\-@\t\r].*")) {
        return "'" + value;  // Prefix apostrofem
    }
    return value;
}
```

#### PDF Export — Apache PDFBox

- Brak JavaScript w PDF (`PDDocumentCatalog.setOpenAction(null)`)
- Brak zewnętrznych zasobów (`PDDocument` z `MemoryUsageSetting.setupMainMemoryOnly()`)
- Limit rozmiaru PDF: max 10 MB (ochrona przed OOM przez duże eksporty)

---

## 7. Faza 5 — i18n (Sprint 8, Tygodnie 15–16)

### 7.1 Wymagania bezpieczeństwa i18n

| Wymaganie | Implementacja |
|---|---|
| Klucze i18n (pl.json/en.json) nie zawierają surowego HTML | Walidacja w CI: `node scripts/validate-i18n.js --no-html` |
| Interpolacja `ngx-translate` (`{{ 'key' | translate }}`) nie wykonuje HTML | ngx-translate używa Angular's bezpiecznej interpolacji — nie `innerHTML` |
| `Accept-Language` header sanitized — tylko 'pl' lub 'en' | Spring `LocaleInterceptor` — `if (!Set.of("pl","en").contains(lang)) lang = "en";` |
| Lokalizacja persystowana w `localStorage` (`sv_locale`) | Nie używamy `navigator.language` bez walidacji — mogłoby być manipulowane |
| Próbki kodu nigdy tłumaczone | `codeSnippet` field pominięty w procesie tłumaczenia |

#### Spring LocaleInterceptor — sanityzacja Accept-Language

```java
@Component
public class LocaleInterceptor implements HandlerInterceptor {

    private static final Set<String> ALLOWED_LOCALES = Set.of("pl", "en");

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String lang = request.getHeader("Accept-Language");
        // Sanityzacja: tylko 'pl' lub 'en' — brak parsowania BCP47 bez walidacji
        String safeLocale = (lang != null && ALLOWED_LOCALES.contains(lang.substring(0, 2).toLowerCase()))
            ? lang.substring(0, 2).toLowerCase()
            : "en";
        LocaleContextHolder.setLocale(Locale.forLanguageTag(safeLocale));
        return true;
    }
}
```

### 7.2 Test parytetu kluczy i18n

```typescript
// i18n-parity.spec.ts
import plJson from '../i18n/pl.json';
import enJson from '../i18n/en.json';

describe('i18n key parity', () => {
  it('pl.json i en.json powinny mieć identyczne zestawy kluczy', () => {
    const plKeys = flattenKeys(plJson).sort();
    const enKeys = flattenKeys(enJson).sort();
    expect(plKeys).toEqual(enKeys);
  });

  it('żaden klucz i18n nie powinien zawierać HTML tags', () => {
    const allValues = [...Object.values(flattenValues(plJson)),
                       ...Object.values(flattenValues(enJson))];
    const htmlTagRegex = /<[^>]+>/;
    allValues.forEach(v => expect(v).not.toMatch(htmlTagRegex));
  });
});
```

---

## 8. Faza 6 — Cornucopia: FRE + LLM + AAI (Sprint 9, Tygodnie 17–18)

### 8.1 Wymagania bezpieczeństwa — nowe (US-12, US-13, US-14)

| ID | Wymaganie | Implementacja |
|---|---|---|
| SR-C-01 | `CornucopiaCard.descriptionPl` i `descriptionEn` — tylko plain text, brak HTML | Walidacja przy ładowaniu YAML: `if (desc.matches(".*<[^>]+>.*")) throw new ContentValidationException()` |
| SR-C-02 | `DomSanitizer.sanitize(SecurityContext.HTML, desc)` przed renderowaniem kart | `CornucopiaCardComponent` używa `sanitizer.sanitize()` — nie `bypassSecurityTrustHtml` |
| SR-C-03 | SHA-256 każdego pliku YAML weryfikowany przez `ContentIntegrityVerifier` `@PostConstruct` | Fail-secure: `ContentIntegrityException` przy niezgodności → aplikacja nie startuje |
| SR-C-04 | `OwaspRefValidator` — allowlist identyfikatorów OWASP | Nie przyjmuje `A99:2099` ani innych dowolnych stringów |
| SR-C-05 | Rate limit Bucket4j: 60 req/min per IP na wszystkich `/api/v1/threats?suit=*` | `@RateLimiter(name="suit-browser")` na `CardSuitController` |
| SR-C-06 | Diagramy łańcuchów agentów AAI renderowane server-side jako SVG | Spring MVC endpoint `/api/v1/threats/aai/diagram/{id}` zwraca `image/svg+xml` |
| SR-C-07 | `AUTONOMY RISK` badge na kartach AAIK, AAIQ | `CornucopiaCardComponent`: `@if (card.isCritical && card.suitCode === 'AAI')` |

### 8.2 Abuse Cases — Cornucopia

| ID | Scenariusz ataku | Źródło zagrożenia | Mitigacja | Test |
|---|---|---|---|---|
| AC-09 | Bot scraping całego katalogu kart przez OAT-011 (Scraping) | Cornucopia BOT → OAT-011 | Bucket4j 60 req/min → HTTP 429 + Loki SEC-007 | `BotScrapingRateLimitIT.java` |
| AC-10 | XSS przez admin update opisu karty FRE | Cornucopia FRE4 → A03:2021 | OWASP Java HTML Sanitizer (backend) + Angular DomSanitizer (frontend) | `CardDescriptionXSSIT.java` |
| AC-11 | Modyfikacja pliku YAML kart w CI/CD | Cornucopia DVO8 → CICD-SEC-09 + A08:2021 | `ContentIntegrityVerifier` SHA-256 — aplikacja nie startuje przy mismatch | `YamlIntegrityVerifierTest.java` |
| AC-12 | Przesłanie fałszywego MITRE ATLAS ID `"T9999"` przez admin API | Cornucopia EMR → MITRE ATLAS | `MitreAtlasRefValidator` — rejects nieznane kody → 400 Bad Request | `MitreAtlasRefValidatorTest.java` |
| AC-13 | Clickjacking strony `/stride-heatmap` osadzonej w iframe atakującego | Cornucopia FREX → Client-Side C05 | `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'` | ZAP headerscan w CI |

### 8.3 YAML Integrity Pipeline — szczegółowy opis

```
YAML Integrity Pipeline — przepływ:

1. DEVELOPER tworzy PR z modyfikacją /data/cornucopia/*.yaml
   → GitHub: CODEOWNERS wymaga 2 zatwierdzeń od @security-team

2. CI Job: yaml-content-integrity (uruchamia się dla PRów dotykających /data/cornucopia/)
   ├── Krok 1: Schema validation (ajv)
   │   Schema sprawdza: id, value, desc, suits structure
   ├── Krok 2: Injection scan
   │   grep -i "<script\|javascript:\|onclick\|onerror" — fail jeśli znalezione
   ├── Krok 3: OwaspRefValidator dry-run
   │   Weryfikacja że owaspRefs w kartach są z allowlist
   └── Krok 4: Hash dry-run verification
       node scripts/hash-generator.js --dry-run — sprawdza czy hashes.json jest aktualny

3. Po MERGE do main: CI Job yaml-hash-update
   ├── node scripts/hash-generator.js --update
   ├── Aktualizuje data/hashes.json
   └── Bot commit: "chore: update YAML content hashes [skip ci]"

4. DEPLOYMENT: Spring Boot start
   @PostConstruct ContentIntegrityVerifier:
   ├── Wczytuje data/hashes.json
   ├── Oblicza SHA-256 każdego pliku YAML
   ├── Porównuje z zapisanymi hashami
   └── Jeśli niezgodność → throws ContentIntegrityException
       → aplikacja NIE startuje (fail-secure)

5. RUNTIME: Loki monitoring
   Metryka: content_integrity_check_ok = 1 (OK) / 0 (FAIL)
   Alert SEC-008: FAIL → CRITICAL → deployment blocked
```

#### ContentIntegrityVerifier.java — implementacja

```java
@Component
public class ContentIntegrityVerifier {

    private final Path hashesFile = Path.of("data/hashes.json");

    @PostConstruct
    public void verify() {
        Map<String, String> expectedHashes = loadHashes();
        expectedHashes.forEach((filename, expectedHash) -> {
            String actualHash = computeSha256(Path.of("data/cornucopia/", filename));
            if (!actualHash.equals(expectedHash)) {
                throw new ContentIntegrityException(
                    "YAML integrity check FAILED for: " + filename +
                    " — expected: " + expectedHash + " got: " + actualHash
                );
            }
        });
        log.info("Content integrity check PASSED for {} YAML files", expectedHashes.size());
        integrityCheckOk.set(1.0);  // Prometheus metric
    }
}
```

---

## 9. Faza 7 — STRIDE EoP + MLSec (Sprint 10–11, Tygodnie 19–22)

### 9.1 Wymagania bezpieczeństwa (US-15, US-16)

| ID | Wymaganie | Implementacja |
|---|---|---|
| SR-S-01 | `/stride-heatmap` wymaga JWT | Spring `@PreAuthorize("isAuthenticated()")` + Angular `AuthGuard` |
| SR-S-02 | `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'` dla `/stride-heatmap` | Nginx location block dla `/stride-heatmap` dodaje headery |
| SR-S-03 | `MitreAtlasRefValidator` — allowlist technik ATLAS | Plik `data/mitre-atlas-allowlist.json` z validowanymi kodami T0001–T9999 |
| SR-S-04 | `ML-SPECIFIC` badge na kartach EMR/EIR/EOR/EDR | `CornucopiaCardComponent`: `@if (card.edition === 'mlsec')` |
| SR-S-05 | ECharts STRIDE heatmap cachowana w Redis (TTL 5 min) | `@Cacheable(value = "stride-heatmap", key = "#componentId")` |

### 9.2 Testy: STRIDE heatmap auth

#### StrideHeatmapAuthGuard.spec.ts

```typescript
describe('StrideHeatmapAuthGuard', () => {
  it('powinien przekierować na /login bez JWT', () => {
    authService.isAuthenticated.set(false);
    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute('/stride-heatmap'), mockState)
    );
    expect(result).toEqual(router.createUrlTree(['/login']));
  });

  it('powinien zezwolić na dostęp z ważnym JWT', () => {
    authService.isAuthenticated.set(true);
    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute('/stride-heatmap'), mockState)
    );
    expect(result).toBe(true);
  });
});
```

#### StrideHeatmapControllerIT.java

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class StrideHeatmapControllerIT {

    @Test
    void getStrideHeatmap_withoutJwt_returns401() {
        given().when().get("/api/v1/stride-heatmap")
               .then().statusCode(401);
    }

    @Test
    void getStrideHeatmap_withValidJwt_returns200() {
        String jwt = generateTestJwt("user@test.com", "USER");
        given().header("Authorization", "Bearer " + jwt)
               .when().get("/api/v1/stride-heatmap")
               .then().statusCode(200)
               .body("categories", hasSize(6));
    }

    @Test
    void getStrideHeatmap_responseHasXFrameOptionsDeny() {
        String jwt = generateTestJwt("user@test.com", "USER");
        given().header("Authorization", "Bearer " + jwt)
               .when().get("/api/v1/stride-heatmap")
               .then().header("X-Frame-Options", equalTo("DENY"));
    }
}
```

---

## 10. Faza 8 — Mobile + DevOps (Sprint 12–13, Tygodnie 23–26)

### 10.1 Wymagania bezpieczeństwa (US-17, US-18)

| ID | Wymaganie | Implementacja |
|---|---|---|
| SR-M-01 | `MavsRefValidator` — allowlist OWASP MASVS 2.0 kontrolek | `data/ref-allowlists.json` → sekcja `masvs` |
| SR-M-02 | `CicdSecRefValidator` — allowlist CICD-SEC-01–10 | `data/ref-allowlists.json` → sekcja `cicdSec` |
| SR-M-03 | `OatRefValidator` — allowlist OAT-001–021 | `data/ref-allowlists.json` → sekcja `oat` |
| SR-M-04 | `BotWarningDialog` (Angular `MatDialog`) — wyświetlany przed dostępem do kart BOT | `BotSecurityComponent.ngOnInit()` → `if (!localStorage.getItem('bot_warning_ack'))` → `dialog.open(BotWarningDialogComponent)` |
| SR-M-05 | Karty DVO z przykładami CI/CD — tylko pseudokod | Code review checklist: brak działających exploitów pipeline w `codeSnippet` |
| SR-M-06 | Dogfooding: aplikacja ucząca o BOT sama implementuje obronę BOT | Bucket4j aktywny na wszystkich `/api/v1/threats?suit=BOT` endpointach |

### 10.2 Abuse Cases AC-14 – AC-18

| ID | Scenariusz ataku | Karta Cornucopia | Mitigacja | Test |
|---|---|---|---|---|
| AC-14 | Credential stuffing przez endpoint BOT | BOT suit → OAT-008 | Bucket4j 60 req/min + HTTP 429 + Loki SEC-007 | `CredentialStuffingRateLimitIT.java` |
| AC-15 | Supply chain attack przez fałszywą zależność w CI/CD | DVO suit → CICD-SEC-03 | `ContentIntegrityVerifier` + Trivy scan w CI | `CicdSupplyChainIT.java` |
| AC-16 | MASVS allowlist bypass — przesłanie `"MASVS-CUSTOM-99"` przez admin API | Mobile suit → NSX/AA | `MavsRefValidator` rejects → 400 Bad Request | `MavsRefValidatorTest.java` |
| AC-17 | Mobile SSL stripping poprzez brak weryfikacji TLS (ilustracja) | Mobile suit → NSX | Karta NSX widoczna z opisem + referencja MASVS-NETWORK-2 | E2E `us17-mobile-security.cy.ts` |
| AC-18 | Bypass `BotWarningDialog` przez bezpośrednie URL do `/threats/BOTX` | BOT suit → BOTX | `BotWarningGuard` sprawdza `localStorage.bot_warning_ack` | `BotWarningGuard.spec.ts` |

---

## 11. Faza 9 — Integracja i hardening (Sprint 14–16, Tygodnie 27–31)

### 11.1 Testy pełnej integracji

#### E2E Cypress — 18 plików testów

```
e2e/cypress/
├── us01-framework-catalog.cy.ts
├── us02-threat-filters.cy.ts
├── us03-threat-detail.cy.ts
├── us04-owasp-mitre-mapping.cy.ts
├── us05-stride-heatmap.cy.ts
├── us06-global-search.cy.ts
├── us07-export-csv.cy.ts
├── us08-mitre-kill-chain.cy.ts
├── us09-scala-code-samples.cy.ts
├── us10-lua-code-samples.cy.ts
├── us11-language-switch.cy.ts
├── us12-frontend-security.cy.ts
├── us13-llm-security.cy.ts
├── us14-agentic-ai.cy.ts
├── us15-stride-catalogue.cy.ts
├── us16-ml-security.cy.ts
├── us17-mobile-security.cy.ts
└── us18-devops-security.cy.ts
```

Każdy plik Cypress uruchamia się przeciw:
- Backend: Spring Boot z Testcontainers (PostgreSQL 16 + Redis 7)
- Frontend: `ng serve` (lub statyczny build z `ng build`)

**Dodatkowe testy cross-browser (opcjonalne):**
- Playwright: Chromium + Firefox
- Konfiguracja: `playwright.config.ts`

#### Test wydajności

```bash
# Apache Bench — 100 concurrent users, 1000 requests
ab -n 1000 -c 100 http://localhost:8080/api/v1/threats

# Cel: p95 < 200 ms dla endpointów listowych
# Cel: p95 < 500 ms dla endpointów full-text search
# Cel: p95 < 1000 ms dla eksportu PDF (do 50 zagrożeń)
```

### 11.2 DAST — OWASP ZAP

#### Konfiguracja ZAP full active scan w CI

```
CI Job: dast-scan (uruchamia się na staging, po deploy)

Kroki:
1. docker compose up -d (backend + frontend + postgres + redis)
2. Poczekaj na health check: GET /api/v1/actuator/health → 200
3. ZAP authentication (JWT dla endpointów chronionych)
4. ZAP spider: crawl wszystkich /api/v1/* endpoints
5. ZAP active scan: wszystkie odkryte endpointy
6. Sprawdź raport: 0 High/Critical → pass; 1+ → fail
7. Upload ZAP HTML + JSON report jako CI artifact
8. Znane false positives: zap-suppressions.json

Zakres skanowania:
  /api/v1/threats/**
  /api/v1/frameworks/**
  /api/v1/search
  /api/v1/stride-heatmap (z JWT)
  /api/v1/admin/** (z JWT ADMIN)
  /api/v1/export/**
  Wszystkie Angular SPA routes (przez ZAP Ajax Spider)
```

#### zap-suppressions.json — przykłady false positives

```json
{
  "suppressions": [
    {
      "ruleId": "10038",
      "name": "Content Security Policy (CSP) Header Not Set",
      "reason": "CSP ustawiane przez Nginx dla tras Angular SPA, nie przez Spring Boot — ZAP skanuje port 8080 bezpośrednio",
      "expiresOn": "2027-01-01"
    }
  ]
}
```

### 11.3 Accessibility audit (WCAG 2.1 AA)

#### cypress-axe integracja

```typescript
// support/commands.ts
import 'cypress-axe';

// Każdy plik E2E:
beforeEach(() => {
  cy.injectAxe();
});

afterEach(() => {
  cy.checkA11y(undefined, {
    runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa'] },
    includedImpacts: ['critical', 'serious']
  });
});
```

#### Wymagania WCAG 2.1 AA dla komponentów Angular Material

| Komponent | Wymaganie | Implementacja |
|---|---|---|
| `mat-table` | `aria-label` + `aria-sort` dla nagłówków | `<mat-header-cell aria-label="Zagrożenie" aria-sort="ascending">` |
| `mat-dialog` | Focus trap + `aria-modal="true"` | Angular CDK `FocusTrap` — wbudowane w `MatDialog` |
| `LanguageToggle` | `aria-label` dla przycisku | `<button aria-label="Zmień język / Change language">` |
| `mat-chip` (ATTACK_DEMO badge) | Kontrast ≥ 4.5:1 | Angular Material `warn` theme — czerwony na białym: 5.1:1 ✓ |
| Karta Cornucopia | Opis dostępny przez `aria-description` | `<mat-card aria-description="{{ card.descriptionPl }}">` |
| Nawigacja | Skip link | `<a href="#main-content" class="skip-link">Przejdź do treści</a>` |

### 11.4 Monitoring i alerting w produkcji

#### Loki — alert rules

| Alert ID | Warunek | Severity | Akcja |
|---|---|---|---|
| SEC-007 | > 5 HTTP 429 responses/min z jednego IP | HIGH | PagerDuty notification + IP logging |
| SEC-008 | `ContentIntegrityVerifier` FAIL przy starcie | CRITICAL | Deployment blocked + natychmiastowy alert |
| SEC-009 | `AuthenticationFailureEvent` > 10/min | MEDIUM | Alert do Security team — potencjalny brute-force |
| SEC-010 | `MitreAtlasRefValidator` reject > 5/min | MEDIUM | Alert — potencjalny content poisoning attempt |
| SEC-011 | `BotWarningDialog` bypass attempts > 10/h | LOW | Log + analiza |

#### Prometheus metrics

| Metryka | Typ | Opis |
|---|---|---|
| `content_integrity_check_ok` | Gauge | 1 = wszystkie pliki YAML OK, 0 = mismatch |
| `rate_limit_rejections_total` | Counter | Łączna liczba odrzuconych żądań Bucket4j per IP |
| `threat_api_request_duration_seconds` | Histogram | Czas odpowiedzi API zagrożeń (p50, p95, p99) |
| `yaml_validator_rejections_total` | Counter | Liczba odrzuconych *RefValidator requestów per validator type |
| `bot_warning_dialogs_shown_total` | Counter | Liczba wyświetleń BotWarningDialog |

#### SLO (Service Level Objectives)

| SLO | Cel | Metryka |
|---|---|---|
| API latency p95 | < 200 ms | `threat_api_request_duration_seconds` |
| Content integrity | 100% uptime | `content_integrity_check_ok` = 1 zawsze |
| Rate limit accuracy | 60 ± 2 req/min per IP | `rate_limit_rejections_total` / czas |
| YAML validation | 0 fałszywych negatywów | `yaml_validator_rejections_total` |

---

## 12. Tabela mapowania: US → Wymagania bezpieczeństwa → Abuse Cases → Kontrole

| US | Zagrożenie główne | OWASP/STRIDE ref | Abuse Case | Kontrola techniczna | Test |
|---|---|---|---|---|---|
| US-01 | Nieuprawniony dostęp do frameworks | A01:2021 | — | Spring Security public endpoint | `FrameworkControllerIT` |
| US-02 | SQL Injection przez filtry | A03:2021 | AC-01 | JPA named parameters, Bean Validation | `ThreatFilterSQLInjectionTest` |
| US-03 | XSS w szczegółach zagrożenia | A03:2021 | AC-04 | Angular interpolation auto-escape, DomSanitizer | `ThreatDetailXSSTest.spec.ts` |
| US-04 | Manipulacja danymi mapowania OWASP–MITRE | T — Tampering | — | Dane tylko w DB (nie w klientach), JPA read-only dla publicznych | `CrossReferenceControllerIT` |
| US-05 | Clickjacking heatmapy STRIDE | FREX → C05 | AC-13 | X-Frame-Options: DENY, CSP frame-ancestors | ZAP headerscan |
| US-06 | ReDoS / info harvesting przez wyszukiwarkę | D — DoS | AC-06, AC-07 | `plainto_tsquery`, `@Size(max=200)`, rate limit | `SearchRateLimitIT` |
| US-07 | CSV Injection w eksporcie | A03:2021 | AC-08 | Apache Commons CSV quote-all, formula prefix | `CsvExportInjectionTest` |
| US-08 | XSS przez SVG w MITRE timeline | A03:2021 | — | SVG renderowane server-side, brak inline JS | `MitreTimelineControllerIT` |
| US-09 | Wykonanie złośliwego kodu Scala | STRIDE E — Elevation | SR-CODE-02 | Brak ScriptEngine, tylko text rendering | `CodeSamplePanelComponent.spec.ts` |
| US-10 | Wykonanie złośliwego kodu Lua | STRIDE E — Elevation | SR-CODE-02 | Brak exec(), tylko text rendering | `CodeSamplePanelComponent.spec.ts` |
| US-11 | Manipulacja locale przez Accept-Language | T — Tampering | — | `LocaleInterceptor` allowlist {'pl','en'} | `LocaleInterceptorTest` |
| US-12 | XSS przez opis karty FRE | FRE4 → A03:2021 | AC-10 | Angular DomSanitizer + OWASP Java HTML Sanitizer | `CardDescriptionXSSIT` |
| US-13 | Prompt injection w opisach kart LLM | LLM suit → LLM01 | — | DomSanitizer, plain text only w descriptionPl | `LlmCardRenderTest.spec.ts` |
| US-14 | Fałszywy AgentAI ID przez admin | AAI suit → allowlist | AC-12 | `OwaspRefValidator` allowlist | `OwaspRefValidatorTest` |
| US-15 | Clickjacking /stride-heatmap | FREX → C05 | AC-13 | X-Frame-Options: DENY | `StrideHeatmapControllerIT` |
| US-16 | Fałszywy MITRE ATLAS ID | EMR → MITRE | AC-12 | `MitreAtlasRefValidator` | `MitreAtlasRefValidatorTest` |
| US-17 | Fałszywy MASVS ID przez admin | NS/AA → MASVS | AC-16 | `MavsRefValidator` | `MavsRefValidatorTest` |
| US-18 | Bot scraping + BotWarningDialog bypass | BOT → OAT-011 | AC-09, AC-18 | Bucket4j 60 req/min, `bot_warning_ack` guard | `BotScrapingRateLimitIT`, `BotWarningGuard.spec.ts` |

---

## 13. Checklista compliance SSDLC

### Faza 0 — Requirements & Threat Modeling

- [ ] STRIDE threat model dla ThreatView 2026 ukończony i zaakceptowany przez Security Lead
- [ ] Kryteria bezpieczeństwa SAC-01–SAC-15 zdefiniowane i włączone do DoD wszystkich US
- [ ] CODEOWNERS skonfigurowane dla `/data/cornucopia/` — @security-team, 2 approvals wymagane
- [ ] Risk Register zainicjowany z 12 zidentyfikowanymi zagrożeniami (tabela sekcja 2.2)
- [ ] OWASP SAMM assessment przeprowadzony — docelowe poziomy dojrzałości określone
- [ ] Warsztaty z interesariuszami przeprowadzone — Security Acceptance Criteria zebrane

### Faza 1 — Foundation

- [ ] Angular 18 strict mode włączony (`strict: true`, `strictTemplates: true` w `tsconfig.json`)
- [ ] Spring Security 6 stateless JWT skonfigurowany (`SESSIONLESS`, RS256)
- [ ] Flyway migracje chronione — checksums włączone
- [ ] Git pre-commit hooks skonfigurowane: ESLint security + SpotBugs + gitleaks
- [ ] CODEOWNERS plik stworzony i aktywny
- [ ] Branch protection rules na main i develop
- [ ] CI pipeline z SAST aktywny — lint-and-sast job GREEN
- [ ] Docker Compose secrets dla DB i JWT kluczy (nie environment variables)
- [ ] Spring Actuator — eksponuje tylko `/health` i `/metrics`
- [ ] SecurityConfig — X-Frame-Options: DENY, HSTS, CSP header

### Faza 2–4 — Development

- [ ] Wszystkie JPA queries parameterized — brak string concatenation w `@Query`
- [ ] Bean validation (`@Valid`) na wszystkich DTO endpointach z `@RestController`
- [ ] `GlobalExceptionHandler` — generyczne komunikaty błędów w produkcji (brak stack traces)
- [ ] `StrideCategoryValidator` — allowlist S,T,R,I,D,E
- [ ] Angular `DomSanitizer` w każdym komponencie renderującym `descriptionPl`/`descriptionEn`
- [ ] Angular `AuthGuard` chroni trasy wymagające autentykacji
- [ ] `mat-table` z `trackBy` — brak niekontrolowanych re-renderów
- [ ] CSV export z quote-all + formula prefix
- [ ] `ab` load test: p95 < 200 ms dla `/api/v1/threats`

### Faza 5 — i18n

- [ ] Klucze i18n nie zawierają HTML tags — weryfikacja w CI (`validate-i18n.js`)
- [ ] `LocaleInterceptor` sanityzuje `Accept-Language` — allowlist `{'pl', 'en'}`
- [ ] `I18nParityTest.spec.ts` weryfikuje parzystość kluczy pl.json i en.json w CI
- [ ] `codeSnippet` pola wyłączone z tłumaczenia

### Faza 6–8 — Cornucopia

- [ ] `ContentIntegrityVerifier` aktywny — fail-secure przy SHA-256 mismatch
- [ ] `OwaspRefValidator` aktywny — allowlist identyfikatorów OWASP
- [ ] `MitreAtlasRefValidator` aktywny — allowlist technik MITRE ATLAS
- [ ] `MavsRefValidator` aktywny — allowlist OWASP MASVS 2.0
- [ ] `CicdSecRefValidator` aktywny — allowlist CICD-SEC-01–10
- [ ] `OatRefValidator` aktywny — allowlist OAT-001–021
- [ ] Rate limiting Bucket4j 60 req/min per IP na `/api/v1/threats?suit=*`
- [ ] `BotWarningDialog` wyświetlany przed kartami BOT — `bot_warning_ack` w localStorage
- [ ] `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'` na `/stride-heatmap`
- [ ] `ML-SPECIFIC` badge na kartach EMR/EIR/EOR/EDR
- [ ] `AUTONOMY RISK` badge na kartach AAIK/AAIQ
- [ ] CI job `yaml-content-integrity` aktywny dla PRów do `/data/cornucopia/`
- [ ] SVG diagramy AAI renderowane server-side (nie client-side JS)
- [ ] DVO karty — tylko pseudokod w próbkach CI/CD (bez działających exploitów)

### Faza 9 — Hardening

- [ ] ZAP full active scan na staging: 0 High/Critical alerts
- [ ] OWASP Dependency Check: 0 Critical CVEs (CVSS < 7.0)
- [ ] `npm audit --audit-level=high`: 0 High/Critical
- [ ] Trivy: 0 CRITICAL CVE w obrazach Docker
- [ ] axe-core (cypress-axe): 0 Critical/Serious WCAG 2.1 AA violations
- [ ] Backend coverage ≥ 80% (JaCoCo report w CI)
- [ ] Frontend coverage ≥ 75% (Jest coverage report w CI)
- [ ] Wszystkie 18 E2E Cypress tests: GREEN w CI
- [ ] Wszystkie abuse cases AC-01–AC-18: GREEN w CI
- [ ] Monitoring aktywny: Loki alerts SEC-007, SEC-008, SEC-009, SEC-010
- [ ] Prometheus metryki `content_integrity_check_ok` i `rate_limit_rejections_total` aktywne
- [ ] SLO zdefiniowane i monitorowane: API latency p95 < 200 ms
- [ ] README z instrukcją uruchomienia (`docker compose up`) i podsumowaniem bezpieczeństwa
- [ ] SDLC_analysis.md zaktualizowany po każdym sprincie
