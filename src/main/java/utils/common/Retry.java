package utils.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import utils.config.EnvDataConfig;

/**
 * Re-runs a test only when {@link TransientFailures} says the failure was the network
 * or the service having a bad moment.
 * <p>
 * {@code retry} in the environment file is the total number of attempts, not the
 * number of retries: {@code retry=3} means one call and at most two more.
 * <p>
 * A test that needed more than one attempt is reported as flaky rather than as simply
 * passed, and the evidence from the failed attempt is kept, so a test that is quietly
 * becoming unreliable stays visible.
 */
public class Retry implements IRetryAnalyzer {

    private static final int MAX_ATTEMPTS = new EnvDataConfig().getRetry();
    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class);

    private int attempts = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (result.isSuccess() || attempts >= MAX_ATTEMPTS) {
            return false;
        }
        if (!TransientFailures.isTransient(result.getThrowable())) {
            return false;
        }
        attempts++;
        LOGGER.warn("Transient failure in {}, attempt {} of {}: {}",
                result.getMethod().getMethodName(), attempts, MAX_ATTEMPTS, summarise(result.getThrowable()));
        return true;
    }

    private String summarise(Throwable failure) {
        if (failure == null) {
            return "no throwable recorded";
        }
        String message = failure.getMessage();
        return message == null ? failure.getClass().getSimpleName() : message.lines().findFirst().orElse("");
    }
}
