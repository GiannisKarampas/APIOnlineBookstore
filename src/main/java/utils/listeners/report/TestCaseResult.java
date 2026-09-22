package utils.listeners.report;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.testng.ITestResult;

/**
 * One row of the execution summary: a single test invocation and how it ended up.
 * <p>
 * Two facts are recorded rather than one conclusion: how the <em>last</em> attempt
 * ended, and whether <em>any</em> attempt failed. Folding retries together then
 * becomes order-independent — the terminal outcome is whichever attempt finished
 * last, and "something failed along the way" is an OR. An earlier version derived a
 * status from two statuses, which gave different answers depending on the order the
 * attempts happened to be collected in.
 * <p>
 * Separate from the reporter that renders it so these rules can be tested without
 * running a suite.
 */
public record TestCaseResult(String suiteName, String contextName, String className, String methodName,
                             String description, String parameters, String terminalStatus,
                             boolean anyAttemptFailed, int attempts, long durationInMillis,
                             long startedAt, long endedAt, String failureMessage) {

    public static final String PASSED = "PASS";
    public static final String FLAKY = "FLAKY";
    public static final String FAILED = "FAIL";
    public static final String SKIPPED = "SKIP";

    public static TestCaseResult of(String suiteName, String contextName, ITestResult result) {
        String outcome = statusOf(result);
        return new TestCaseResult(
                suiteName,
                contextName,
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                descriptionOf(result),
                parametersOf(result),
                outcome,
                FAILED.equals(outcome),
                1,
                result.getEndMillis() - result.getStartMillis(),
                result.getStartMillis(),
                result.getEndMillis(),
                failureMessageOf(result));
    }

    /**
     * How this row should be reported: a pass that needed more than one attempt is
     * flaky, not green.
     */
    public String status() {
        if (FAILED.equals(terminalStatus)) {
            return FAILED;
        }
        if (PASSED.equals(terminalStatus) && anyAttemptFailed) {
            return FLAKY;
        }
        return terminalStatus;
    }

    /**
     * The data set a test was invoked with, rendered so it reads in a report.
     * Empty for a test that takes no parameters.
     */
    public static String parametersOf(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length == 0) {
            return "";
        }
        return Arrays.stream(parameters)
                .map(parameter -> parameter == null ? "null" : "'" + parameter + "'")
                .collect(Collectors.joining(", "));
    }

    /**
     * Folds another attempt at the same invocation into this row.
     * <p>
     * Commutative and associative, so the order TestNG happens to hand the attempts
     * over cannot change the answer.
     */
    public TestCaseResult mergeWith(TestCaseResult other) {
        TestCaseResult latest = other.endedAt >= endedAt ? other : this;
        String evidence = carriesEvidence() ? failureMessage : other.failureMessage;

        return new TestCaseResult(latest.suiteName, latest.contextName, latest.className, latest.methodName,
                latest.description, latest.parameters, latest.terminalStatus,
                anyAttemptFailed || other.anyAttemptFailed,
                attempts + other.attempts,
                durationInMillis + other.durationInMillis,
                Math.min(startedAt, other.startedAt), Math.max(endedAt, other.endedAt), evidence);
    }

    public boolean hasFailed() {
        return FAILED.equals(status());
    }

    /** Passed, but only after a retry. Green, and still worth looking at. */
    public boolean isFlaky() {
        return FLAKY.equals(status());
    }

    /** Whether a failed attempt left a message behind, flaky passes included. */
    public boolean carriesEvidence() {
        return failureMessage != null && !failureMessage.isBlank();
    }

    public String statusIcon() {
        return switch (status()) {
            case PASSED -> "✅";
            case FLAKY -> "⚡";
            case FAILED -> "❌";
            default -> "⚠️";
        };
    }

    private static String descriptionOf(ITestResult result) {
        String description = result.getMethod().getDescription();
        return description == null || description.isBlank() ? result.getMethod().getMethodName() : description;
    }

    /**
     * Translates TestNG's view of an attempt into ours.
     * <p>
     * The {@code wasRetried} branch is the one that matters. TestNG does not file an
     * attempt that is about to be retried as a failure: it files it as skipped, and
     * marks it retried. Reading that literally makes a test which failed twice and
     * then passed look like a clean pass, since nothing in the run was ever recorded
     * as a failure. That is precisely the run worth flagging.
     */
    private static String statusOf(ITestResult result) {
        if (result.getStatus() == ITestResult.SUCCESS) {
            return PASSED;
        }
        if (result.getStatus() == ITestResult.FAILURE) {
            return FAILED;
        }
        return result.wasRetried() ? FAILED : SKIPPED;
    }

    private static String failureMessageOf(ITestResult result) {
        Throwable cause = result.getThrowable();
        if (cause == null) {
            return "";
        }
        String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        return message.lines().limit(6).collect(Collectors.joining("\n"));
    }
}
