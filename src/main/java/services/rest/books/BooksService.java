package services.rest.books;

import static domain.RestEndpointEnum.BOOKS;
import static domain.RestEndpointEnum.BOOK_BY_ID;

import java.util.function.Function;

import io.qameta.allure.Allure;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import services.rest.RestCommonValidations;
import utils.service.implementation.Rest;

/**
 * The transport layer for {@code /api/v1/Books}: one method per documented operation,
 * no assertions. Tests read as a sentence and never build a URL themselves.
 * <p>
 * The ordinary methods are typed, so a caller passing the wrong thing is a compile
 * error. The deliberately malformed input an edge-case test needs goes through the
 * separately named {@code ...Raw...} methods, which say at the call site that
 * something invalid is being sent on purpose.
 */
public class BooksService {

    private static final String ID = "id";

    private final Rest rest;
    private final RestCommonValidations validations;

    public BooksService(Rest rest) {
        this.rest = rest;
        this.validations = new RestCommonValidations(rest);
    }

    /**
     * Runs assertions against the response of the most recent call, inside the
     * reported step named by the preceding description.
     * <p>
     * The checks are handed in rather than chained off a returned object, because a
     * step has to be open while they execute. Naming a step and then asserting
     * outside it produces a step that passed beside a test that failed, which is
     * worse than no step at all.
     * <p>
     * The lambda's value is returned, so the same method serves a chain that only
     * asserts and one that ends by reading a DTO out of the response.
     */
    public <T> T validate(Function<RestCommonValidations, T> checks) {
        String stepName = rest.context().consumeStepDescription().orElse("Verify the response");
        return Allure.step(stepName, () -> checks.apply(validations));
    }


    public Response getAllBooks() {
        return rest.getRequest(BOOKS, "");
    }

    public Response getBookById(int bookId) {
        return rest.getRequestWithPathParams(BOOK_BY_ID, "", ID, bookId);
    }

    /**
     * Requests a book by an id that is not a valid integer, to check how the API
     * rejects it.
     */
    public Response getBookByRawId(String rawBookId) {
        return rest.getRequestWithPathParams(BOOK_BY_ID, "", ID, rawBookId);
    }

    public Response createBook(BookDTO book) {
        return rest.postRequest(BOOKS, "", book);
    }

    /**
     * Submits a body exactly as given, including one that is not a valid book.
     */
    public Response createBookFromRawPayload(String rawPayload) {
        return rest.postRequest(BOOKS, "", rawPayload);
    }

    /**
     * Submits a body under a content type of the caller's choosing, to check how the
     * API handles a media type it does not accept.
     */
    public Response createBookWithContentType(ContentType contentType, String rawPayload) {
        return rest.postRequest(BOOKS, "", rawPayload, contentType);
    }

    public Response updateBook(int bookId, BookDTO book) {
        return rest.putRequest(BOOK_BY_ID, "", book, ID, bookId);
    }

    /**
     * Updates the book at an id that is not a valid integer.
     */
    public Response updateBookWithRawId(String rawBookId, BookDTO book) {
        return rest.putRequest(BOOK_BY_ID, "", book, ID, rawBookId);
    }

    /**
     * Updates a book with a body exactly as given, including an invalid one.
     */
    public Response updateBookFromRawPayload(int bookId, String rawPayload) {
        return rest.putRequest(BOOK_BY_ID, "", rawPayload, ID, bookId);
    }

    /**
     * Sends a PATCH, which this API does not implement, to check it is refused.
     */
    public Response patchBook(int bookId, String rawPayload) {
        return rest.patchRequest(BOOK_BY_ID, "", rawPayload, ID, bookId);
    }

    public Response deleteBook(int bookId) {
        return rest.deleteRequest(BOOK_BY_ID, "", ID, bookId);
    }

    /**
     * Deletes at an id that is not a valid integer.
     */
    public Response deleteBookByRawId(String rawBookId) {
        return rest.deleteRequest(BOOK_BY_ID, "", ID, rawBookId);
    }
}
