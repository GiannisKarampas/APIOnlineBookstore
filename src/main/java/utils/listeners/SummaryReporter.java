package utils.listeners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import utils.listeners.report.RunTotals;
import utils.listeners.report.TestCaseResult;

/**
 * Writes a self-contained pass/fail report next to the Allure results.
 * <p>
 * Allure gives the rich, browsable view but needs its command line tool and a web
 * server to render. This reporter produces two files that need neither: an HTML page
 * that opens straight from disk, and a Markdown summary that CI folds into the build
 * page. Both are the deliverable "test execution report".
 * <p>
 * Retries are collapsed: a test that failed twice and then passed is reported once,
 * as passed, with the number of attempts it took.
 */
public class SummaryReporter implements IReporter {

    private static final Path REPORT_DIRECTORY = Path.of("test-results", "summary");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryReporter.class);

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<TestCaseResult> testCases = collectTestCases(suites);
        if (testCases.isEmpty()) {
            LOGGER.warn("No test results to report on.");
            return;
        }

        RunTotals totals = RunTotals.of(testCases);
        try {
            Files.createDirectories(REPORT_DIRECTORY);
            write(REPORT_DIRECTORY.resolve("index.html"), renderHtml(testCases, totals));
            write(REPORT_DIRECTORY.resolve("summary.md"), renderMarkdown(testCases, totals));
            LOGGER.info("Execution summary written to {}", REPORT_DIRECTORY.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Could not write the execution summary.", e);
        }
    }

    /**
     * Flattens every result of every suite into one row per test method, keeping the
     * final outcome of that method rather than the outcome of each retry.
     */
    private List<TestCaseResult> collectTestCases(List<ISuite> suites) {
        Map<String, TestCaseResult> byMethod = new LinkedHashMap<>();

        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                List<ITestResult> allResults = new ArrayList<>();
                allResults.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
                allResults.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
                allResults.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());

                String contextName = suiteResult.getTestContext().getName();
                for (ITestResult result : allResults) {
                    byMethod.merge(keyOf(suite.getName(), contextName, result),
                            TestCaseResult.of(suite.getName(), contextName, result), TestCaseResult::mergeWith);
                }

                // A failure in a @BeforeMethod is a failure of the run, but TestNG
                // files it separately, so it would otherwise be missing entirely.
                for (ITestResult configuration : suiteResult.getTestContext()
                        .getFailedConfigurations().getAllResults()) {
                    byMethod.merge(keyOf(suite.getName(), contextName, configuration),
                            TestCaseResult.of(suite.getName(), contextName, configuration), TestCaseResult::mergeWith);
                }
            }
        }

        return byMethod.values().stream()
                .sorted(Comparator.comparing(TestCaseResult::className)
                        .thenComparing(TestCaseResult::methodName)
                        .thenComparing(TestCaseResult::parameters)
                        .thenComparing(TestCaseResult::contextName))
                .toList();
    }

    /**
     * Identifies one reportable case. The data set is part of the key, so a
     * data-driven test contributes one row per data set, while the retries of a
     * single data set fold back into that one row.
     */
    private String keyOf(String suiteName, String contextName, ITestResult result) {
        // The context is part of the key: the same method run under two <test> tags
        // is two independent executions, and merging them would let a pass in one
        // conceal a failure in the other.
        return suiteName + "/" + contextName + "/" + result.getTestClass().getName()
                + "#" + result.getMethod().getMethodName()
                + "(" + TestCaseResult.parametersOf(result) + ")";
    }

    private void write(Path file, String content) throws IOException {
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    private String renderMarkdown(List<TestCaseResult> testCases, RunTotals totals) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Online Bookstore API - Test Execution Report\n\n")
                .append("Executed at ").append(LocalDateTime.now().format(TIMESTAMP)).append("\n\n")
                .append("| Total | Passed | Flaky | Failed | Skipped | Pass rate | Elapsed | Cumulative |\n")
                .append("|---|---|---|---|---|---|---|---|\n")
                .append("| ").append(totals.total()).append(" | ").append(totals.passed())
                .append(" | ").append(totals.flaky()).append(" | ").append(totals.failed())
                .append(" | ").append(totals.skipped())
                .append(" | ").append(totals.passRate()).append("% | ")
                .append(totals.elapsedInSeconds()).append("s | ")
                .append(totals.cumulativeInSeconds()).append("s |\n\n")
                .append("Elapsed is wall-clock time; cumulative is the sum of test durations, ")
                .append("which is larger because classes run in parallel.\n\n")
                .append("| Status | Test case | Scenario | Attempts | Duration |\n")
                .append("|---|---|---|---|---|\n");

        for (TestCaseResult testCase : testCases) {
            markdown.append("| ").append(testCase.statusIcon())
                    .append(" ").append(testCase.status())
                    .append(" | `").append(testCase.className()).append(".").append(testCase.methodName()).append("`")
                    .append(" | ").append(testCase.description())
                    .append(testCase.parameters().isEmpty() ? "" : " <br>`[" + testCase.parameters() + "]`")
                    .append(" | ").append(testCase.attempts())
                    .append(" | ").append(testCase.durationInMillis()).append("ms |\n");
        }

        // Flaky cases are listed with the failures, not hidden among the passes:
        // the evidence from the attempt that failed is the whole point of keeping it.
        List<TestCaseResult> worthReading = testCases.stream()
                .filter(testCase -> testCase.hasFailed() || testCase.isFlaky())
                .filter(TestCaseResult::carriesEvidence)
                .toList();
        if (!worthReading.isEmpty()) {
            markdown.append("\n## Failures and flaky passes\n\n");
            for (TestCaseResult testCase : worthReading) {
                markdown.append("### ").append(testCase.statusIcon()).append(" ").append(testCase.methodName());
                if (testCase.isFlaky()) {
                    markdown.append(" (passed on attempt ").append(testCase.attempts()).append(")");
                }
                markdown.append("\n\n```\n").append(testCase.failureMessage()).append("\n```\n\n");
            }
        }

        return markdown.toString();
    }

    /**
     * Fills the page template. Named placeholders rather than {@code String.formatted},
     * because the embedded CSS is full of percent signs that a format string would
     * try to read as conversions.
     */
    private String renderHtml(List<TestCaseResult> testCases, RunTotals totals) {
        String rows = testCases.stream().map(this::renderHtmlRow).collect(Collectors.joining("\n"));

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Online Bookstore API - Test Execution Report</title>
                <style>
                  :root {
                    --bg: #f6f7f9; --panel: #ffffff; --ink: #14181f; --muted: #5b6472;
                    --line: #e3e6ea; --pass: #1a7f45; --fail: #c62828; --skip: #b26a00;
                  }
                  * { box-sizing: border-box; }
                  body { margin: 0; padding: 32px 16px; background: var(--bg); color: var(--ink);
                         font: 15px/1.5 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                  main { max-width: 1100px; margin: 0 auto; }
                  h1 { font-size: 24px; margin: 0 0 4px; }
                  .when { color: var(--muted); margin: 0 0 24px; font-size: 13px; }
                  .tiles { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; margin-bottom: 24px; }
                  .tile { background: var(--panel); border: 1px solid var(--line); border-radius: 10px; padding: 16px; }
                  .tile .label { color: var(--muted); font-size: 12px; text-transform: uppercase; letter-spacing: .06em; }
                  .tile .value { font-size: 28px; font-weight: 650; margin-top: 4px; font-variant-numeric: tabular-nums; }
                  table { width: 100%; border-collapse: collapse; background: var(--panel);
                          border: 1px solid var(--line); border-radius: 10px; overflow: hidden; }
                  th { text-align: left; font-size: 12px; text-transform: uppercase; letter-spacing: .06em;
                       color: var(--muted); padding: 12px 14px; border-bottom: 1px solid var(--line); }
                  td { padding: 12px 14px; border-bottom: 1px solid var(--line); vertical-align: top; }
                  tr:last-child td { border-bottom: 0; }
                  code { font: 13px/1.4 ui-monospace, SFMono-Regular, Menlo, monospace; }
                  .status { font-weight: 650; white-space: nowrap; }
                  .PASS { color: var(--pass); } .FAIL { color: var(--fail); }
                  .SKIP, .FLAKY { color: var(--skip); }
                  .num { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; color: var(--muted); }
                  .why { color: var(--fail); font-size: 13px; margin-top: 6px; white-space: pre-wrap; }
                  @media (prefers-color-scheme: dark) {
                    :root { --bg: #0f1216; --panel: #171b21; --ink: #e8eaed; --muted: #9aa4b2;
                            --line: #262b33; --pass: #4ade80; --fail: #f87171; --skip: #fbbf24; }
                  }
                </style>
                </head>
                <body>
                <main>
                  <h1>Online Bookstore API &mdash; Test Execution Report</h1>
                  <p class="when">Executed at {{executedAt}}</p>
                  <section class="tiles">
                    <div class="tile"><div class="label">Total</div><div class="value">{{total}}</div></div>
                    <div class="tile"><div class="label">Passed</div><div class="value PASS">{{passed}}</div></div>
                    <div class="tile"><div class="label">Flaky</div><div class="value FLAKY">{{flaky}}</div></div>
                    <div class="tile"><div class="label">Failed</div><div class="value FAIL">{{failed}}</div></div>
                    <div class="tile"><div class="label">Skipped</div><div class="value SKIP">{{skipped}}</div></div>
                    <div class="tile"><div class="label">Pass rate</div><div class="value">{{passRate}}%</div></div>
                    <div class="tile"><div class="label">Elapsed</div><div class="value">{{elapsed}}s</div>
                      <div class="label" style="margin-top:6px">{{cumulative}}s cumulative</div></div>
                  </section>
                  <table>
                    <thead><tr><th>Status</th><th>Test case</th><th>Scenario</th><th class="num">Attempts</th><th class="num">Duration</th></tr></thead>
                    <tbody>
                {{rows}}
                    </tbody>
                  </table>
                </main>
                </body>
                </html>
                """
                .replace("{{executedAt}}", LocalDateTime.now().format(TIMESTAMP))
                .replace("{{total}}", String.valueOf(totals.total()))
                .replace("{{passed}}", String.valueOf(totals.passed()))
                .replace("{{failed}}", String.valueOf(totals.failed()))
                .replace("{{skipped}}", String.valueOf(totals.skipped()))
                .replace("{{passRate}}", totals.passRate())
                .replace("{{flaky}}", String.valueOf(totals.flaky()))
                .replace("{{elapsed}}", totals.elapsedInSeconds())
                .replace("{{cumulative}}", totals.cumulativeInSeconds())
                .replace("{{rows}}", rows);
    }

    private String renderHtmlRow(TestCaseResult testCase) {
        String failure = testCase.carriesEvidence()
                ? "<div class=\"why\">" + (testCase.isFlaky() ? "Passed on retry. First attempt: " : "")
                        + escape(testCase.failureMessage()) + "</div>"
                : "";
        String dataSet = testCase.parameters().isEmpty()
                ? ""
                : "<br><code>[" + escape(testCase.parameters()) + "]</code>";
        return """
                      <tr>
                        <td class="status %s">%s</td>
                        <td><code>%s</code><br><code>%s</code></td>
                        <td>%s%s</td>
                        <td class="num">%d</td>
                        <td class="num">%dms</td>
                      </tr>""".formatted(
                testCase.status(), testCase.status(),
                escape(testCase.className()), escape(testCase.methodName()),
                escape(testCase.description()) + dataSet, failure,
                testCase.attempts(), testCase.durationInMillis());
    }

    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
