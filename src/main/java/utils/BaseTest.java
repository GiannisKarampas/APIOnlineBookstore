package utils;


import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.google.common.collect.ImmutableMap;

import utils.config.EnvDataConfig;
import utils.service.implementation.WebService;

/**
 * The generic test base: it gives every test method its own web service and a way to
 * name the step it is about to take, and it records the environment into the report.
 * <p>
 * The web service is held in a {@link ThreadLocal} because a service carries the
 * response of the last call. Sharing one across a parallel suite would let one test
 * assert on another test's response.
 */
public class BaseTest {

    private static final Path ALLURE_RESULTS = Path.of("test-results", "allure-results");
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BaseTest.class);
    private static final ThreadLocal<WebService> WEB_SERVICE = ThreadLocal.withInitial(WebService::new);

    private final EnvDataConfig envDataConfig = new EnvDataConfig();

    @BeforeSuite(alwaysRun = true)
    public void writeAllureEnvironment() {
        try {
            Files.createDirectories(ALLURE_RESULTS);
            // Written unconditionally. Skipping it when the file already exists meant
            // a run without `clean`, against a different environment, reported the
            // previous run's base URI.
            allureEnvironmentWriter(describeEnvironment(), ALLURE_RESULTS + File.separator);
        } catch (IOException e) {
            LOGGER.warn("Could not record the environment into the Allure report.", e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void startWithAFreshWebService(ITestResult result) {
        WEB_SERVICE.set(new WebService());
        LOGGER.info("Starting {}", result.getMethod().getMethodName());
    }

    @AfterMethod(alwaysRun = true)
    public void discardWebService() {
        WEB_SERVICE.remove();
    }

    /**
     * Names what the test is about to do and returns the service to do it with.
     * <p>
     * The description is recorded rather than reported here. Whatever happens next
     * claims it: a request wraps its own send in a step of that name, so the report
     * shows the real duration with the request and response attached inside it, and
     * an assertion chain emits it as a label. Reporting it here instead would give a
     * step that closed before the work it names had started.
     */
    public WebService step(String testStepDescription) {
        LOGGER.info("STEP: {}", testStepDescription);
        WebService webService = WEB_SERVICE.get();
        webService.context().setStepDescription(testStepDescription);
        return webService;
    }

    private ImmutableMap<String, String> describeEnvironment() {
        return ImmutableMap.<String, String>builder()
                .put("FRAMEWORK", "Online Bookstore API Test Suite")
                .put("ENVIRONMENT", envDataConfig.getEnvironment().getName())
                .put("BASE URI", envDataConfig.getRestApiUrl())
                .put("OS", System.getProperty("os.name"))
                .put("JAVA", System.getProperty("java.version"))
                .build();
    }
}
