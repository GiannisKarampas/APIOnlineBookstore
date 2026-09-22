package TS_FRAMEWORK;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.FRAMEWORK;
import static utils.TestGroups.REGRESSION;

import java.util.List;

import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import utils.listeners.report.RunTotals;
import utils.listeners.report.TestCaseResult;

/**
 * How retried tests are folded into the execution summary.
 * <p>
 * The earlier version let a passing attempt overwrite the failed one entirely, so a
 * test that failed twice and then passed appeared as a clean pass with no evidence
 * left. That is the shape of a suite quietly becoming untrustworthy, so it is pinned
 * down here.
 */
@Epic("Framework")
@Feature("Reporting")
@Story("Execution summary")
public class TC_FRAMEWORK_04_ExecutionSummary {

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A test that failed and then passed is reported as flaky, keeping the evidence")
    public void aPassAfterAFailureIsFlaky() {
        TestCaseResult merged = failed("expected [200] but found [503]").mergeWith(passed());

        assertEquals(merged.status(), TestCaseResult.FLAKY,
                "A pass that needed a retry is not the same as a pass, and reporting it as one hides "
                        + "a test on its way to becoming unreliable.");
        assertEquals(merged.attempts(), 2, "Both attempts should be counted.");
        assertTrue(merged.carriesEvidence(),
                "The message from the failed attempt is the only reason anyone could investigate later.");
        assertTrue(merged.failureMessage().contains("503"),
                "The evidence kept should be the original failure, not an empty string.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A test that never recovered is reported as failed")
    public void repeatedFailuresStayFailed() {
        TestCaseResult merged = failed("first").mergeWith(failed("second"));

        assertEquals(merged.status(), TestCaseResult.FAILED, "Nothing passed, so nothing should be green.");
        assertEquals(merged.attempts(), 2, "Both attempts should be counted.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A clean pass stays a clean pass")
    public void aSingleAttemptIsAPlainPass() {
        assertEquals(passed().status(), TestCaseResult.PASSED, "One attempt, no failure, nothing to qualify.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Flaky cases count as green in the pass rate but are reported separately")
    public void flakyCasesAreCountedSeparately() {
        RunTotals totals = RunTotals.of(List.of(
                passed(),
                failed("still broken").mergeWith(passed())));

        assertEquals(totals.flaky(), 1, "The flaky case has to be visible as flaky.");
        assertEquals(totals.failed(), 0, "It did ultimately pass, so it is not a failure.");
        assertEquals(totals.passRate(), "100.0",
                "It passed, so the rate is 100% - which is exactly why the flaky count must sit beside it.");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Folding attempts gives the same answer whatever order they arrive in")
    public void foldingIsOrderIndependent() {
        String failFailPass = failed("a").mergeWith(failed("b")).mergeWith(passed()).status();
        String passFailFail = passed().mergeWith(failed("a")).mergeWith(failed("b")).status();

        assertEquals(failFailPass, passFailFail,
                "The same three attempts gave different verdicts depending on collection order. "
                        + "Folding has to be driven by facts that do not depend on order - which attempt "
                        + "finished last, and whether any of them failed.");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "An attempt TestNG marks as retried is treated as a failure, not a skip")
    public void aRetriedAttemptCountsAsAFailure() {
        // This is how TestNG really reports the first two attempts of a test that
        // fails, is retried, and then passes: skipped, with wasRetried set. Reading
        // that literally made a flaky test look like a clean pass, because nothing in
        // the run was ever recorded as a failure.
        TestCaseResult retriedAway = TestCaseResult.of("suite", "context",
                TestNgResults.retriedAway("Expected status 200 but the API answered 503"));
        TestCaseResult eventualPass = TestCaseResult.of("suite", "context", TestNgResults.passed());

        assertEquals(retriedAway.mergeWith(eventualPass).status(), TestCaseResult.FLAKY,
                "A test that only passed because it was retried has to be reported as flaky.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A genuinely skipped test is still reported as skipped")
    public void anUnretriedSkipStaysASkip() {
        TestCaseResult skipped = TestCaseResult.of("suite", "context", TestNgResults.skipped());

        assertEquals(skipped.status(), TestCaseResult.SKIPPED,
                "A test skipped because its dependency failed is not a failure of its own.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Elapsed time is wall clock, not the sum of test durations")
    public void elapsedTimeIsWallClock() {
        // Two tests of one second each, running at the same moment on two threads.
        TestCaseResult first = new TestCaseResult("suite", "context", "C", "a", "a", "",
                TestCaseResult.PASSED, false, 1, 1_000, 0, 1_000, "");
        TestCaseResult second = new TestCaseResult("suite", "context", "C", "b", "b", "",
                TestCaseResult.PASSED, false, 1, 1_000, 0, 1_000, "");

        RunTotals totals = RunTotals.of(List.of(first, second));

        assertEquals(totals.elapsedInSeconds(), "1.0",
                "Elapsed time must reflect how long the run actually took.");
        assertEquals(totals.cumulativeInSeconds(), "2.0",
                "Cumulative time is the sum, and is larger under parallel execution. Reporting only one "
                        + "of the two, unlabelled, is what makes a report look wrong.");
    }

    private TestCaseResult passed() {
        return attempt(TestCaseResult.PASSED, "", 2_000);
    }

    private TestCaseResult failed(String message) {
        return attempt(TestCaseResult.FAILED, message, 1_000);
    }

    /**
     * An attempt as {@link TestCaseResult#of} would have built it from a TestNG
     * result, including the flag that records whether that attempt failed.
     */
    private TestCaseResult attempt(String outcome, String message, long startedAt) {
        return new TestCaseResult("suite", "context", "Class", "method", "a scenario", "",
                outcome, TestCaseResult.FAILED.equals(outcome), 1, 100, startedAt, startedAt + 100, message);
    }
}
