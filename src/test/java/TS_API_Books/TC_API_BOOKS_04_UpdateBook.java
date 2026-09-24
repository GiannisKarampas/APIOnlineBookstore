package TS_API_Books;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
import io.restassured.http.ContentType;
import models.errors.ProblemDetailsDTO;
import services.rest.books.BookAssertions;
import services.rest.books.BookDTO;
import utils.data.BookFactory;
import utils.data.SeededCatalogue.Books;

/**
 * PUT /api/v1/Books/{id} - updating a book.
 */
@Epic("Online Bookstore API")
@Feature("Books")
@Story("PUT /api/v1/Books/{id}")
public class TC_API_BOOKS_04_UpdateBook extends BookstoreTest {

    /** Reserved for this class by the fixture, so no reader sees these writes. */
    private static final int A_SEEDED_BOOK_ID = Books.OWNED_BY_UPDATE_TESTS;

    @DataProvider(name = "unbindableBookIds")
    public Object[][] unbindableBookIds() {
        return new Object[][]{{"abc"}, {"1.5"}, {"2147483648"}};
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {SMOKE, REGRESSION, BOOKS, HAPPY_PATH},
            description = "An update to an existing book is acknowledged and echoed back")
    public void anUpdateToAnExistingBookIsAcknowledged() {
        BookDTO update = BookFactory.aBookWithId(A_SEEDED_BOOK_ID);

        books("Update book " + A_SEEDED_BOOK_ID + " with a new title and page count").updateBook(A_SEEDED_BOOK_ID, update);

        BookDTO updated = books("Verify the update is accepted and returned as JSON").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyContentTypeIsJson()
                    // No contract check here, for the same reason as on POST: see
                    // TC_API_CONTRACT_01_OpenApiContract.
                    .as(BookDTO.class));

        BookAssertions.assertEchoes(updated, update);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Updating an id that matches no book is accepted rather than reported as not found")
    public void updatingAnUnknownIdIsAccepted() {
        int unknownId = BookFactory.aGeneratedId();
        BookDTO update = BookFactory.aBookWithId(unknownId);

        books("Update the non-existent book " + unknownId).updateBook(unknownId, update);

        BookDTO updated = books("Verify the demo API accepts the update anyway").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        BookAssertions.assertEchoes(updated, update);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "An update is not persisted, because the demo API keeps no state")
    public void theUpdateIsNotPersisted() {
        // Read the record first, so the comparison afterwards is against what was
        // actually there rather than merely against what was submitted.
        books("Read book " + A_SEEDED_BOOK_ID + " before touching it").getBookById(A_SEEDED_BOOK_ID);
        BookDTO before = books("Capture its current state").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        BookDTO update = BookFactory.aBookWithId(A_SEEDED_BOOK_ID);
        books("Update book " + A_SEEDED_BOOK_ID).updateBook(A_SEEDED_BOOK_ID, update);
        books("Verify the update was accepted").validate(checks -> checks.verifyStatusCode(SC_OK));

        books("Read book " + A_SEEDED_BOOK_ID + " back").getBookById(A_SEEDED_BOOK_ID);
        BookDTO after = books("Verify the stored book is unchanged").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        // publishDate is regenerated on every read, so only the stable fields are
        // compared. Those are the ones the update tried to change.
        assertEquals(after.getTitle(), before.getTitle(), "The title changed, so the update was stored.");
        assertEquals(after.getPageCount(), before.getPageCount(),
                "The page count changed, so the update was stored.");
        assertNotEquals(after.getTitle(), update.getTitle(),
                "The stored book now carries the submitted title. The demo API is documented as stateless, "
                        + "so the suite's assumptions need revisiting.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "When the id in the path and the id in the body disagree, the body wins")
    public void theBodyIdWinsOverThePathId() {
        int idInPath = A_SEEDED_BOOK_ID;
        int idInBody = idInPath + 500;
        BookDTO update = BookFactory.aBookWithId(idInBody);

        books("Update book " + idInPath + " with a body claiming to be book " + idInBody)
                .updateBook(idInPath, update);

        BookDTO updated = books("Verify which id the API honoured").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .as(BookDTO.class));

        // Worth pinning down: the API silently takes the body's id and ignores the
        // path's, rather than rejecting the disagreement or preferring the path.
        // A client that trusted the path would be writing to the wrong record.
        assertEquals(updated.getId(), Integer.valueOf(idInBody),
                "The API was expected to echo the body's id, ignoring the path. If it now prefers the path "
                        + "or rejects the mismatch, that is a behaviour change worth knowing about.");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unbindableBookIds",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An update addressed to a non-integer id is rejected as a bad request")
    public void anUpdateToANonIntegerIdIsRejected(String unbindableId) {
        books("Update the book with the non-integer id '" + unbindableId + "'")
                .updateBookWithRawId(unbindableId, BookFactory.aValidBook());

        ProblemDetailsDTO problem = books("Verify the request is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("id"),
                "The problem document should name 'id' as the offending field but reported: " + problem.getErrors());
    }

    /**
     * The same bodies POST refuses. A passing POST says nothing about PUT: they are
     * separate routes and can bind their bodies differently.
     */
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
                {"id", "{\"id\":null,\"title\":\"A title\",\"pageCount\":1,\"publishDate\":\"2026-01-01T00:00:00Z\"}"},
                {"pageCount", "{\"id\":1,\"title\":\"A title\",\"pageCount\":null,\"publishDate\":\"2026-01-01T00:00:00Z\"}"},
                {"publishDate", "{\"id\":1,\"title\":\"A title\",\"pageCount\":1,\"publishDate\":null}"},
        };
    }

    @DataProvider(name = "unacceptableContentTypes")
    public Object[][] unacceptableContentTypes() {
        return new Object[][]{{"plain text", ContentType.TEXT}, {"XML", ContentType.XML}};
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unusableBodies",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An update whose body is not a book object is rejected")
    public void anUnusableBodyIsRejected(String scenario, String body) {
        books("Update book " + A_SEEDED_BOOK_ID + " with " + scenario)
                .updateBookFromRawPayload(A_SEEDED_BOOK_ID, body);

        books("Verify the request is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "nonNullableFields",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "Each non-nullable field is rejected on its own when an update sends it as null")
    public void aNullNonNullableFieldIsRejected(String field, String payload) {
        books("Update book " + A_SEEDED_BOOK_ID + " with a null " + field)
                .updateBookFromRawPayload(A_SEEDED_BOOK_ID, payload);

        ProblemDetailsDTO problem = books("Verify the request is rejected").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$." + field),
                "Expected the problem document to blame $." + field + " on its own, but it reported: "
                        + problem.getErrors());
    }

    @Severity(SeverityLevel.MINOR)
    @Test(dataProvider = "unacceptableContentTypes",
            groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An update sent under a media type the API does not accept is refused")
    public void anUnsupportedMediaTypeIsRefused(String scenario, ContentType contentType) {
        books("Update book " + A_SEEDED_BOOK_ID + " as " + scenario)
                .updateBookWithContentType(A_SEEDED_BOOK_ID, contentType,
                        "{\"id\":1,\"title\":\"A title\",\"pageCount\":1}");

        books("Verify the API refuses the media type").validate(checks -> checks
                    .verifyStatusCode(SC_UNSUPPORTED_MEDIA_TYPE));
    }

    @Severity(SeverityLevel.MINOR)
    @Test(groups = {REGRESSION, BOOKS, EDGE_CASE},
            description = "An update whose field types do not match the schema is rejected")
    public void anUpdateWithWrongFieldTypesIsRejected() {
        books("Update book " + A_SEEDED_BOOK_ID + " with a non-numeric page count")
                .updateBookFromRawPayload(A_SEEDED_BOOK_ID, "{\"id\":1,\"title\":\"A title\",\"pageCount\":\"many\"}");

        ProblemDetailsDTO problem = books("Verify the payload is rejected with a problem document").validate(checks -> checks
                    .verifyProblemDetails(SC_BAD_REQUEST));

        assertTrue(problem.getErrors().containsKey("$.pageCount"),
                "Expected the problem document to blame $.pageCount but it reported: " + problem.getErrors());
    }
}
