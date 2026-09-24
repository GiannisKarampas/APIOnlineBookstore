# Online Bookstore — API Test Automation

[![API Tests](https://github.com/GiannisKarampas/APIOnlineBookstore/actions/workflows/api-tests.yml/badge.svg)](https://github.com/GiannisKarampas/APIOnlineBookstore/actions/workflows/api-tests.yml)

Automated test suite for the [FakeRestAPI](https://fakerestapi.azurewebsites.net/index.html)
Online Bookstore service, covering the **Books** endpoints in full and the **Authors**
endpoints as the bonus extension.

| | |
|---|---|
| **Language / build** | Java 21, Maven |
| **Test framework** | TestNG 7.9 |
| **HTTP client** | REST Assured 5.4 |
| **Reporting** | Allure 2.26 + a self-contained HTML/Markdown summary |
| **Contract** | Responses validated against the API's published OpenAPI document |
| **CI** | GitHub Actions (`.github/workflows/api-tests.yml`) and a `Jenkinsfile` |
| **Coverage** | 123 API and contract cases, plus 35 framework cases — 158 cases from 92 test methods |

**Contents** — [Quick start](#quick-start) · [Running the tests](#running-the-tests) ·
[Reports](#reports) · [How the project is organised](#how-the-project-is-organised) ·
[Traceability](#traceability) · [Test coverage](#test-coverage) · [Contract testing](#contract-testing) ·
[Extending the framework](#extending-the-framework) ·
[CI/CD](#cicd) · [Limitations](#limitations) · [Troubleshooting](#troubleshooting) ·
[Notes on some decisions](#notes-on-some-decisions)

---

## Quick start

**Requirements:** **JDK 21 or newer. That is all.** Maven does not need to be
installed — the repository ships the Maven Wrapper, so `./mvnw` fetches the pinned
Maven version on first use. The Allure command line tool is downloaded by the build
when it is first needed. The API under test is public, so there are no credentials,
no local services and no `.env` to configure.

If your JDK is too old the build says so in its first five seconds, by name and
version, rather than failing later with a confusing compile error.

The suite runs against the **live public API**, so an outbound internet connection
is required; there is no recorded or mocked mode.

From the root of the repository:

```bash
# macOS / Linux
./run-tests.sh                        # full suite against the dev environment
open test-results/summary/index.html  # the report

# Windows
mvnw.cmd clean test -Pdev
start test-results\summary\index.html
```

`./run-tests.sh` runs 35 framework cases as a gate, then 123 against the live API,
and generates the reports. The saved September 23 run records 0.8 seconds for the
framework tests and 12.2 seconds for the API tests; build and report generation add
time, and live API latency varies.

---

## Running the tests

Everything the script does is plain Maven:

```bash
./mvnw clean test                                                  # FULL_RUN, dev environment
./mvnw clean test -Dsuite.xml=src/test/resources/test-suites/SMOKE_RUN.xml
./mvnw clean test -Dgroups=smoke                                   # by TestNG group
./mvnw clean test -Dtest=TC_API_BOOKS_02_GetBookById               # a single class
```

`mvn` works just as well if you have it installed; `./mvnw` is used throughout so
that everyone — and CI — runs the same Maven version.

`./run-tests.sh [SUITE]` wraps the same commands and always generates both reports
afterwards, e.g. `./run-tests.sh SMOKE_RUN`.

### Suites

| Suite | What it runs | When to use it |
|---|---|---|
| `FRAMEWORK` | 35 framework cases, no network | The gate in front of everything else |
| `FULL_RUN` | 123 API and contract cases; excludes framework tests | The default; CI nightly |
| `SMOKE_RUN` | 10 cases in the `smoke` group | Gating a deployment |
| `BOOKS_ONLY` | 63 Books cases | The mandatory scope of the assessment on its own |

### Groups

`smoke`, `regression`, `happy-path`, `edge-case`, `books`, `authors`, `contract`,
`framework`, `provider-behaviour` — declared as
constants in [`TestGroups`](src/main/java/utils/TestGroups.java) so a typo fails at
compile time rather than silently running nothing.

### Running the lanes yourself

```bash
S=src/test/resources/test-suites

./mvnw test -Dsuite.xml=$S/FRAMEWORK.xml      # 35 cases, no network
./mvnw test -Dsuite.xml=$S/SMOKE_RUN.xml      # 10 cases, the merge gate
./mvnw test -DexcludedGroups=provider-behaviour   # 105 cases, the main-branch regression
./mvnw test                                   # 123 cases, every API and contract case
./run-tests.sh                                # the framework gate, then the API suite
```

The framework tests live in their own suite rather than inside `FULL_RUN`. They are a
different kind of check — no network, and a different meaning when they fail — and
listing them in both meant they ran twice on every push to `main`, which left a red
API job ambiguous about what had actually broken.

### Running without the provider-characterisation tests

`provider-behaviour` marks the tests that pin what this API does today rather than
what it owes anyone — the writes that do not persist, the contract that omits its
error responses, the values accepted without validation. A failure there means the
provider changed something, possibly for the better.

To gate a pipeline on regressions alone:

```bash
./mvnw clean test -DexcludedGroups=provider-behaviour
```

The property is case-sensitive and Surefire ignores an unrecognised one silently, so
a misspelling runs the full suite while looking like it excluded something. 123 cases
with the group, 105 without — if the count does not drop, the flag did not take.

### Environments

Each environment is one properties file under `src/main/resources/config/`, selected
by the matching Maven profile:

```properties
rest.url=https://fakerestapi.azurewebsites.net
retry=3
```

There is exactly one, `dev`, because there is exactly one instance to point at: the
public sandbox. Earlier versions of this suite also carried `sit`, `uat`, `preprod`
and a `local` profile aimed at `http://localhost:8080`; they demonstrated a pattern at
the cost of pretending to environments that do not exist, and FakeRestAPI publishes
neither source nor an image anyone could host. Adding a real one is one properties
file and one Maven profile, and no code change.

For an instance whose address is not known in advance — a review app, an ephemeral
deployment — `-DbaseUrl` overrides the profile without adding one:

```bash
./mvnw clean test -DbaseUrl=https://bookstore-pr-421.example.com
```

A profile also carries its own connect and socket timeouts, which is what a profile
buys over a bare `-DbaseUrl`: an environment is a set of behaviours, not just an
address. Certificates must validate everywhere — nothing in the suite relaxes TLS,
because a suite that accepts any certificate cannot tell a correctly served API from
one behind a broken or substituted one.

---

## Reports

The execution summary is written by every run, including a failed one. The Allure
report is rendered by a separate command — `./mvnw test` leaves raw results in
`test-results/allure-results`, which is what `./run-tests.sh` and CI then render.

**1. Execution summary** — `test-results/summary/index.html`

Written by [`SummaryReporter`](src/main/java/utils/listeners/SummaryReporter.java).
A single self-contained page listing every test case with its status, data set,
duration and failure message. It opens straight from disk, needs no tooling, and its
Markdown twin (`summary.md`) is what CI folds into the run page. A copy of the latest
run is committed under [`reports/`](reports/).

**2. Allure report** — `allure-report/index.html`, published from `main` at
**<https://gianniskarampas.github.io/APIOnlineBookstore/>**

```bash
./mvnw io.qameta.allure:allure-maven:report   # generate
./mvnw allure:serve                           # generate and open in a browser
```

Every request and response is attached, and every test reads as the sequence of named
steps the test declared. Results are grouped by **Epic → Feature → Story** (the
resource and the endpoint) and carry a severity: `CRITICAL` for the smoke checks,
`NORMAL` for the remaining happy paths, `MINOR` for the edge cases. Severity is
declared per test with `@Severity`; it was chosen to line up with each test's groups,
but nothing enforces that, so the two can drift if someone changes one and not the
other.

---

## How the project is organised

```
src/main/java/                        the framework
├── base/BookstoreTest.java           what every test extends: step(...) + a service
├── domain/RestEndpointEnum.java      every URL, declared once
├── models/errors/                    the RFC 7807 problem document
├── services/
│   ├── ApiService.java               what every service shares: rest + validate(...)
│   ├── contract/                     ApiContract · ContractService
│   └── rest/
│       ├── RestCommonValidations.java assertions valid for any response
│       ├── books/                    BookDTO · BooksService · BookAssertions
│       └── authors/                  AuthorDTO · AuthorsService · AuthorAssertions
└── utils/
    ├── common/                       Json · Retry · TransientFailures ·
    │                                 UnexpectedStatusException
    ├── config/                       environment resolution
    ├── data/                         BookFactory · AuthorFactory ·
    │                                 SeededCatalogue · DataSeed
    ├── factories/                    hands out the resource services
    ├── listeners/                    logging · retry · the summary report
    │   └── report/                   TestCaseResult · RunTotals
    └── service/                      the REST transport

src/main/resources/
└── contracts/                        the committed OpenAPI snapshot

src/test/java/                        the tests
├── TS_FRAMEWORK/                     the framework's own tests, no network
├── TS_API_Books/                     TC_API_BOOKS_01..05
├── TS_API_Authors/                   TC_API_AUTHORS_01..05
└── TS_API_Contract/                  TC_API_CONTRACT_01

src/test/resources/test-suites/       FRAMEWORK · FULL_RUN · SMOKE_RUN · BOOKS_ONLY
```

### The layers, and why there are exactly these

| Layer | Responsibility | Depends on |
|---|---|---|
| **Test** | States a scenario and what should be true | Service + assertions |
| **Service** (`BooksService`) | One method per API operation. No assertions. Extends `ApiService`, which holds the transport and the `validate(...)` all three services share | Transport + endpoints |
| **Assertions** (`BookAssertions`) | What this suite expects of a *book from the seeded catalogue* — distinct from schema validity, which the contract check covers | The DTO only |
| **Validations** (`RestCommonValidations`) | What makes any *response* valid | The transport |
| **Transport** (`Rest`) | Builds and sends requests, records the response | Configuration |
| **Configuration** | Which environment, which URL | Nothing |

Adding an endpoint touches one enum constant and one service method, followed by its
tests. Adding a resource also needs factory wiring, a test-facing accessor and suite
registration; see [Extending the framework](#extending-the-framework).

The split between `RestCommonValidations` and `BookAssertions` is the same idea from
the other direction: "a 200 with a JSON body" and "a book with a positive id" are
different kinds of truth that change for different reasons.

### How a test reads

```java
@Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
        description = "An existing book is returned in full")
public void anExistingBookIsReturned() {
    books("Request the book with id 1").getBookById(1);

    BookDTO book = books("Verify a well-formed book is returned as JSON").validate(checks -> checks
            .verifyStatusCode(SC_OK)
            .verifyContentTypeIsJson()
            .verifyMatchesContract()
            .as(BookDTO.class));

    BookAssertions.assertMeetsCatalogueExpectations(book);
}
```

`books("...")` names the step and returns the service to act on, so the body of a test
is a list of intentions. The checks are **handed in** rather than chained off a
returned object, because the step has to still be open while they run — a step that
closes first reports as passed beside a test that failed, which is worse than no step
at all. In Allure this reads as:

```
▸ Request the book with id 1                     240ms   [Request] [Response]
▸ Verify a well-formed book is returned as JSON     2ms
    ↳ Verify the status code is 200                      ✓
    ↳ Verify the content type is JSON                    ✓
    ↳ Verify the response matches the published contract  ✗
```

---

## Testing the framework itself

35 cases never open a socket. They cover request isolation, the retry
policy and attempt limit, execution context, summary aggregation, thread isolation
and schema validation with valid and malformed responses
([`TS_FRAMEWORK`](src/test/java/TS_FRAMEWORK)).

They exist because of a specific lesson. Three defects — a content type that leaked
into subsequent requests, a step description written to an object nobody read, and a
retry policy that re-ran deterministic failures — all survived a full green run of the
API suite, because nothing exercised those paths. Every one of them now has a test
that fails if it comes back.

They have their own suite,
[`FRAMEWORK.xml`](src/test/resources/test-suites/FRAMEWORK.xml), and their own CI job,
which the API job depends on. `./run-tests.sh` runs them the same way: if they fail it
stops there, because an API result gathered with a broken transport is measuring the
wrong thing.

---

## Code quality gates

Three checks run in the `validate` phase, before a single test starts, so a style
problem and a failing suite never arrive as the same red build:

| Gate | What it enforces | Fix it with |
|---|---|---|
| **Enforcer** | JDK 21+, Maven 3.8+ | Point `JAVA_HOME` at a supported JDK |
| **Spotless** | Import order, no unused imports, no trailing whitespace, final newline, 4-space indent | `./mvnw spotless:apply` |
| **Checkstyle** | [`checkstyle.xml`](checkstyle.xml) — naming, likely defects, structure, and Javadoc on every public type (`MissingJavadocType` requires it, `JavadocType` validates it) | By hand; the message names the rule |

```bash
./mvnw spotless:check checkstyle:check   # what CI runs
./mvnw spotless:apply                    # fix formatting automatically
```

The Checkstyle rule set is written for this project rather than imported wholesale.
Google's and Sun's bundled configs encode choices this codebase does not make
(two-space indent, an 80-column limit, Javadoc on every accessor), so adopting either
would have meant thousands of suppressions, which teaches a reader nothing. The rules
that are there catch real defects, and **every one of them passes**. There are two
[suppressions](checkstyle-suppressions.xml), both scoped to test sources: the
`TS_`/`TC_` traceability identifiers in test names, and the Javadoc requirement —
tests are documented by their `@Test` descriptions, which is what the report renders.

---

## Contract testing

The API publishes an OpenAPI 3.0.1 document at `/swagger/v1/swagger.json`. Responses
on the documented read operations are validated against it, so the suite checks the
whole shape of a payload rather than only the fields someone thought to assert on:

```java
books("Verify a well-formed book is returned as JSON").validate(checks -> checks
        .verifyStatusCode(SC_OK)
        .verifyContentTypeIsJson()
        .verifyMatchesContract()      // every field, its type, its nullability
        .as(BookDTO.class));
```

This catches what hand-written assertions structurally cannot. A `pageCount` that
quietly becomes a quoted `"100"` still deserializes into an `Integer`, and an
undocumented new field is silently dropped by the DTO — both pass every other
assertion in this suite, and both fail the contract check.

The document is a **committed snapshot**
([`bookstore-openapi.json`](src/main/resources/contracts/bookstore-openapi.json)),
not a live fetch: a run should not depend on the provider's docs endpoint, and a
change to the contract should arrive as a reviewable diff rather than silently
altering what the suite asserts. `theSnapshotMatchesThePublishedDocument` fails when
the two drift apart, naming the sections that moved, so the snapshot cannot go stale
unnoticed.

### Two findings about the published document

Contract validation immediately turned up two places where FakeRestAPI's own document
does not describe FakeRestAPI:

| Finding | Evidence |
|---|---|
| **No error responses are documented.** Every operation declares `200` and nothing else, although the API returns `404` for an unknown id and `400` for an unbindable one. | `Response status 404 not defined for path '/api/v1/Books/{id}'.` |
| **Books writes declare no response body, but return one.** `POST /Books` and `PUT /Books/{id}` are documented as `200: {description: Success}` with no content, yet both echo the submitted book back. | `No response body is expected but one was found.` |

The gaps are asserted using a Books GET 404 and a Books POST response in
[`TC_API_CONTRACT_01_OpenApiContract`](src/test/java/TS_API_Contract/TC_API_CONTRACT_01_OpenApiContract.java),
the same way the statelessness of the API is. They are findings about the provider's
documentation, not defects in this suite — and the day the provider corrects the
document, those tests go red and name the assertion to relax. `POST /Books` and
`PUT /Books/{id}` therefore carry no contract check in their own tests, with a comment
pointing here.

---

## Extending the framework

**Adding a case to an endpoint already covered** — add a `@Test` to the matching
class, or a row to its `@DataProvider`. The suites already include that class, so no
suite change is needed. A new class must also be registered in `FULL_RUN.xml`.

**Adding an endpoint to a resource already covered** — two edits:

```java
// 1. domain/RestEndpointEnum.java  — this API really does expose this one
AUTHORS_BY_BOOK(AUTHORS.getPath() + "/authors/books/{idBook}"),

// 2. services/rest/authors/AuthorsService.java
public Response getAuthorsByBook(int bookId) {
    return rest.getRequestWithPathParams(AUTHORS_BY_BOOK, "", "idBook", bookId);
}
```

(`GET /api/v1/Authors/authors/books/{idBook}` is documented by the API but sits outside
the assessment's list of endpoints, which is why it is not covered here.)

**Adding a whole resource** — a new package, plus edits to four existing files. The
compiler catches most of it; the suite registration it will not:

```
services/rest/users/
├── UserDTO.java          the payload
├── UsersService.java     one method per operation, no assertions
└── UserAssertions.java   what this suite expects of a user
```

| Step | File | Caught by the compiler? |
|---|---|---|
| 1. Declare the endpoints | `domain/RestEndpointEnum.java` | — |
| 2. Add the accessor | `utils/factories/interfaces/IRestServiceFactory.java` | — |
| 3. **Construct and return it** | `utils/factories/RestServiceObjectFactory.java` | Yes — the interface will not be satisfied without it |
| 4. Add the test-facing accessor | `base/BookstoreTest.java` | Yes, at the first call site |
| 5. **Register the test class** | `src/test/resources/test-suites/FULL_RUN.xml` | **No.** `FULL_RUN` lists classes explicitly, so an unregistered class runs nowhere and nothing says so |

Step 5 is the one that bites. `SMOKE_RUN` and `BOOKS_ONLY` select by package, so a new
class in an existing package is picked up there automatically — but `FULL_RUN`, the
default, will not see it.

**If a test needs a seeded record it will write to**, reserve an id for it in
[`SeededCatalogue`](src/main/java/utils/data/SeededCatalogue.java) rather than
hard-coding one — see [Notes on some decisions](#notes-on-some-decisions).

---

## Traceability

Every endpoint the assessment names, and where it is tested.

| # | Endpoint | Test class |
|---|---|---|
| 1 | `GET /api/v1/Books` | [`TC_API_BOOKS_01_GetAllBooks`](src/test/java/TS_API_Books/TC_API_BOOKS_01_GetAllBooks.java) |
| 2 | `GET /api/v1/Books/{id}` | [`TC_API_BOOKS_02_GetBookById`](src/test/java/TS_API_Books/TC_API_BOOKS_02_GetBookById.java) |
| 3 | `POST /api/v1/Books` | [`TC_API_BOOKS_03_CreateBook`](src/test/java/TS_API_Books/TC_API_BOOKS_03_CreateBook.java) |
| 4 | `PUT /api/v1/Books/{id}` | [`TC_API_BOOKS_04_UpdateBook`](src/test/java/TS_API_Books/TC_API_BOOKS_04_UpdateBook.java) |
| 5 | `DELETE /api/v1/Books/{id}` | [`TC_API_BOOKS_05_DeleteBook`](src/test/java/TS_API_Books/TC_API_BOOKS_05_DeleteBook.java) |
| 6 | `GET /api/v1/Authors` *(bonus)* | [`TC_API_AUTHORS_01_GetAllAuthors`](src/test/java/TS_API_Authors/TC_API_AUTHORS_01_GetAllAuthors.java) |
| 7 | `GET /api/v1/Authors/{id}` *(bonus)* | [`TC_API_AUTHORS_02_GetAuthorById`](src/test/java/TS_API_Authors/TC_API_AUTHORS_02_GetAuthorById.java) |
| 8 | `POST /api/v1/Authors` *(bonus)* | [`TC_API_AUTHORS_03_CreateAuthor`](src/test/java/TS_API_Authors/TC_API_AUTHORS_03_CreateAuthor.java) |
| 9 | `PUT /api/v1/Authors/{id}` *(bonus)* | [`TC_API_AUTHORS_04_UpdateAuthor`](src/test/java/TS_API_Authors/TC_API_AUTHORS_04_UpdateAuthor.java) |
| 10 | `DELETE /api/v1/Authors/{id}` *(bonus)* | [`TC_API_AUTHORS_05_DeleteAuthor`](src/test/java/TS_API_Authors/TC_API_AUTHORS_05_DeleteAuthor.java) |

And the assessment's other requirements:

| Requirement | Where |
|---|---|
| Project structure | [How the project is organised](#how-the-project-is-organised) |
| Happy paths and edge cases | [Test coverage](#test-coverage) |
| Test report | [`reports/`](reports/), generated by [`SummaryReporter`](src/main/java/utils/listeners/SummaryReporter.java) |
| CI/CD pipeline | [`.github/workflows/api-tests.yml`](.github/workflows/api-tests.yml), [`Jenkinsfile`](Jenkinsfile) |
| Code quality | [Code quality gates](#code-quality-gates) |

---

## Test coverage

Cases count individual data-provider rows as separate executions. They are not a
code-coverage percentage or a count of assertions.

| Folder | Test methods | Cases | What the scenarios protect |
|---|---|---|---|
| [`TS_API_Books`](src/test/java/TS_API_Books) | 33 | 63 | Catalogue reads, identity lookup, payload handling and write acknowledgments |
| [`TS_API_Authors`](src/test/java/TS_API_Authors) | 33 | 56 | Author operations and the relationship between authors and books |
| [`TS_API_Contract`](src/test/java/TS_API_Contract) | 4 | 4 | Snapshot freshness, response schema conformance and known documentation gaps |
| [`TS_FRAMEWORK`](src/test/java/TS_FRAMEWORK) | 22 | 35 | Request isolation, retry limits, execution context, reporting, thread separation and schema validation |
| **Total** | **92** | **158** | **123 API/contract cases and 35 framework cases** |

### Books

| Endpoint | Happy path | Edge cases |
|---|---|---|
| `GET /Books` | Returns JSON matching the contract, every book well-formed, within a response-time budget; at least 200 records including IDs 1, 50, 100 and 200 | IDs are unique; two nonempty consecutive reads return the same IDs; collection `PATCH` → 405 |
| `GET /Books/{id}` | IDs 1, 50, 100, 200 each return their own book | `0`, `-1`, `201`, `9999`, `MAX_VALUE` → 404; `abc`, `1.5`, a space, `2147483648` → 400 naming `id`; item `PATCH` → 405 |
| `POST /Books` | A valid book is accepted and echoed unchanged | Wrong field types and individually null `id`, `pageCount`, `publishDate` → 400; empty body, truncated JSON and a bare string → 400; text/plain and XML → 415; 5,000-character text, Unicode and escaping survive; a non-UTC date retains its instant; defaults, negative page count acceptance and absent persistence are characterised |
| `PUT /Books/{id}` | An update is acknowledged and echoed back | Non-integer IDs, wrong page-count type, individually null numeric/date fields and unusable bodies → 400; text/plain and XML → 415; unknown IDs are accepted; the body's ID wins over the path's; the update is not persisted |
| `DELETE /Books/{id}` | Acknowledged with an empty body matching the contract | An unknown ID is acknowledged too; non-integer ID → 400; the deleted ID still returns the same book identity |

Boundary values distinguish a valid integer with no matching record from an ID that
cannot bind to an integer. Null-field cases isolate one invalid field at a time, so
one rejection cannot conceal missing validation on another field. Long text checks
truncation; fixed CJK, emoji, right-to-left, quote, backslash and whitespace inputs
check encoding and escaping. These are representative inputs, not an exhaustive
search for payload limits.

### Framework

| Check | Covered |
|---|---|
| Request isolation | A content-type override does not leak into the next request; each call gets its own specification |
| Retry policy | 14 failure shapes, including 502; wrapped timeouts; the analyzer stops at the configured attempt budget and refuses a deterministic failure |
| Execution context | The service and transport share a context; a step description is delivered once then cleared; an absent description is handled |
| Execution summary | Fail-then-pass reports as flaky and keeps the evidence; elapsed and cumulative time are distinguished |
| Thread isolation | Two threads receive different services and contexts; a request path recorded on one is absent on the other |
| Contract validation | An in-memory malformed response produces violations; a valid response produces none |

These cases protect the reliability of the test results themselves. The thread tests
exercise separation between threads, not cleanup when a worker thread is reused.
The schema tests exercise violation collection; they do not yet assert that
`verifyMatchesContract()` throws for a malformed response.

### Contract

| Check | Covered |
|---|---|
| Snapshot freshness | The committed snapshot still matches the published document **where this suite relies on it** — the Books and Authors operations and the Book and Author schemas. A change to Users or Activities is none of the bookstore's business |
| Schema conformance | A documented operation is validated against its declared schema, not merely its status |
| Documentation gaps | The two findings above, asserted by name |

### Authors (bonus)

One class per endpoint, 56 cases. Authors share the core validation scenarios with
Books and add relationship checks. The fixed Unicode and date-offset cases are
currently specific to Books.

| Endpoint | Happy path | Edge cases |
|---|---|---|
| `GET /Authors` | Returns JSON, every author well-formed, within a response-time budget, matches the contract | Ids are unique; every author attributed to a book in the catalogue, and within the 1–200 range; two consecutive reads both return well-formed collections; `PATCH` → 405 |
| `GET /Authors/{id}` | Ids 1, 50, 100, 200 each return their own author | `0`, `-1`, `999999`, `MAX_VALUE` → 404; `abc`, `1.5`, a space, `2147483648` → 400 naming `id`; item `PATCH` → 405 |
| `POST /Authors` | A valid author is accepted and echoed unchanged | Wrong field types (`$.id`, `$.idBook`, `$.firstName`) → 400; `id` and `idBook` each rejected on their own when null; empty body, truncated JSON, a bare string → 400; `text/plain` and XML → 415; 5 000-character names survive untruncated; an empty object is filled with defaults; **an author may reference a book that does not exist**; the created author is not retrievable |
| `PUT /Authors/{id}` | An update is acknowledged and echoed back, matching the contract | Non-integer IDs, wrong book-reference type, individually null `id`/`idBook` and unusable bodies → 400; text/plain and XML → 415; an unknown ID is accepted; **the body's ID wins over the path's**; the update is not persisted |
| `DELETE /Authors/{id}` | Acknowledged with an empty body, matches the contract | Non-integer id → 400; an unknown id is acknowledged too; the deletion is not persisted |

Two relationship and identity findings are tagged `provider-behaviour`:
`idBook` is the only relationship this API models and **nothing enforces it** — an
author can be attributed to book 999999 — and `PUT` silently honours the id in the
body over the one in the path. This establishes which identity is echoed, not a write
to a stored record. Books behaves the same way on the second point. Comparing valid
seeded relationships with accepted dangling references shows why clean sample data
does not prove relationship enforcement on writes.

---

## What the service under test actually guarantees

FakeRestAPI is a **stateless demo**. It validates a payload and echoes it back, but
stores nothing:

```
POST   /api/v1/Books        → 200, the submitted book echoed back
GET    /api/v1/Books/{thatId} → 404
DELETE /api/v1/Books/1      → 200
GET    /api/v1/Books/1      → 200, still there
```

A suite written against an imagined create-read-update-delete lifecycle would fail on
every write, and the failures would say nothing about the service. So the write tests
assert what the API really promises — the status code and the echoed payload — and
the absence of persistence is asserted **explicitly**, in tests named for it
(`theCreatedBookIsNotRetrievable`, `theUpdateIsNotPersisted`,
`theDeletionIsNotPersisted`). If the service ever grows a real store, those tests go
red and say exactly what changed. Documenting a quirk in a comment would not.

`theUpdateIsNotPersisted` carries this in its failure message: *"The demo API is
documented as stateless, but the update survived. The suite's assumptions need
revisiting."*

---

## CI/CD

[`.github/workflows/api-tests.yml`](.github/workflows/api-tests.yml) is split into
lanes, chosen by what happened rather than by a flag. The split exists so that a red
build means one thing.

| Event | What runs | Checks | Blocks a merge? |
|---|---|---|---|
| **Pull request** | Static analysis · framework tests · `SMOKE_RUN` | 35 + 10 | **Yes** |
| **Push to `main`** | Static analysis · framework tests · regression `-DexcludedGroups=provider-behaviour` | 35 + 105 | **Yes** |
| **Nightly, 06:00** | Everything, provider characterisation included | 35 + 123 | No — reports |
| **Monday, 03:00** | Dependency audit, on its own clock | — | Yes, its own job |
| **On demand** | A chosen suite, optionally with the audit | varies | — |

Why the lanes fall there:

- **The framework tests gate everything and reach nothing.** 35 cases, about a
  second. Tests that dispatch requests use Rest Assured filters to capture or answer
  them in memory without forwarding to the network, with `bookstore.invalid` as
  their base URI. A failure there is
  unambiguously this repository's fault, which is what a merge gate should mean.
- **A pull request runs smoke, not the full suite.** Ten cases catch a broken
  request before it merges; running all 123 would make every pull request depend on a
  free-tier sandbox being awake.
- **`main` runs 105 of the 123 API cases.** The 18 excluded are `provider-behaviour` — they pin
  what FakeRestAPI does *today*. If the provider completes their OpenAPI document
  tomorrow, those go red. A test that fails because a third party improved must never
  block somebody's merge.
- **The nightly runs all 123 API cases.** Nothing is being merged at 06:00, so
  that is exactly where you want to hear that the provider changed something.

The API job generates the Allure report **even when the suite is red**, writes the
Markdown summary onto the run page, and uploads the report, the summary and the raw
results. The framework job uploads its summary and raw results separately.
Publishing to Pages happens on `main` only, so a pull request cannot overwrite what
is published.

### Jenkins

A [`Jenkinsfile`](Jenkinsfile) mirrors the same lanes — static analysis, framework
tests, API tests, then the audit — for a Jenkins controller. The
`INCLUDE_PROVIDER_CHARACTERISATION` parameter is the equivalent of the nightly lane:
off for a merge gate, on for a scheduled run.

It needs, on the controller:

| Requirement | Where |
|---|---|
| A JDK tool installation named `jdk-21` | Manage Jenkins → Tools → JDK installations |
| The **Allure** plugin, plus an Allure Commandline tool installation | Plugins, then Tools |
| The **HTML Publisher** plugin | Plugins |

No Maven tool installation is needed: every stage calls `./mvnw`, so the build uses
the wrapper's pinned Maven rather than whatever the controller happens to have.

**It has been run.** A Jenkins LTS controller executes it green — static analysis,
the 35 framework cases, then 105 API cases with `provider-behaviour` excluded, with
the Allure report generated and archived. The evidence is under
[`reports/jenkins/`](reports/jenkins/), because that controller is local and cannot be
reached from outside.

### Which pipeline you can verify

The two are not equivalent, and it is worth being explicit about which is which.

**GitHub Actions runs on every push and is publicly verifiable** — the badge at the
top of this file, the run history and the uploaded artifacts are all part of this
repository, and anyone can open them.

**Jenkins mirrors the same lanes on a local controller.** It cannot be reached from
outside, so the build log and screenshots under [`reports/jenkins/`](reports/jenkins/)
are the evidence rather than a live link.

**It has not been run against a real Jenkins.** It parses as valid Groovy, which
catches syntax errors but says nothing about whether the declarative DSL is well
formed. Validate it before relying on it:

```bash
curl -X POST -F "jenkinsfile=<Jenkinsfile" $JENKINS_URL/pipeline-model-converter/validate
```

The GitHub Actions workflow is the pipeline that is actually exercised.

---

## Limitations

Worth stating plainly, because each one shaped a decision in this suite:

- **No authentication is tested, because the API has none.** There are no tokens,
  sessions, roles or ownership, so the authorization half of an API test strategy has
  nothing to attach to here.
- **Writes do not persist.** The service validates and echoes; it stores nothing. See
  [what the service actually guarantees](#what-the-service-under-test-actually-guarantees).
- **The contract is the provider's, and it is incomplete.** No error responses are
  documented, and Books writes declare no response body while returning one. Both are
  [recorded as findings](#two-findings-about-the-published-document).
- **Collection membership is sampled.** The Books collection must contain at least
  200 records and the four read-only IDs under test; this does not prove every seeded
  ID from 1 to 200 is present. The Authors deletion persistence check verifies a
  subsequent 200 response, but does not yet assert the returned author's ID.
- **Summary aggregation has an equal-timestamp edge case.** If failed and successful
  attempts have the same end timestamp, their merge order can change the reported
  verdict between failed and flaky. The current order-independence test uses different
  timestamps for failure and success and does not cover this boundary.
- **The target is a shared public sandbox** on free-tier hosting. It cold-starts, and
  occasionally times out. Hence the generous response-time budget and the retry policy
  limited to transient failures.
- **No load or performance testing.** Driving load at someone else's demo instance
  would be antisocial, and the numbers would measure their hosting tier rather than
  the API.
- **Tests marked `provider-behaviour` pin what the API does today, not what it owes.**
  A failure in that group means the provider changed something — possibly for the
  better. It is not a regression in this suite.

---

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| Only the API results appear in the Allure report | The two lanes share a results directory and only the first `clean` is intended. `./run-tests.sh` cleans once at the start and runs both lanes without cleaning between them; a manual `./mvnw clean test` between them discards the first lane's results. |
| Tests fail with connection or 5xx errors across the board | Check the configured URL and <https://fakerestapi.azurewebsites.net/api/v1/Books>; the public sandbox may be down or cold-starting. Eligible transient failures get up to three total attempts. A plain 500 is not retried. |
| `cannot find symbol: method getId()` and similar on every DTO | Lombok is not running. From JDK 23 javac no longer picks annotation processors off the classpath; the POM declares Lombok under `annotationProcessorPaths` to handle this. If you changed the compiler configuration, put it back. |
| The Allure report has no steps, and the log mentions `Unsupported class file major version` | AspectJ cannot weave on your JDK. Bump `aspectj.version` in the POM; Allure's `@Step` support depends on it. |
| `Detected JDK version ... is not in the allowed range [21,)` | The enforcer doing its job. `./mvnw` uses `JAVA_HOME`, which may differ from the JDK your `mvn` uses. Set `JAVA_HOME` to a JDK 21+ installation. |
| The build fails in `validate` with a Spotless or Checkstyle error | Formatting or static analysis, not a test failure. Run `./mvnw spotless:apply`; for Checkstyle the message names the file, line and rule. |
| `./mvnw test` runs zero tests | `-Dtest=` or `-Dgroups=` matched nothing. Group names are listed under [Groups](#groups); class names under [Test coverage](#test-coverage). |
| The report is not where the README says | Reports are written relative to the working directory. Run Maven from the repository root. |

---

## Notes on some decisions

**Retries are selective, and `retry=3` means three attempts, not three retries.** The API is a shared free-tier sandbox that occasionally
cold-starts, so a test gets up to `retry=3` **total attempts** — the first call plus
two more — and only when the failure is transient:
a transport-level exception, or a 429/502/503/504
([`TransientFailures`](src/main/java/utils/common/TransientFailures.java)). A plain
500 is *not* retried: it is far more often a real defect than a passing squall. A wrong status code or a broken
assertion is reported the first time it happens. Retrying everything triples the
feedback loop, triples the load on someone else's sandbox, and quietly turns a
deterministic defect into an intermittent one nobody trusts. The summary report folds
the retries into one row per case and shows the attempt count, so a test that is
slowly becoming unreliable stays visible.

**Parallelism.** Suites run `parallel="classes"`. Each test method gets its own
`WebService` from a `ThreadLocal` in
[`BaseTest`](src/main/java/utils/BaseTest.java), because a service carries the
response of the last call — sharing one across a parallel suite would let one test
assert on another test's response.

**Seeded records are owned, not shared.** Suites run `parallel="classes"`, so a class
that writes to a record another class is reading would be a race.
[`SeededCatalogue`](src/main/java/utils/data/SeededCatalogue.java) declares every
seeded id the suite touches and gives each one an owner: the read-only ids are never
written to, and the update and delete classes get ids of their own. This API is
stateless, which would hide such a race today.

It is a first step, not a finished answer: collection reads still overlap writes, and
a genuinely stateful service would also need setup, teardown and a decision about
isolation between runs. What the division buys today is that the obvious collision —
one class deleting the record another is asserting on — cannot happen.

**Test data.** Payloads come from typed builders
([`BookFactory`](src/main/java/utils/data/BookFactory.java)) rather than JSON
fixtures, so a change to *our model* is a compile error rather than a runtime
surprise. A change to the *provider's* schema is a different matter and is caught by
[contract validation](#contract-testing), not by the compiler.

Generated values are seeded and the seed is printed at the start of every run:

```
Generated test data uses seed -5970878229670656902. Replay this run with -Ddata.seed=-5970878229670656902
```

Reusing the seed helps reproduce generated values, but does not guarantee the exact
payload for a particular test: parallel classes share factory generators, and dates
come from the clock. Inspect the request attachment when investigating a failure.
Generated ids start at `100_000`, well above the 200 seeded books, so a generated book
does not collide with the current seeded catalogue. Publish dates are truncated to whole seconds because
the API drops sub-second precision when it echoes a payload back, which would
otherwise fail an equality assertion for a reason that is not a defect.

**One JSON mapper.** [`Json`](src/main/java/utils/common/Json.java) is handed to REST
Assured through `ObjectMapperConfig`, so a DTO is serialized into a request and read
back out of a response by identical rules. Without this, `OffsetDateTime` serializes
as an array of numbers on the way out and fails to parse on the way back.

**Response-time budgets** are deliberately generous (15 s) and applied only where they
are meaningful. A tight budget against a free-tier sandbox reports infrastructure
latency as a product defect.
