# Test execution report

The committed result of a full run against the `dev` environment
(`https://fakerestapi.azurewebsites.net`).

| File | What it is |
|---|---|
| `execution-summary.html` | The API suite. Open it in a browser: every test case with its status, data set, attempts and duration. Self-contained, no server needed. |
| `execution-summary.md` | The same content as Markdown. This is what CI folds into the GitHub Actions run page. |
| `framework-summary.html` | The framework's own tests — the gate that runs before the API suite. |
| `framework-summary.md` | The same, as Markdown. |
| `jenkins/` | The build log and screenshots from the Jenkins controller, which is local and cannot be linked to. See [`jenkins/README.md`](jenkins/README.md). |

**Result: 123 / 123 API cases passed, and 35 / 35 framework cases.**

The two are reported separately because they mean different things. A failure in the
API suite may mean the shared public sandbox is having a bad moment; a failure in the
framework suite means this repository is wrong.

The Allure report is not committed — it is several hundred generated files and needs
a web server to render. Regenerate it in one command:

```bash
mvn allure:serve
```

CI publishes it as a build artifact on every run, and to GitHub Pages on `main`.
