package models;

import java.util.Optional;

import io.restassured.http.Method;
import io.restassured.response.Response;
import lombok.Data;

/**
 * The state a single test carries between its steps.
 * <p>
 * There is exactly one of these per test method. {@code WebService.context()} and
 * {@code Rest.context()} return the same instance, so the step that names an action,
 * the transport that performs it and the step that asserts on it all read and write
 * the same object.
 * <p>
 * The templated path and the method are kept alongside the response because checking
 * a response against the OpenAPI contract means naming the operation it belongs to,
 * and {@code /api/v1/Books/{id}} is the operation, while {@code /api/v1/Books/1} is
 * merely one call to it.
 */
@Data
public class ContextData {

    private String stepDescription;
    private Response lastResponse;
    private String lastRequestPath;
    private Method lastRequestMethod;

    /**
     * Takes the pending step description and clears it, so that a description is
     * reported once, against the operation it was written for.
     * <p>
     * Without the clearing, a description left over from an assertion step would
     * later surface as the name of an unrelated request.
     */
    public Optional<String> consumeStepDescription() {
        String pending = stepDescription;
        stepDescription = null;
        return Optional.ofNullable(pending);
    }
}
