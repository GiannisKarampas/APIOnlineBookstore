package utils.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * Writes the shape of a run to the console: which test started, which finished how,
 * and the totals at the end. The browsable record is Allure's; this is what makes a
 * tailed log readable while a suite is running.
 */
public class TestListener extends TestListenerAdapter {

    private static final String LOGGER_SEPARATOR = "====================================================";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.info("Finished running suite {}", context.getName());
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        // Counted per execution rather than per method, so a data-driven test
        // contributes one result per data set, the way the report shows it.
        LOGGER.info("Total tests: {}", passed + failed + skipped);
        LOGGER.info("Total Passed: {}", passed);
        LOGGER.info("Total failures: {}", failed);
        LOGGER.info("Total Skipped: {}", skipped);
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.info("Started running suite {}", context.getName());
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.warn("Test partially succeeded: {}", tr.getName());
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.error("Test failed: {}", tr.getName(), tr.getThrowable());
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.warn("Test skipped: {}", tr.getName());
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onTestStart(ITestResult tr) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.info("Test Name: {}", tr.getName());
        LOGGER.info(LOGGER_SEPARATOR);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        LOGGER.info(LOGGER_SEPARATOR);
        LOGGER.info("Test succeeded: {}", tr.getName());
        LOGGER.info(LOGGER_SEPARATOR);
    }
}
