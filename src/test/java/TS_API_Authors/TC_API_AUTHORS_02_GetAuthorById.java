package TS_API_Authors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.AUTHORS;
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
import services.rest.authors.AuthorAssertions;
import services.rest.authors.AuthorDTO;
import utils.data.SeededCatalogue.Authors;

/**
 * GET /api/v1/Authors/{id} - retrieving a single author.
 */
@Epic("Online Bookstore API")
@Feature("Authors")
@Story("GET /api/v1/Authors/{id}")
public class TC_API_AUTHORS_02_GetAuthorById extends BookstoreTest {

    /**
     * Only ids the fixture marks read-only, so this class can run beside the classes
     * that write.
     */
    @DataProvider(name = "seededAuthorIds")
    public Object[][] seededAuthorIds() {
        return Authors.READ_ONLY.stream().map(id -> new Object[]{id}).toArray(Object[][]::new);
    }

    /**
     * Ids that are valid integers but match no author.
     */
    @DataProvider(name = "unknownAuthorIds")
    public Object[][] unknownAuthorIds() {
        return new Object[][]{{0}, {-1}, {999_999}, {Integer.MAX_VALUE}};
    }

    /**
     * Ids the routing layer cannot bind to an integer, including one past
     * {@link Integer#MAX_VALUE}.
     */
    @DataProvider(name = "unbindableAuthorIds")
    public Object[][] unbindableAuthorIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {" "}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, AUTHORS, HAPPY_PATH},
            description = "An existing author is returned in full")
    public void anExistingAuthorIsReturned() {
        int requestedId = Authors.FIRST;

        authors("Request the author with id " + requestedId).getAuthorById(requestedId);

        AuthorDTO author = authors("Verify a well-formed author is returned").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .verifyContentTypeIsJson()
                .verifyMatchesContract()
                .as(AuthorDTO.class));

        AuthorAssertions.assertMeetsSeededExpectations(author);
        assertEquals(author.getId(), Integer.valueOf(requestedId),
                "The API returned a different author than requested.");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(dataProvider = "seededAuthorIds",
            groups = {REGRESSION, AUTHORS, HAPPY_PATH},
            description = "Each seeded author can be fetched by their id")
    public void aSeededAuthorIsReturned(int authorId) {
        authors("Request the author with id " + authorId).getAuthorById(authorId);

        AuthorDTO author = authors("Verify the requested author is returned").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .as(AuthorDTO.class));

        AuthorAssertions.assertMeetsSeededExpectations(author);
        assertEquals(author.getId(), Integer.valueOf(authorId),
                "The API returned a different author than requested.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unknownAuthorIds",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An id that matches no author is reported as not found")
    public void anUnknownIdIsNotFound(int unknownId) {
        authors("Request the author with id " + unknownId).getAuthorById(unknownId);

        authors("Verify the API reports the author as not found").validate(checks -> checks
                .verifyStatusCode(SC_NOT_FOUND));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A method this resource does not support is refused with 405")
    public void anUnsupportedMethodIsRefused() {
        authors("Send PATCH to author " + Authors.FIRST + ", which the API does not implement")
                .patchAuthor(Authors.FIRST, "{}");

        authors("Verify the method is refused").validate(checks -> checks
                .verifyStatusCode(SC_METHOD_NOT_ALLOWED));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableAuthorIds",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An id that is not an integer is rejected as a bad request")
    public void aNonIntegerIdIsRejected(String unbindableId) {
        authors("Request an author with the non-integer id '" + unbindableId + "'")
                .getAuthorByRawId(unbindableId);

        ProblemDetailsDTO problem = authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: "
                        + problem.getErrors());
    }
}
