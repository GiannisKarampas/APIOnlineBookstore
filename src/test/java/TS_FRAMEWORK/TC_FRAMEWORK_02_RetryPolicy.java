package TS_FRAMEWORK;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.FRAMEWORK;
import static utils.TestGroups.REGRESSION;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import utils.common.Retry;
import utils.common.TransientFailures;
import utils.common.UnexpectedStatusException;
import utils.config.EnvDataConfig;

/**
 * What counts as worth trying again.
 * <p>
 * The first version of this policy matched HTTP statuses by searching exception text
 * and treated every {@code IOException} as transient, which meant a missing fixture,
 * an invalid certificate and a genuine 500 were all retried three times. These rows
 * are the cases that got it wrong, kept so they cannot get it wrong again.
 */
@Epic("Framework")
@Feature("Reliability")
@Story("Retry policy")
public class TC_FRAMEWORK_02_RetryPolicy {

    @DataProvider(name = "failures")
    public Object[][] failures() {
        return new Object[][]{
                // Worth another attempt: no considered answer arrived.
                {"connection refused", new ConnectException("Connection refused"), true},
                {"read timed out", new SocketTimeoutException("Read timed out"), true},
                {"unknown host", new UnknownHostException("bookstore.invalid"), true},
                {"bad gateway", status(502), true},
                {"service unavailable", status(503), true},
                {"gateway timeout", status(504), true},
                {"too many requests", status(429), true},

                // Deterministic: another attempt would fail identically.
                {"not found", status(404), false},
                {"bad request", status(400), false},
                {"internal server error", status(500), false},
                {"missing fixture", new FileNotFoundException("contracts/openapi.json"), false},
                {"invalid certificate", new SSLHandshakeException("PKIX path building failed"), false},
                {"unparsable payload", unparsablePayload(), false},
                {"ordinary assertion failure", new AssertionError("A book must carry a title"), false},
        };
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(dataProvider = "failures",
            groups = {FRAMEWORK, REGRESSION},
            description = "Only failures that could plausibly succeed on a second attempt are retried")
    public void onlyTransientFailuresAreRetried(String scenario, Throwable failure, boolean expectedToRetry) {
        assertEquals(TransientFailures.isTransient(failure), expectedToRetry,
                "Wrong retry decision for " + scenario + ". Retrying a deterministic failure hides a real "
                        + "defect behind a slower, noisier build; refusing to retry a transient one makes the "
                        + "suite look unreliable when the network was at fault.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A transient failure nested inside another exception is still recognised")
    public void aWrappedTransientFailureIsRecognised() {
        Throwable wrapped = new IllegalStateException("could not complete the call",
                new SocketTimeoutException("Read timed out"));

        assertEquals(TransientFailures.isTransient(wrapped), true,
                "The policy has to walk the cause chain: transport failures usually arrive wrapped.");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A transient failure is retried until the attempt budget is spent, then reported")
    public void retriesStopWhenTheBudgetIsSpent() {
        Retry retry = new Retry();
        int budget = new EnvDataConfig().getRetry();

        int attemptsMade = 1;
        while (retry.retry(TestNgResults.failedWith(new SocketTimeoutException("Read timed out")))) {
            attemptsMade++;
            assertTrue(attemptsMade <= budget,
                    "The analyzer kept asking for another attempt past the configured budget of " + budget
                            + ". An unbounded retry turns one slow endpoint into a hung build.");
        }

        assertEquals(attemptsMade, budget,
                "retry=" + budget + " means " + budget + " total attempts - the first call plus "
                        + (budget - 1) + " more. The analyzer stopped after " + attemptsMade + ".");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A deterministic failure is never retried, not even once")
    public void aDeterministicFailureIsNotRetriedAtAll() {
        Retry retry = new Retry();

        assertEquals(retry.retry(TestNgResults.failedWith(status(404))), false,
                "A 404 will be a 404 on the next attempt too. Retrying it triples the feedback loop "
                        + "and hides a reproducible defect behind an intermittent one.");
    }

    private static UnexpectedStatusException status(int actual) {
        return new UnexpectedStatusException(200, actual, "{}");
    }

    private static JsonProcessingException unparsablePayload() {
        return new JsonProcessingException("Cannot deserialize value of type OffsetDateTime") {
            private static final long serialVersionUID = 1L;
        };
    }
}
