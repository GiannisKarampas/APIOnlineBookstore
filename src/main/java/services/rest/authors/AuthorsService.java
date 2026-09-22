package services.rest.authors;

import static domain.RestEndpointEnum.AUTHORS;
import static domain.RestEndpointEnum.AUTHOR_BY_ID;

import java.util.function.Function;

import io.qameta.allure.Allure;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import services.rest.RestCommonValidations;
import utils.service.implementation.Rest;

/**
 * The transport layer for {@code /api/v1/Authors}. Mirrors
 * {@link services.rest.books.BooksService}, including the split between typed
 * methods and the separately named raw ones an edge-case test uses.
 */
public class AuthorsService {

    private static final String ID = "id";

    private final Rest rest;
    private final RestCommonValidations validations;

    public AuthorsService(Rest rest) {
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


    public Response getAllAuthors() {
        return rest.getRequest(AUTHORS, "");
    }

    public Response getAuthorById(int authorId) {
        return rest.getRequestWithPathParams(AUTHOR_BY_ID, "", ID, authorId);
    }

    /**
     * Requests an author by an id that is not a valid integer.
     */
    public Response getAuthorByRawId(String rawAuthorId) {
        return rest.getRequestWithPathParams(AUTHOR_BY_ID, "", ID, rawAuthorId);
    }

    public Response createAuthor(AuthorDTO author) {
        return rest.postRequest(AUTHORS, "", author);
    }

    /**
     * Submits a body exactly as given, including one that is not a valid author.
     */
    public Response createAuthorFromRawPayload(String rawPayload) {
        return rest.postRequest(AUTHORS, "", rawPayload);
    }

    /**
     * Submits a body under a content type of the caller's choosing, to check how the
     * API handles a media type it does not accept.
     */
    public Response createAuthorWithContentType(ContentType contentType, String rawPayload) {
        return rest.postRequest(AUTHORS, "", rawPayload, contentType);
    }

    /**
     * Sends a PATCH, which this API does not implement, to check it is refused.
     */
    public Response patchAuthor(int authorId, String rawPayload) {
        return rest.patchRequest(AUTHOR_BY_ID, "", rawPayload, ID, authorId);
    }

    public Response updateAuthor(int authorId, AuthorDTO author) {
        return rest.putRequest(AUTHOR_BY_ID, "", author, ID, authorId);
    }

    /**
     * Updates the author at an id that is not a valid integer.
     */
    public Response updateAuthorWithRawId(String rawAuthorId, AuthorDTO author) {
        return rest.putRequest(AUTHOR_BY_ID, "", author, ID, rawAuthorId);
    }

    /**
     * Updates an author with a body exactly as given, including an invalid one.
     */
    public Response updateAuthorFromRawPayload(int authorId, String rawPayload) {
        return rest.putRequest(AUTHOR_BY_ID, "", rawPayload, ID, authorId);
    }

    public Response deleteAuthor(int authorId) {
        return rest.deleteRequest(AUTHOR_BY_ID, "", ID, authorId);
    }

    /**
     * Deletes at an id that is not a valid integer.
     */
    public Response deleteAuthorByRawId(String rawAuthorId) {
        return rest.deleteRequest(AUTHOR_BY_ID, "", ID, rawAuthorId);
    }
}
