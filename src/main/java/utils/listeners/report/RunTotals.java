package utils.listeners.report;

import java.util.List;
import java.util.Locale;

/**
 * The headline numbers of a run.
 * <p>
 * Every number is rendered under {@link Locale#ROOT}. The default locale would write
 * {@code 100,0} on a machine configured for a comma-decimal language, which changes
 * the report a reviewer opens and fails this class's own tests for a reason that has
 * nothing to do with the suite.
 */
public record RunTotals(int total, int passed, int flaky, int failed, int skipped,
                        long cumulativeMillis, long elapsedMillis) {

    public static RunTotals of(List<TestCaseResult> testCases) {
        long earliest = testCases.stream().mapToLong(TestCaseResult::startedAt).min().orElse(0);
        long latest = testCases.stream().mapToLong(TestCaseResult::endedAt).max().orElse(0);
        return new RunTotals(
                testCases.size(),
                (int) testCases.stream().filter(testCase -> TestCaseResult.PASSED.equals(testCase.status())).count(),
                (int) testCases.stream().filter(TestCaseResult::isFlaky).count(),
                (int) testCases.stream().filter(TestCaseResult::hasFailed).count(),
                (int) testCases.stream().filter(testCase -> TestCaseResult.SKIPPED.equals(testCase.status())).count(),
                testCases.stream().mapToLong(TestCaseResult::durationInMillis).sum(),
                Math.max(latest - earliest, 0));
    }

    /**
     * Flaky cases count as green here, because they did ultimately pass. They are
     * reported separately so that a rate of 100% alongside a non-zero flaky count
     * cannot be mistaken for a clean run.
     */
    public String passRate() {
        return total == 0 ? "0.0" : String.format(Locale.ROOT, "%.1f", ((passed + flaky) * 100.0) / total);
    }

    /** Wall-clock time from the first test starting to the last one finishing. */
    public String elapsedInSeconds() {
        return String.format(Locale.ROOT, "%.1f", elapsedMillis / 1000.0);
    }

    /** Summed test durations. Exceeds elapsed time when tests run in parallel. */
    public String cumulativeInSeconds() {
        return String.format(Locale.ROOT, "%.1f", cumulativeMillis / 1000.0);
    }
}
