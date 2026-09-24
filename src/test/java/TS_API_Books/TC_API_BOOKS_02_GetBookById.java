package TS_API_Books;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.BOOKS;
import static utils.TestGroups.EDGE_CASE;
import static utils.TestGroups.HAPPY_PATH;
import static utils.TestGroups.REGRESSION;
import static utils.TestGroups.SMOKE;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.BookstoreTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import models.errors.ProblemDetailsDTO;
import services.rest.books.BookAssertions;
import services.rest.books.BookDTO;
import utils.data.SeededCatalogue.Books;

/**
 * GET /api/v1/Books/{id} - retrieving a single book.
 */
@Epic("Online Bookstore API")
@Feature("Books")
@Story("GET /api/v1/Books/{id}")
public class TC_API_BOOKS_02_GetBookById extends BookstoreTest {

    /**
      * Only ids the fixture marks read-only, so this class can run beside the
      * classes that write.
      */
    @DataProvider(name = "seededBookIds")
    public Object[][] seededBookIds() {
        return Books.READ_ONLY.stream().map(id -> new Object[]{id}).toArray(Object[][]::new);
    }

    /**
     * Ids that are syntactically valid integers but match no book.
     * <p>
     * {@code 0} and {@code -1} sit immediately below the seeded range and {@code 201}
     * immediately above it, so an off-by-one at either edge surfaces here. The larger
     * values are the same equivalence class further out, with {@code MAX_VALUE} kept
     * as the partner to {@code 2147483648} below: one binds to an int and misses the
     * lookup, the next cannot bind at all.
     */
    @DataProvider(name = "unknownBookIds")
    public Object[][] unknownBookIds() {
        return new Object[][]{{0}, {-1}, {201}, {9_999}, {Integer.MAX_VALUE}};
    }

    /**
     * Ids the routing layer cannot bind to an integer at all, including a value one
     * past {@link Integer#MAX_VALUE}.
     */
    @DataProvider(name = "unbindableBookIds")
    public Object[][] unbindableBookIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {" "}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
            description = "An existing book is returned in full")
    public void anExistingBookIsReturned() {
        int requestedId = Books.FIRST;

        books("Request the book with id " + requestedId).getBookById(requestedId);

        BookDTO book = books("Verify a well-formed book is returned as JSON").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyContentTypeIsJson()
                    .verifyMatchesContract()
                    .as(BookDTO.class));

        BookAssertions.assertMeetsCatalogueExpectations(book);
        assertEquals(book.getId(), Integer.valueOf(requestedId), "The API returned a different book than requested.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(dataProvider = "seededBookIds",
            groups = {REGRESSION, BOOKS, HAPPY_PATH},
            description = "Each book of the seeded catalogue can be fetched by its id")
    public void aSeededBookIsReturned(int bookId) {
        books("Request the book with id " + bookId).getBookById(bookId);

        BookDTO book = books("Verify the requested book is returned").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        BookAssertions.assertMeetsCatalogueExpectations(book);
        assertEquals(book.getId(), Integer.valueOf(bookId), "The API returned a different book than requested.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unknownBookIds",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An id that matches no book is reported as not found")
    public void anUnknownIdIsNotFound(int unknownId) {
        books("Request the book with id " + unknownId).getBookById(unknownId);

        books("Verify the API reports the book as not found").validate(checks -> checks
                    .verifyStatusCode(SC_NOT_FOUND));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A method this resource does not support is refused with 405")
    public void anUnsupportedMethodIsRefused() {
        books("Send PATCH to book " + Books.FIRST + ", which the API does not implement")
                .patchBook(Books.FIRST, "{}");

        books("Verify the method is refused").validate(checks -> checks
                    .verifyStatusCode(SC_METHOD_NOT_ALLOWED));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableBookIds",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An id that is not an integer is rejected as a bad request")
    public void aNonIntegerIdIsRejected(String unbindableId) {
        books("Request a book with the non-integer id '" + unbindableId + "'").getBookByRawId(unbindableId);

        ProblemDetailsDTO problem = books("Verify the request is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: " + problem.getErrors());
    }
}
