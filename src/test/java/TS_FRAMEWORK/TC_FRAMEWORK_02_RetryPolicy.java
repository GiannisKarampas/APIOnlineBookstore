package TS_FRAMEWORK;

import static org.testng.Assert.assertEquals;
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
import utils.common.TransientFailures;
import utils.common.UnexpectedStatusException;

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

    private static UnexpectedStatusException status(int actual) {
        return new UnexpectedStatusException(200, actual, "{}");
    }

    private static JsonProcessingException unparsablePayload() {
        return new JsonProcessingException("Cannot deserialize value of type OffsetDateTime") {
            private static final long serialVersionUID = 1L;
        };
    }
}
