package TS_API_Books;

import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertEquals;
import static utils.TestGroups.BOOKS;
import static utils.TestGroups.EDGE_CASE;
import static utils.TestGroups.HAPPY_PATH;
import static utils.TestGroups.REGRESSION;
import static utils.TestGroups.SMOKE;

import java.util.List;

import org.testng.annotations.Test;

import base.BookstoreTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import services.rest.books.BookAssertions;
import services.rest.books.BookDTO;
import utils.data.SeededCatalogue.Books;

/**
 * GET /api/v1/Books - retrieving the whole catalogue.
 */
@Epic("Online Bookstore API")
@Feature("Books")
@Story("GET /api/v1/Books")
public class TC_API_BOOKS_01_GetAllBooks extends BookstoreTest {

    /**
     * Generous on purpose: the API is a free-tier sandbox that may be cold, and a
     * tight budget here would report infrastructure latency as a product defect.
     */
    private static final long RESPONSE_TIME_BUDGET_IN_MILLIS = 15_000;

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
            description = "The catalogue is returned as a JSON collection of well-formed books")
    public void theCatalogueIsReturnedAsJson() {
        books("Request the whole catalogue").getAllBooks();

        books("Verify the catalogue is returned as JSON within the response time budget").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyContentTypeIsJson()
                    .verifyResponseTimeIsBelow(RESPONSE_TIME_BUDGET_IN_MILLIS)
                    .verifyMatchesContract());

        List<BookDTO> catalogue = books("Read the returned books").validate(checks -> checks.asListOf(BookDTO.class));

        BookAssertions.assertAllMeetCatalogueExpectations(catalogue);
        BookAssertions.assertContainsId(catalogue, Books.FIRST);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {REGRESSION, BOOKS, HAPPY_PATH},
            description = "Every book in the catalogue is listed under its own id")
    public void bookIdsAreUnique() {
        books("Request the whole catalogue").getAllBooks();

        List<BookDTO> catalogue = books("Read the returned books").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .asListOf(BookDTO.class));

        BookAssertions.assertIdsAreUnique(catalogue);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "Reading the catalogue twice in a row returns the same books")
    public void theCatalogueIsStableAcrossCalls() {
        books("Request the catalogue a first time").getAllBooks();
        List<BookDTO> firstRead = books("Read the first response").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .asListOf(BookDTO.class));

        books("Request the catalogue a second time").getAllBooks();
        List<BookDTO> secondRead = books("Read the second response").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .asListOf(BookDTO.class));

        assertEquals(idsOf(secondRead), idsOf(firstRead),
                "A read-only endpoint returned a different set of books on two consecutive calls.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A method the resource does not support is refused with 405")
    public void anUnsupportedMethodIsRefused() {
        books("Send PATCH to a book, which the API does not implement")
                .patchBook(Books.FIRST, "{}");

        books("Verify the method is refused").validate(checks -> checks
                    .verifyStatusCode(SC_METHOD_NOT_ALLOWED));
    }

    /**
     * Only the ids are compared: the API regenerates {@code publishDate} on every
     * call, so comparing whole books would fail for a reason that is not a defect.
     */
    private List<Integer> idsOf(List<BookDTO> books) {
        return books.stream().map(BookDTO::getId).sorted().toList();
    }
}
