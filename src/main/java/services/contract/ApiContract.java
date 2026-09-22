package services.contract;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.report.ValidationReport;

/**
 * The API's own published OpenAPI document, used as the reference a response is
 * checked against.
 * <p>
 * The document is a committed snapshot rather than a live fetch, for two reasons: a
 * run must not depend on the provider's docs endpoint being up, and a change to the
 * contract should arrive as a reviewable diff rather than silently altering what the
 * suite asserts. {@code TC_API_CONTRACT_01_OpenApiContract} fails when the snapshot
 * and the published document drift apart, so the snapshot cannot go quietly stale.
 * <p>
 * Parsing the document is expensive and the result is immutable, so one validator is
 * built for the whole run.
 */
public final class ApiContract {

    /** Where the snapshot lives on the classpath. */
    public static final String SPECIFICATION = "contracts/bookstore-openapi.json";

    private static final OpenApiInteractionValidator VALIDATOR =
            OpenApiInteractionValidator.createFor(SPECIFICATION).build();

    private ApiContract() {
    }

    /**
     * Checks a response against the operation the contract declares for this path
     * and method.
     *
     * @param pathTemplate the templated path, e.g. {@code /api/v1/Books/{id}},
     *                     not the path with the id already substituted in
     * @return the report; ask it for {@code hasErrors()} and the messages
     */
    public static ValidationReport validateResponse(String pathTemplate, Request.Method method, Response response) {
        return VALIDATOR.validateResponse(pathTemplate, method, response);
    }

    /**
     * The snapshot as it sits on the classpath, for the drift check to compare
     * against what the API publishes today.
     */
    public static String readSpecification() {
        try (InputStream stream = ApiContract.class.getClassLoader().getResourceAsStream(SPECIFICATION)) {
            if (stream == null) {
                throw new IllegalStateException("The OpenAPI snapshot is missing from the classpath: " + SPECIFICATION);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the OpenAPI snapshot at " + SPECIFICATION, e);
        }
    }
}
