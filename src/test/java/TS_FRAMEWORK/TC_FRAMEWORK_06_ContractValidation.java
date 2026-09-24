package TS_FRAMEWORK;

import static org.testng.Assert.assertFalse;
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
import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.Filter;
import services.rest.books.BooksService;
import utils.service.implementation.Rest;

/**
 * Proof that contract validation can still fail.
 * <p>
 * Every {@code verifyMatchesContract()} in the API suite asserts that the validator
 * reported nothing. If the validator ever stopped reporting - a bad upgrade, a
 * snapshot that no longer loads, an operation it cannot resolve - all of those
 * assertions would pass while checking nothing at all, and the suite would look
 * greener than before.
 * <p>
 * The API suite guards against that from one direction: the two documentation
 * findings assert the validator <em>does</em> produce specific messages. These tests
 * guard from the other, and without a socket: a response that is deliberately wrong
 * is fed through the real validation path and must be rejected.
 */
@Epic("Framework")
@Feature("Contract")
@Story("Contract validation is live")
public class TC_FRAMEWORK_06_ContractValidation {

    private static final String UNRESOLVABLE = "http://bookstore.invalid";

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A response whose field types contradict the schema is reported as a violation")
    public void aMalformedResponseFailsValidation() {
        BooksService books = booksAnsweredWith("{\"id\":\"not-a-number\",\"pageCount\":\"many\"}");

        books.getBookById(1);
        List<String> violations = books.validate(checks -> checks.contractViolations());

        assertFalse(violations.isEmpty(),
                "A book whose id is a string and whose page count is a word satisfied the published "
                        + "schema. Contract validation is not running, which means every "
                        + "verifyMatchesContract() in the API suite is passing without checking anything.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {FRAMEWORK, REGRESSION},
            description = "A well-formed response produces no violations, so the check is not simply always red")
    public void aWellFormedResponsePassesValidation() {
        BooksService books = booksAnsweredWith(
                "{\"id\":1,\"title\":\"A title\",\"description\":\"d\",\"pageCount\":100,"
                        + "\"excerpt\":\"e\",\"publishDate\":\"2026-01-01T00:00:00Z\"}");

        books.getBookById(1);
        List<String> violations = books.validate(checks -> checks.contractViolations());

        // The negative control. Without it, a validator that rejected everything would
        // satisfy the test above and still be useless.
        assertTrue(violations.isEmpty(),
                "A valid book was reported as violating the contract: " + violations);
    }

    /**
     * A Books service whose requests are answered in memory with the given body.
     * <p>
     * The filter never calls {@code ctx.next(...)}, and the base URI cannot resolve,
     * so nothing is transmitted - but the response travels the real validation path,
     * including the templated request path the validator looks the operation up by.
     */
    private BooksService booksAnsweredWith(String body) {
        Filter answerInMemory = (request, responseSpec, ctx) -> new ResponseBuilder()
                .setStatusCode(200)
                .setContentType("application/json; charset=utf-8")
                .setBody(body)
                .build();
        return new Rest(UNRESOLVABLE, answerInMemory).service().books();
    }
}
