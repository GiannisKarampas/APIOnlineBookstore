package TS_FRAMEWORK;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.FRAMEWORK;
import static utils.TestGroups.REGRESSION;

import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import models.ContextData;
import utils.service.implementation.WebService;

/**
 * The state a test carries between its steps.
 * <p>
 * Also here because it went wrong: the web service and the transport each used to
 * build their own context, so the step description was written to one object and read
 * from another — which is to say never read at all.
 */
@Epic("Framework")
@Feature("Transport")
@Story("Execution context")
public class TC_FRAMEWORK_03_ExecutionContext {

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "The web service and its transport share one context")
    public void theContextIsShared() {
        WebService webService = new WebService();

        assertSame(webService.context(), webService.rest().context(),
                "Two contexts means whatever a step records is read back from a different object, "
                        + "so it is silently lost.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A step description is delivered once and then cleared")
    public void aDescriptionIsConsumedOnce() {
        ContextData context = new ContextData();
        context.setStepDescription("Request the whole catalogue");

        assertEquals(context.consumeStepDescription().orElse(null), "Request the whole catalogue",
                "The first consumer should receive the description that was recorded.");
        assertTrue(context.consumeStepDescription().isEmpty(),
                "A description left behind after being used would surface as the name of an unrelated "
                        + "later step, mislabelling whatever ran next.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Asking for a description that was never set yields nothing rather than failing")
    public void anAbsentDescriptionIsEmpty() {
        assertFalse(new ContextData().consumeStepDescription().isPresent(),
                "A call made without a named step should fall back to a generated name, not throw.");
    }
}
