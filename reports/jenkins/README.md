# Jenkins evidence

The [`Jenkinsfile`](../../Jenkinsfile) runs on a Jenkins LTS controller hosted
locally. That controller is not reachable from outside this machine, so these files
are the evidence that the pipeline runs rather than merely parses.

| File | What it shows |
|---|---|
| `console-output.txt` | The full build log, 2,932 lines: the Jenkinsfile fetched from git, every stage in order, both test runs, and `Finished: SUCCESS` |
| `overview.png` | Build #9, green in 26 seconds, from revision `e3f33dd` of this repository. Archived artifacts: the Allure report, both summary reports and the JUnit XML |
| `allure-report.png` | The Allure report published from that build: **140 cases, 140 passed** — 35 from the framework suite and 105 from the API suite |

## What the log shows

The stage sequence, in the order the pipeline declares it:

| Stage | Result |
|---|---|
| Checkout SCM | Jenkinsfile obtained from git, not from a working copy |
| Tool Install | The `jdk-21` tool installation |
| Static analysis | Spotless and Checkstyle, `BUILD SUCCESS` |
| Framework tests | `Tests run: 35, Failures: 0` in 1.2s |
| API tests | `Tests run: 105, Failures: 0` in 9.1s |
| Dependency audit | `skipped due to when conditional` — the guard doing its job |
| Post actions | JUnit results recorded, Allure generated, summaries archived |

## Reconciling 105 against the 123 in the main README

The build ran with the default parameters, and `INCLUDE_PROVIDER_CHARACTERISATION` is
off by default. That excludes the 18 `provider-behaviour` cases, which pin what
FakeRestAPI does today rather than what it owes anyone — the same exclusion the
`main` lane uses in GitHub Actions, and for the same reason: a test that fails
because the provider *improved* must never block a merge.

The log shows the exclusion landing per suite, which is worth checking rather than
taking on trust:

| Suite | Full run | This build | Excluded |
|---|---|---|---|
| Books | 63 | 55 | 8 |
| Contract | 4 | 2 | 2 |
| Authors | 56 | 48 | 8 |
| **API total** | **123** | **105** | **18** |

Turning the parameter on runs all 123. The Allure report's own metadata panel records
the environment (`dev`, `https://fakerestapi.azurewebsites.net`), written by the suite
rather than typed into this file.

## Reproducing it

The controller needs a JDK tool installation named `jdk-21`, the Allure plugin with
an Allure Commandline tool, and the HTML Publisher plugin. No Maven tool is required:
every stage calls `./mvnw`. See the Jenkins section of the
[main README](../../README.md#jenkins).

The log contains no credentials — the build needs none, since the repository is
public and the API under test is unauthenticated. The workspace paths in it are local
to the machine that ran it.

## Why this is not a link

GitHub Actions is the pipeline a reviewer can verify independently — it runs on every
push, and its history and artifacts are part of this repository. Jenkins is the same
lanes on a controller that happens to be local, and these files are how that is shown.
Treating the two as equivalent would overstate what the second one proves.
