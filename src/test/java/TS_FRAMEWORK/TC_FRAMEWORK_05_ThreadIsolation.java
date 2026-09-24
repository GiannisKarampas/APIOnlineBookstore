package TS_FRAMEWORK;

import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static utils.TestGroups.FRAMEWORK;
import static utils.TestGroups.REGRESSION;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import utils.BaseTest;
import utils.service.implementation.WebService;

/**
 * The claim that makes parallel execution safe: no two threads share a web service.
 * <p>
 * Every suite runs {@code parallel="classes"}, and a web service carries the response
 * of the last call. If one were shared, a test would occasionally assert on another
 * test's response - intermittently, and only under load, which is the worst shape a
 * defect can take. The isolation is a {@link ThreadLocal} in {@link BaseTest}, and
 * until now nothing exercised it.
 * <p>
 * Extends {@link BaseTest} deliberately: the property under test belongs to the real
 * lifecycle, so testing a copy of the mechanism would prove nothing about it.
 */
@Epic("Framework")
@Feature("Transport")
@Story("Thread isolation")
public class TC_FRAMEWORK_05_ThreadIsolation extends BaseTest {

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Two threads asking for a web service get two different ones")
    public void eachThreadGetsItsOwnWebService() throws Exception {
        WebService onThisThread = step("Act on the test's own thread");

        ExecutorService elsewhere = Executors.newSingleThreadExecutor();
        WebService onAnotherThread;
        try {
            onAnotherThread = elsewhere.submit(() -> step("Act on a second thread")).get();
        } finally {
            elsewhere.shutdown();
        }

        assertNotSame(onThisThread, onAnotherThread,
                "Both threads were handed the same web service. A service holds the last response, so "
                        + "one test would be able to assert on another test's response.");
        assertNotSame(onThisThread.context(), onAnotherThread.context(),
                "The two web services share one context, which leaks the last response and the pending "
                        + "step description across threads just as surely.");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "What one thread records is invisible to another")
    public void oneThreadsStateDoesNotReachAnother() throws Exception {
        WebService onThisThread = step("Record a request on the test's own thread");
        onThisThread.context().setLastRequestPath("/api/v1/Books/{id}");

        ExecutorService elsewhere = Executors.newSingleThreadExecutor();
        String seenElsewhere;
        try {
            seenElsewhere = elsewhere.submit(
                    () -> step("Read the context on a second thread").context().getLastRequestPath()).get();
        } finally {
            elsewhere.shutdown();
        }

        // Asserted on the state rather than on identity, because two distinct services
        // that somehow shared a context would satisfy the test above and still leak.
        assertNull(seenElsewhere,
                "A second thread could see the request path recorded by the first. Contract validation "
                        + "reads that path to name the operation, so a leak here would validate one "
                        + "test's response against another test's endpoint.");
    }
}
