package TS_API_Authors;

import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.AUTHORS;
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
import services.rest.authors.AuthorAssertions;
import services.rest.authors.AuthorDTO;
import services.rest.books.BookDTO;

/**
 * GET /api/v1/Authors - retrieving the whole author list.
 */
@Epic("Online Bookstore API")
@Feature("Authors")
@Story("GET /api/v1/Authors")
public class TC_API_AUTHORS_01_GetAllAuthors extends BookstoreTest {

    private static final long RESPONSE_TIME_BUDGET_IN_MILLIS = 15_000;

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, AUTHORS, HAPPY_PATH},
            description = "The author list is returned as a JSON collection of well-formed authors")
    public void theAuthorListIsReturnedAsJson() {
        authors("Request the whole author list").getAllAuthors();

        List<AuthorDTO> allAuthors = authors("Verify the list is returned as JSON within budget")
                .validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .verifyContentTypeIsJson()
                        .verifyMatchesContract()
                        .verifyResponseTimeIsBelow(RESPONSE_TIME_BUDGET_IN_MILLIS)
                        .asListOf(AuthorDTO.class));

        AuthorAssertions.assertAllMeetSeededExpectations(allAuthors);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {REGRESSION, AUTHORS, HAPPY_PATH},
            description = "Every author is listed under their own id")
    public void authorIdsAreUnique() {
        authors("Request the whole author list").getAllAuthors();

        List<AuthorDTO> allAuthors = authors("Read the returned authors").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .asListOf(AuthorDTO.class));

        AuthorAssertions.assertIdsAreUnique(allAuthors);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "Every author is attributed to a book id that exists in the catalogue")
    public void everyAuthorIsAttributedToAnExistingBook() {
        authors("Request the whole author list").getAllAuthors();
        List<AuthorDTO> allAuthors = authors("Read the returned authors").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .asListOf(AuthorDTO.class));

        books("Request the whole catalogue").getAllBooks();
        List<Integer> catalogueIds = books("Read the returned books").validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .asListOf(BookDTO.class))
                .stream().map(BookDTO::getId).toList();

        List<Integer> orphaned = allAuthors.stream()
                .map(AuthorDTO::getIdBook)
                .filter(idBook -> !catalogueIds.contains(idBook))
                .distinct()
                .toList();

        assertTrue(orphaned.isEmpty(),
                "Authors are attributed to books that are not in the catalogue: " + orphaned);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "Reading the list twice in a row returns a collection of the same shape")
    public void theAuthorListIsStableAcrossCalls() {
        authors("Request the list a first time").getAllAuthors();
        List<AuthorDTO> firstRead = authors("Read the first response").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .asListOf(AuthorDTO.class));

        authors("Request the list a second time").getAllAuthors();
        List<AuthorDTO> secondRead = authors("Read the second response").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .asListOf(AuthorDTO.class));

        // Unlike the book catalogue, this collection is regenerated per call and its
        // length varies, so the ids cannot be compared. What must hold is that a
        // read-only endpoint keeps returning a well-formed, non-empty collection.
        assertFalse(firstRead.isEmpty(), "The author list was empty on the first read.");
        assertFalse(secondRead.isEmpty(), "The author list was empty on the second read.");
        AuthorAssertions.assertAllMeetSeededExpectations(firstRead);
        AuthorAssertions.assertAllMeetSeededExpectations(secondRead);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A method the resource does not support is refused with 405")
    public void anUnsupportedMethodIsRefused() {
        authors("Send PATCH to the author list, which the API does not implement")
                .patchAuthors("{}");

        authors("Verify the method is refused").validate(checks -> checks
                .verifyStatusCode(SC_METHOD_NOT_ALLOWED));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "The collection reports every author's book attribution within the catalogue range")
    public void everyAttributionIsWithinTheCatalogueRange() {
        authors("Request the whole author list").getAllAuthors();
        List<AuthorDTO> allAuthors = authors("Read the returned authors").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .asListOf(AuthorDTO.class));

        long outOfRange = allAuthors.stream()
                .map(AuthorDTO::getIdBook)
                .filter(idBook -> idBook < 1 || idBook > 200)
                .count();

        assertEquals(outOfRange, 0L,
                "The catalogue holds books 1 to 200, so no author should be attributed outside that range.");
    }
}
