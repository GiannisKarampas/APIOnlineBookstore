package TS_API_Authors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
import io.restassured.path.json.JsonPath;
import models.errors.ProblemDetailsDTO;
import services.rest.authors.AuthorAssertions;
import services.rest.authors.AuthorDTO;
import utils.data.AuthorFactory;

/**
 * POST /api/v1/Authors - adding an author.
 * <p>
 * As with books, the demo API validates and echoes a payload but stores nothing, so
 * "created" here means "accepted and echoed unchanged".
 */
@Epic("Online Bookstore API")
@Feature("Authors")
@Story("POST /api/v1/Authors")
public class TC_API_AUTHORS_03_CreateAuthor extends BookstoreTest {

    private static final int OVERSIZED_TEXT_LENGTH = 5_000;

    /**
     * The contract marks id and idBook as non-nullable. Each row nulls exactly one of
     * them and leaves the rest valid, so each field is proven to be enforced on its
     * own rather than merely something being rejected.
     */
    @DataProvider(name = "nonNullableFields")
    public Object[][] nonNullableFields() {
        return new Object[][]{
                {"id", "{\"id\":null,\"idBook\":1,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"},
                {"idBook", "{\"id\":1,\"idBook\":null,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"},
        };
    }

    @DataProvider(name = "payloadsWithWrongFieldTypes")
    public Object[][] payloadsWithWrongFieldTypes() {
        return new Object[][]{
                {"a non-numeric id", "{\"id\":\"abc\",\"idBook\":1,\"firstName\":\"Ada\"}", "$.id"},
                {"a non-numeric book reference", "{\"id\":1,\"idBook\":\"one\",\"firstName\":\"Ada\"}", "$.idBook"},
                {"a numeric first name", "{\"id\":1,\"idBook\":1,\"firstName\":{}}", "$.firstName"},
        };
    }

    @DataProvider(name = "unusableBodies")
    public Object[][] unusableBodies() {
        return new Object[][]{
                {"an empty body", ""},
                {"truncated JSON", "{\"id\":"},
                {"a bare string", "\"just a string\""},
        };
    }

    @DataProvider(name = "unacceptableContentTypes")
    public Object[][] unacceptableContentTypes() {
        return new Object[][]{
                {"plain text", ContentType.TEXT},
                {"XML", ContentType.XML},
        };
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, AUTHORS, HAPPY_PATH},
            description = "A valid author is accepted and echoed back unchanged")
    public void aValidAuthorIsAccepted() {
        AuthorDTO submitted = AuthorFactory.aValidAuthor();

        authors("Submit a new author").createAuthor(submitted);

        AuthorDTO accepted = authors("Verify the author is accepted and returned as JSON")
                .validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .verifyContentTypeIsJson()
                        .verifyMatchesContract()
                        .as(AuthorDTO.class));

        AuthorAssertions.assertEchoes(accepted, submitted);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "Names far longer than the seeded data are accepted without truncation")
    public void oversizedTextIsAcceptedWithoutTruncation() {
        AuthorDTO submitted = AuthorFactory.anAuthorWithOversizedText(OVERSIZED_TEXT_LENGTH);

        authors("Submit an author with " + OVERSIZED_TEXT_LENGTH + "-character names")
                .createAuthor(submitted);

        AuthorDTO accepted = authors("Verify the long names survive the round trip")
                .validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .as(AuthorDTO.class));

        AuthorAssertions.assertEchoes(accepted, submitted);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "payloadsWithWrongFieldTypes",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A payload whose field types do not match the schema is rejected")
    public void aPayloadWithWrongFieldTypesIsRejected(String scenario, String payload,
                                                      String expectedOffendingField) {
        authors("Submit an author with " + scenario).createAuthorFromRawPayload(payload);

        ProblemDetailsDTO problem = authors("Verify the payload is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey(expectedOffendingField),
                "Expected the problem document to blame " + expectedOffendingField
                        + " but it reported: " + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "nonNullableFields",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "Each non-nullable field is rejected on its own when sent as null")
    public void aNullNonNullableFieldIsRejected(String field, String payload) {
        authors("Submit an author whose " + field + " is null").createAuthorFromRawPayload(payload);

        ProblemDetailsDTO problem = authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$." + field),
                "Expected the problem document to blame $." + field + " on its own, but it reported: "
                        + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unusableBodies",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A body that is not an author object is rejected")
    public void anUnusableBodyIsRejected(String scenario, String body) {
        authors("Submit " + scenario).createAuthorFromRawPayload(body);

        authors("Verify the request is rejected").validate(checks -> checks
                .verifyProblemDetails(SC_BAD_REQUEST));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unacceptableContentTypes",
            groups = {REGRESSION, AUTHORS, EDGE_CASE},
            description = "A body sent under a media type the API does not accept is refused")
    public void anUnsupportedMediaTypeIsRefused(String scenario, ContentType contentType) {
        authors("Submit an author as " + scenario).createAuthorWithContentType(contentType,
                "{\"id\":1,\"idBook\":1,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}");

        authors("Verify the API refuses the media type").validate(checks -> checks
                .verifyStatusCode(SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "An empty JSON object is accepted and filled with type defaults rather than rejected")
    public void anEmptyJsonObjectIsFilledWithDefaults() {
        authors("Submit an empty JSON object").createAuthorFromRawPayload("{}");

        JsonPath accepted = authors("Verify it is accepted").validate(checks -> checks
                .verifyStatusCode(SC_OK)
                .getLastResponse()
                .jsonPath());

        // Documented, not endorsed: the API applies .NET type defaults instead of
        // requiring the fields its own contract marks as non-nullable.
        assertEquals(accepted.getInt("id"), 0, "Expected the id to default to 0.");
        assertEquals(accepted.getInt("idBook"), 0, "Expected the book reference to default to 0.");
        assertNull(accepted.getString("firstName"), "Expected the name to be absent rather than invented.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Finding: an author may be attributed to a book that does not exist")
    public void anAuthorMayReferenceABookThatDoesNotExist() {
        int nonExistentBook = 999_999;
        AuthorDTO submitted = AuthorFactory.aValidAuthor().toBuilder().idBook(nonExistentBook).build();

        authors("Submit an author attributed to book " + nonExistentBook).createAuthor(submitted);

        AuthorDTO accepted = authors("Verify the API accepts the dangling reference")
                .validate(checks -> checks
                        .verifyStatusCode(SC_OK)
                        .as(AuthorDTO.class));

        // idBook is the only relationship this API models, and nothing enforces it.
        // Worth knowing: a client cannot rely on an author's book existing.
        assertEquals(accepted.getIdBook(), Integer.valueOf(nonExistentBook),
                "The API was expected to accept a book reference it cannot resolve, having no referential "
                        + "integrity. If it now rejects one, that is a behaviour change worth knowing about.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, AUTHORS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "An author accepted by POST cannot be read back, because the demo API keeps no state")
    public void theCreatedAuthorIsNotRetrievable() {
        AuthorDTO submitted = AuthorFactory.aValidAuthor();

        authors("Submit a new author").createAuthor(submitted);
        authors("Verify the author was accepted").validate(checks -> checks.verifyStatusCode(SC_OK));

        authors("Read the author back by their id").getAuthorById(submitted.getId());
        authors("Verify the demo API did not persist them").validate(checks -> checks
                .verifyStatusCode(SC_NOT_FOUND));
    }
}
