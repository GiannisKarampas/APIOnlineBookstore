package TS_API_Authors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.AUTHORS;
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
import utils.data.AuthorFactory;
import utils.data.SeededCatalogue.Authors;

/**
 * DELETE /api/v1/Authors/{id} - removing an author.
 */
@Epic("Online Bookstore API")
@Feature("Authors")
@Story("DELETE /api/v1/Authors/{id}")
public class TC_API_AUTHORS_05_DeleteAuthor extends BookstoreTest {

    /** Reserved for this class by the fixture, so no reader sees these writes. */
    private static final int A_SEEDED_AUTHOR_ID = Authors.OWNED_BY_DELETE_TESTS;

    @DataProvider(name = "unbindableAuthorIds")
    public Object[][] unbindableAuthorIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, AUTHORS, HAPPY_PATH},
            description = "A deletion of an existing author is acknowledged with an empty body")
    public void aDeletionOfAnExistingAuthorIsAcknowledged() {
        authors("Delete author " + A_SEEDED_AUTHOR_ID).deleteAuthor(A_SEEDED_AUTHOR_ID);

        authors("Verify the deletion is acknowledged without a body").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .verifyBodyIsEmpty()
                .verifyMatchesContract());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableAuthorIds",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A deletion addressed to a non-integer id is rejected as a bad request")
    public void aDeletionOfANonIntegerIdIsRejected(String unbindableId) {
        authors("Delete the author with the non-integer id '" + unbindableId + "'")
                .deleteAuthorByRawId(unbindableId);

        ProblemDetailsDTO problem = authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: "
                        + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Deleting an id that matches no author is acknowledged rather than reported as not found")
    public void deletingAnUnknownIdIsAccepted() {
        int unknownId = AuthorFactory.aGeneratedId();

        authors("Delete the non-existent author " + unknownId).deleteAuthor(unknownId);

        authors("Verify the demo API acknowledges the deletion anyway").validate(checks -> checks
                .verifyStatusCode(SC_OK));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "A deletion is not persisted, because the demo API keeps no state")
    public void theDeletionIsNotPersisted() {
        authors("Delete author " + A_SEEDED_AUTHOR_ID).deleteAuthor(A_SEEDED_AUTHOR_ID);
        authors("Verify the deletion was acknowledged").validate(checks -> checks.verifyStatusCode(SC_OK));

        authors("Read the deleted author back").getAuthorById(A_SEEDED_AUTHOR_ID);
        authors("Verify the author is still there").validate(checks -> checks.verifyStatusCode(SC_OK));
    }
}
