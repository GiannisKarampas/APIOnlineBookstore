package TS_FRAMEWORK;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
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
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import services.rest.books.BooksService;
import utils.data.BookFactory;
import utils.service.implementation.Rest;

/**
 * One request must not be able to change the next one.
 * <p>
 * This is here because it already went wrong once. An overload that set a content
 * type did so on the shared specification, which Rest Assured mutates in place, so a
 * test that sent one text/plain body left every later request in that test sending
 * text/plain too. Sixty-six passing API tests did not notice, because none of them
 * sent a normal request after an unusual one.
 */
@Epic("Framework")
@Feature("Transport")
@Story("Request isolation")
public class TC_FRAMEWORK_01_RequestIsolation {

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A request sent under an unusual content type does not change the next one")
    public void aContentTypeOverrideDoesNotLeak() {
        // Driven through the service and captured at dispatch, so this asserts on
        // what would actually have been sent. An earlier version inspected the
        // specification objects instead, which meant restoring the original defect -
        // it lived in the service overload - would have left this test green.
        CapturedRequests captured = new CapturedRequests();
        BooksService books = new Rest("http://bookstore.invalid", captured).service().books();

        books.createBookWithContentType(ContentType.TEXT, "{}");
        books.createBook(BookFactory.aValidBook());

        List<String> sent = captured.contentTypes();
        assertEquals(sent.size(), 2, "Both requests should have been dispatched.");
        assertTrue(sent.get(0).startsWith("text/plain"),
                "The first request was meant to go out as text/plain but was: " + sent.get(0));
        assertTrue(sent.get(1).startsWith("application/json"),
                "The second request went out as " + sent.get(1) + ". The content type from the previous "
                        + "call leaked into it: Rest Assured mutates a specification in place, so an "
                        + "override has to be applied to one built for that single call.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "Each call receives its own request specification")
    public void eachCallGetsItsOwnSpecification() {
        Rest rest = new Rest();

        assertNotSame(rest.getRequestSpec(), rest.getRequestSpec(),
                "Two calls shared one specification object, so anything either sets would be seen by the other.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "An overridden specification really does carry the requested content type")
    public void anOverriddenSpecificationCarriesTheRequestedType() {
        Rest rest = new Rest();

        assertEquals(contentTypeOf(rest.getRequestSpec(ContentType.TEXT)), ContentType.TEXT.toString(),
                "The override did not take effect at all, so the isolation check above would prove nothing.");
    }

    private String contentTypeOf(RequestSpecification specification) {
        return ((FilterableRequestSpecification) specification).getContentType();
    }
}
