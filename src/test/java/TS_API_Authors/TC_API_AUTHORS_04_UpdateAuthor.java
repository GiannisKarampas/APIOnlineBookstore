package TS_API_Authors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
import io.restassured.http.ContentType;
import models.errors.ProblemDetailsDTO;
import services.rest.authors.AuthorAssertions;
import services.rest.authors.AuthorDTO;
import utils.data.AuthorFactory;
import utils.data.SeededCatalogue.Authors;

/**
 * PUT /api/v1/Authors/{id} - updating an author.
 */
@Epic("Online Bookstore API")
@Feature("Authors")
@Story("PUT /api/v1/Authors/{id}")
public class TC_API_AUTHORS_04_UpdateAuthor extends BookstoreTest {

    /** Reserved for this class by the fixture, so no reader sees these writes. */
    private static final int A_SEEDED_AUTHOR_ID = Authors.OWNED_BY_UPDATE_TESTS;

    @DataProvider(name = "unbindableAuthorIds")
    public Object[][] unbindableAuthorIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, AUTHORS, HAPPY_PATH},
            description = "An update to an existing author is acknowledged and echoed back")
    public void anUpdateToAnExistingAuthorIsAcknowledged() {
        AuthorDTO update = AuthorFactory.anAuthorWithId(A_SEEDED_AUTHOR_ID);

        authors("Update author " + A_SEEDED_AUTHOR_ID).updateAuthor(A_SEEDED_AUTHOR_ID, update);

        AuthorDTO updated = authors("Verify the update is accepted and returned as JSON")
                .validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .verifyContentTypeIsJson()
                        .verifyMatchesContract()
                        .as(AuthorDTO.class));

        AuthorAssertions.assertEchoes(updated, update);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableAuthorIds",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An update addressed to a non-integer id is rejected as a bad request")
    public void anUpdateToANonIntegerIdIsRejected(String unbindableId) {
        authors("Update the author at the non-integer id '" + unbindableId + "'")
                .updateAuthorWithRawId(unbindableId, AuthorFactory.aValidAuthor());

        ProblemDetailsDTO problem = authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: "
                        + problem.getErrors());
    }

    @DataProvider(name = "unusableBodies")
    public Object[][] unusableBodies() {
        return new Object[][]{
                {"an empty body", ""},
                {"truncated JSON", "{\"id\":"},
                {"a bare string", "\"just a string\""},
        };
    }

    @DataProvider(name = "nonNullableFields")
    public Object[][] nonNullableFields() {
        return new Object[][]{
                {"id", "{\"id\":null,\"idBook\":1,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"},
                {"idBook", "{\"id\":1,\"idBook\":null,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"},
        };
    }

    @DataProvider(name = "unacceptableContentTypes")
    public Object[][] unacceptableContentTypes() {
        return new Object[][]{{"plain text", ContentType.TEXT}, {"XML", ContentType.XML}};
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unusableBodies",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An update whose body is not an author object is rejected")
    public void anUnusableBodyIsRejected(String scenario, String body) {
        authors("Update author " + A_SEEDED_AUTHOR_ID + " with " + scenario)
                .updateAuthorFromRawPayload(A_SEEDED_AUTHOR_ID, body);

        authors("Verify the request is rejected with a problem document").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "nonNullableFields",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "Each non-nullable field is rejected on its own when an update sends it as null")
    public void aNullNonNullableFieldIsRejected(String field, String payload) {
        authors("Update author " + A_SEEDED_AUTHOR_ID + " with a null " + field)
                .updateAuthorFromRawPayload(A_SEEDED_AUTHOR_ID, payload);

        ProblemDetailsDTO problem = authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$." + field),
                "Expected the problem document to blame $." + field + " on its own, but it reported: "
                        + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unacceptableContentTypes",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An update sent under a media type the API does not accept is refused")
    public void anUnsupportedMediaTypeIsRefused(String scenario, ContentType contentType) {
        authors("Update author " + A_SEEDED_AUTHOR_ID + " as " + scenario)
                .updateAuthorWithContentType(A_SEEDED_AUTHOR_ID, contentType,
                        "{\"id\":1,\"idBook\":1,\"firstName\":\"Ada\"}");

        authors("Verify the API refuses the media type").validate(checks -> checks
                .verifyStatusCode(SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "An update whose field types do not match the schema is rejected")
    public void anUpdateWithWrongFieldTypesIsRejected() {
        authors("Update author " + A_SEEDED_AUTHOR_ID + " with a non-numeric book reference")
                .updateAuthorFromRawPayload(A_SEEDED_AUTHOR_ID,
                        "{\"id\":1,\"idBook\":\"one\",\"firstName\":\"Ada\",\"lastName\":\"L\"}");

        ProblemDetailsDTO problem = authors("Verify the payload is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$.idBook"),
                "Expected the problem document to blame $.idBook but it reported: " + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "When the id in the path and the id in the body disagree, the body wins")
    public void theBodyIdWinsOverThePathId() {
        int idInPath = A_SEEDED_AUTHOR_ID;
        int idInBody = idInPath + 500;
        AuthorDTO update = AuthorFactory.anAuthorWithId(idInBody);

        authors("Update author " + idInPath + " with a body claiming to be author " + idInBody)
                .updateAuthor(idInPath, update);

        AuthorDTO updated = authors("Verify which id the API honoured").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .as(AuthorDTO.class));

        // Same behaviour as Books: the path is ignored. A client that trusted it
        // would be writing to the wrong record.
        assertEquals(updated.getId(), Integer.valueOf(idInBody),
                "The API was expected to echo the body's id, ignoring the path. If it now prefers the "
                        + "path or rejects the mismatch, that is a behaviour change worth knowing about.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Updating an id that matches no author is accepted rather than reported as not found")
    public void updatingAnUnknownAuthorIsAccepted() {
        int unknownId = AuthorFactory.aGeneratedId();
        AuthorDTO update = AuthorFactory.anAuthorWithId(unknownId);

        authors("Update the non-existent author " + unknownId).updateAuthor(unknownId, update);

        AuthorDTO updated = authors("Verify the demo API accepts it anyway").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .as(AuthorDTO.class));

        AuthorAssertions.assertEchoes(updated, update);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "An update is not persisted, because the demo API keeps no state")
    public void theUpdateIsNotPersisted() {
        // Read the record first, so the comparison afterwards is against what was
        // actually there rather than merely against what was submitted.
        authors("Read author " + A_SEEDED_AUTHOR_ID + " before touching them")
                .getAuthorById(A_SEEDED_AUTHOR_ID);
        AuthorDTO before = authors("Capture their current state").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .as(AuthorDTO.class));

        AuthorDTO update = AuthorFactory.anAuthorWithId(A_SEEDED_AUTHOR_ID);
        authors("Update author " + A_SEEDED_AUTHOR_ID).updateAuthor(A_SEEDED_AUTHOR_ID, update);
        authors("Verify the update was accepted").validate(checks -> checks.verifyStatusCode(SC_OK));

        authors("Read author " + A_SEEDED_AUTHOR_ID + " back").getAuthorById(A_SEEDED_AUTHOR_ID);
        AuthorDTO after = authors("Verify the stored author is unchanged").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .as(AuthorDTO.class));

        assertEquals(after.getFirstName(), before.getFirstName(),
                "The first name changed, so the update was stored.");
        assertEquals(after.getLastName(), before.getLastName(),
                "The last name changed, so the update was stored.");
        assertNotEquals(after.getFirstName(), update.getFirstName(),
                "The stored author now carries the submitted name. The demo API is documented as "
                        + "stateless, so the suite's assumptions need revisiting.");
    }
}
