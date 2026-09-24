package TS_API_Books;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static utils.TestGroups.BOOKS;
import static utils.TestGroups.EDGE_CASE;
import static utils.TestGroups.HAPPY_PATH;
import static utils.TestGroups.PROVIDER_BEHAVIOUR;
import static utils.TestGroups.REGRESSION;
import static utils.TestGroups.SMOKE;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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
import services.rest.books.BookAssertions;
import services.rest.books.BookDTO;
import utils.data.BookFactory;

/**
 * POST /api/v1/Books - adding a book.
 * <p>
 * The service under test is a demo API that validates and echoes a payload but keeps
 * no state, so "created" here means "accepted and echoed unchanged". The test that
 * documents the missing persistence is {@link #theCreatedBookIsNotRetrievable()}.
 */
@Epic("Online Bookstore API")
@Feature("Books")
@Story("POST /api/v1/Books")
public class TC_API_BOOKS_03_CreateBook extends BookstoreTest {

    private static final int OVERSIZED_TEXT_LENGTH = 5_000;

    /**
     * Payloads whose field types do not match the schema, paired with the field the
     * API is expected to complain about.
     */
    @DataProvider(name = "payloadsWithWrongFieldTypes")
    public Object[][] payloadsWithWrongFieldTypes() {
        return new Object[][]{
                {"a non-numeric id", "{\"id\":\"abc\",\"title\":\"A title\",\"pageCount\":1}", "$.id"},
                {"a non-numeric page count", "{\"id\":1,\"title\":\"A title\",\"pageCount\":\"many\"}", "$.pageCount"},
                {"an unparsable publish date", "{\"id\":1,\"publishDate\":\"not-a-date\"}", "$.publishDate"},
        };
    }

    /**
     * The contract marks id, pageCount and publishDate as non-nullable. Each row
     * nulls exactly one of them and leaves the rest valid.
     */
    @DataProvider(name = "nonNullableFields")
    public Object[][] nonNullableFields() {
        return new Object[][]{
                {"id", validBookWith("\"id\":null")},
                {"pageCount", validBookWith("\"pageCount\":null")},
                {"publishDate", validBookWith("\"publishDate\":null")},
        };
    }

    @DataProvider(name = "unacceptableContentTypes")
    public Object[][] unacceptableContentTypes() {
        return new Object[][]{
                {"plain text", ContentType.TEXT},
                {"XML", ContentType.XML},
        };
    }

    /**
     * Bodies that are not a JSON object at all.
     */
    @DataProvider(name = "unusableBodies")
    public Object[][] unusableBodies() {
        return new Object[][]{
                {"an empty body", ""},
                {"truncated JSON", "{\"id\":"},
                {"a bare string", "\"just a string\""},
        };
    }

    /**
     * A complete, valid book payload with one field replaced by the given fragment.
     */
    private static String validBookWith(String replacement) {
        String field = replacement.substring(1, replacement.indexOf("\":"));
        String complete = "{\"id\":1,\"title\":\"A title\",\"description\":\"d\","
                + "\"pageCount\":1,\"excerpt\":\"e\",\"publishDate\":\"2026-01-01T00:00:00Z\"}";
        return complete.replaceAll("\"" + field + "\":(\"[^\"]*\"|[^,}]*)", java.util.regex.Matcher
                .quoteReplacement(replacement));
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
            description = "A valid book is accepted and echoed back unchanged")
    public void aValidBookIsAccepted() {
        BookDTO submitted = BookFactory.aValidBook();

        books("Submit a new book titled '" + submitted.getTitle() + "'").createBook(submitted);

        BookDTO accepted = books("Verify the book is accepted and returned as JSON").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyContentTypeIsJson()
                    // No contract check here: the published document declares no body on
                    // this operation although the API returns one. That gap is asserted by
                    // name in TC_API_CONTRACT_01_OpenApiContract.
                    .as(BookDTO.class));

        BookAssertions.assertEchoes(accepted, submitted);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "Text fields far longer than the seeded data are accepted without truncation")
    public void oversizedTextIsAcceptedWithoutTruncation() {
        BookDTO submitted = BookFactory.aBookWithOversizedText(OVERSIZED_TEXT_LENGTH);

        books("Submit a book with " + OVERSIZED_TEXT_LENGTH + "-character text fields").createBook(submitted);

        BookDTO accepted = books("Verify the long text survives the round trip").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        BookAssertions.assertEchoes(accepted, submitted);
    }

    /**
     * Text that is entirely valid and entirely unlike what the generator produces.
     * <p>
     * Generated happy-path data is ASCII words, so nothing in the suite would notice a
     * field limited by bytes rather than characters, an encoding that mangles
     * astral-plane code points, or escaping that breaks on a quote. These rows are
     * fixed rather than generated, so a failure names the exact input every time.
     */
    @DataProvider(name = "awkwardButValidText")
    public Object[][] awkwardButValidText() {
        return new Object[][]{
                {"CJK characters", "\u66f8\u7c4d\u306e\u30bf\u30a4\u30c8\u30eb"},
                {"emoji outside the basic plane", "A title \ud83d\udcda\ud83d\udd16"},
                {"quotes and a backslash", "He said \"hello\" \\ then left"},
                {"a newline and a tab", "Line one\nLine two\tindented"},
                {"right-to-left text", "\u0639\u0646\u0648\u0627\u0646 \u0627\u0644\u0643\u062a\u0627\u0628"},
        };
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "awkwardButValidText",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "Valid but awkward text survives the round trip unchanged")
    public void awkwardTextSurvivesTheRoundTrip(String scenario, String title) {
        BookDTO submitted = BookFactory.aValidBook().toBuilder().title(title).build();

        books("Submit a book whose title is " + scenario).createBook(submitted);

        BookDTO accepted = books("Verify the text comes back byte for byte").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        BookAssertions.assertEchoes(accepted, submitted);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A publish date carrying a non-UTC offset keeps its instant")
    public void aNonUtcPublishDateKeepsItsInstant() {
        // Every generated date is UTC, so nothing else in the suite would notice an
        // offset being dropped rather than converted - which silently moves a date.
        OffsetDateTime twoHoursAhead = OffsetDateTime.now(ZoneOffset.ofHours(2)).truncatedTo(ChronoUnit.SECONDS);
        BookDTO submitted = BookFactory.aValidBook().toBuilder().publishDate(twoHoursAhead).build();

        books("Submit a book published at " + twoHoursAhead).createBook(submitted);

        BookDTO accepted = books("Verify the instant survives, however it is rendered").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        // The instant, not the offset: normalising +02:00 to UTC is a representation
        // choice, but changing the moment in time is data loss.
        assertEquals(accepted.getPublishDate().toInstant(), submitted.getPublishDate().toInstant(),
                "The publish date moved in time. Submitted " + submitted.getPublishDate()
                        + ", got back " + accepted.getPublishDate() + ".");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "payloadsWithWrongFieldTypes",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A payload whose field types do not match the schema is rejected")
    public void aPayloadWithWrongFieldTypesIsRejected(String scenario, String payload, String expectedOffendingField) {
        books("Submit a book with " + scenario).createBookFromRawPayload(payload);

        ProblemDetailsDTO problem = books("Verify the payload is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey(expectedOffendingField),
                "Expected the problem document to blame " + expectedOffendingField + " but it reported: " + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unusableBodies",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A body that is not a book object is rejected")
    public void anUnusableBodyIsRejected(String scenario, String body) {
        books("Submit " + scenario).createBookFromRawPayload(body);

        books("Verify the request is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "nonNullableFields",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "Each non-nullable field is rejected on its own when sent as null")
    public void aNullNonNullableFieldIsRejected(String field, String payload) {
        // One field at a time. Nulling all of them at once proves only that the API
        // rejected something, not that it enforces each field independently.
        books("Submit a book whose " + field + " is null").createBookFromRawPayload(payload);

        ProblemDetailsDTO problem = books("Verify the request is rejected").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$." + field),
                "Expected the problem document to blame $." + field + " on its own, but it reported: "
                        + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Finding: an empty JSON object is filled with defaults, one of which is not a valid date-time")
    public void anEmptyJsonObjectIsFilledWithDefaults() {
        books("Submit an empty JSON object").createBookFromRawPayload("{}");

        // Read as raw JSON rather than as a BookDTO on purpose. The defaulted
        // publishDate cannot be deserialized at all, which is the finding.
        JsonPath accepted = books("Verify it is accepted").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .getLastResponse()
                    .jsonPath());

        // Documented, not endorsed: the API applies .NET type defaults instead of
        // requiring the fields its own contract marks as non-nullable.
        assertEquals(accepted.getInt("id"), 0, "Expected the id to default to 0.");
        assertEquals(accepted.getInt("pageCount"), 0, "Expected the page count to default to 0.");
        assertNull(accepted.getString("title"), "Expected the title to be absent rather than invented.");

        // The contract declares publishDate as format: date-time, which requires an
        // offset. The default carries none, so a client generated from the contract
        // cannot parse the API's own response. This suite's BookDTO rejects it too.
        String defaultedDate = accepted.getString("publishDate");
        assertFalse(defaultedDate.endsWith("Z") || defaultedDate.matches(".*[+-]\\d{2}:\\d{2}$"),
                "The defaulted publishDate now carries a UTC offset, so it has become a valid date-time. "
                        + "That is the provider fixing a defect: this expectation should be tightened. Got: "
                        + defaultedDate);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "A negative page count is accepted, so the API applies no semantic validation")
    public void aNegativePageCountIsAccepted() {
        BookDTO submitted = BookFactory.aValidBook().toBuilder().pageCount(-5).build();

        books("Submit a book with a page count of -5").createBook(submitted);

        BookDTO accepted = books("Verify the API accepts a nonsensical page count").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        assertEquals(accepted.getPageCount(), Integer.valueOf(-5),
                "The API was expected to store the negative page count unchanged, having no semantic validation. "
                        + "If it now rejects or clamps it, this expectation needs revisiting.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unacceptableContentTypes",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "A body sent under a media type the API does not accept is refused")
    public void anUnsupportedMediaTypeIsRefused(String scenario, ContentType contentType) {
        books("Submit a book as " + scenario)
                .createBookWithContentType(contentType, "{\"id\":1,\"title\":\"A title\",\"pageCount\":1}");

        books("Verify the API refuses the media type").validate(checks -> checks
                    .verifyStatusCode(SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "A book accepted by POST cannot be read back, because the demo API keeps no state")
    public void theCreatedBookIsNotRetrievable() {
        BookDTO submitted = BookFactory.aValidBook();

        books("Submit a new book").createBook(submitted);
        books("Verify the book was accepted").validate(checks -> checks.verifyStatusCode(SC_OK));

        books("Read the book back by its id").getBookById(submitted.getId());
        books("Verify the demo API did not persist it").validate(checks -> checks.verifyStatusCode(SC_NOT_FOUND));

        assertFalse(submitted.getId() < BookFactory.GENERATED_ID_FLOOR,
                "Generated books must use ids above the seeded catalogue so this check cannot collide with real data.");
    }
}
