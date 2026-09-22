package TS_API_Books;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.BOOKS;
import static utils.TestGroups.EDGE_CASE;
import static utils.TestGroups.HAPPY_PATH;
import static utils.TestGroups.PROVIDER_BEHAVIOUR;
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
import utils.data.BookFactory;
import utils.data.SeededCatalogue.Books;

/**
 * DELETE /api/v1/Books/{id} - removing a book.
 */
@Epic("Online Bookstore API")
@Feature("Books")
@Story("DELETE /api/v1/Books/{id}")
public class TC_API_BOOKS_05_DeleteBook extends BookstoreTest {

    /** Reserved for this class by the fixture, so no reader sees these writes. */
    private static final int A_SEEDED_BOOK_ID = Books.OWNED_BY_DELETE_TESTS;

    @DataProvider(name = "unbindableBookIds")
    public Object[][] unbindableBookIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
            description = "Deleting an existing book is acknowledged with an empty body")
    public void anExistingBookIsDeleted() {
        books("Delete book " + A_SEEDED_BOOK_ID).deleteBook(A_SEEDED_BOOK_ID);

        books("Verify the deletion is acknowledged without a body").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyBodyIsEmpty()
                    .verifyMatchesContract());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Deleting an id that matches no book is acknowledged rather than reported as not found")
    public void deletingAnUnknownIdIsAccepted() {
        int unknownId = BookFactory.aGeneratedId();

        books("Delete the non-existent book " + unknownId).deleteBook(unknownId);

        books("Verify the demo API acknowledges the deletion anyway").validate(checks -> checks
                    .verifyStatusCode(SC_OK));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "A deletion is not persisted, because the demo API keeps no state")
    public void theDeletionIsNotPersisted() {
        books("Delete book " + A_SEEDED_BOOK_ID).deleteBook(A_SEEDED_BOOK_ID);
        books("Verify the deletion was acknowledged").validate(checks -> checks.verifyStatusCode(SC_OK));

        books("Read the deleted book back").getBookById(A_SEEDED_BOOK_ID);
        books("Verify the book is still there").validate(checks -> checks.verifyStatusCode(SC_OK));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableBookIds",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A deletion addressed to a non-integer id is rejected as a bad request")
    public void aDeletionOfANonIntegerIdIsRejected(String unbindableId) {
        books("Delete the book with the non-integer id '" + unbindableId + "'").deleteBookByRawId(unbindableId);

        ProblemDetailsDTO problem = books("Verify the request is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: " + problem.getErrors());
    }
}
