package services.rest;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.testng.asserts.SoftAssert;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.RestAssuredResponse;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import models.errors.ProblemDetailsDTO;
import services.contract.ApiContract;
import utils.common.Json;
import utils.common.UnexpectedStatusException;
import utils.service.implementation.Rest;

/**
 * Assertions that apply to any REST response, regardless of the resource behind it.
 * <p>
 * Each method is a reported step in its own right, so a failed assertion shows as a
 * failed step rather than as a passing one beside a failed test.
 * <p>
 * Resource-specific expectations do not belong here: they live next to their DTO, in
 * {@code BookAssertions} and {@code AuthorAssertions}.
 */
public class RestCommonValidations {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RestCommonValidations.class);

    private final Rest rest;

    public RestCommonValidations(Rest rest) {
        this.rest = rest;
    }

    /**
     * The response produced by the most recent call on this service.
     */
    public Response getLastResponse() {
        Response lastResponse = rest.context().getLastResponse();
        assertNotNull(lastResponse, "No request has been sent yet, so there is nothing to validate.");
        return lastResponse;
    }

    @Step("Verify the status code is {expectedStatusCode}")
    public RestCommonValidations verifyStatusCode(int expectedStatusCode) {
        int actual = getLastResponse().statusCode();
        if (actual != expectedStatusCode) {
            // Carries the status as a number, so the retry policy can tell a gateway
            // hiccup from a genuinely wrong answer without reading the message.
            throw new UnexpectedStatusException(expectedStatusCode, actual, getLastResponse().asString());
        }
        return this;
    }

    /**
     * Compares only the media type, so the {@code charset} and API-version suffixes
     * the service appends do not make the assertion brittle.
     */
    @Step("Verify the content type is {expectedContentType}")
    public RestCommonValidations verifyContentType(ContentType expectedContentType) {
        String actual = getLastResponse().getContentType();
        assertNotNull(actual, "The response carries no Content-Type header.");
        assertTrue(ContentType.fromContentType(actual) == expectedContentType,
                "Expected a " + expectedContentType + " response but got: " + actual);
        return this;
    }

    public RestCommonValidations verifyContentTypeIsJson() {
        return verifyContentType(ContentType.JSON);
    }

    @Step("Verify the response arrived within {thresholdInMillis}ms")
    public RestCommonValidations verifyResponseTimeIsBelow(long thresholdInMillis) {
        long actual = getLastResponse().timeIn(TimeUnit.MILLISECONDS);
        assertTrue(actual < thresholdInMillis,
                "The endpoint answered in " + actual + "ms, above the agreed " + thresholdInMillis + "ms budget.");
        return this;
    }

    @Step("Verify the response carries no body")
    public RestCommonValidations verifyBodyIsEmpty() {
        String body = getLastResponse().asString();
        assertTrue(body == null || body.isBlank(), "Expected an empty body but got: " + body);
        return this;
    }

    /**
     * Asserts the response is a well-formed RFC 7807 problem document reporting the
     * given status, and hands it back so a test can assert on the offending fields.
     */
    @Step("Verify the request is rejected with a {expectedStatusCode} problem document")
    public ProblemDetailsDTO verifyProblemDetails(int expectedStatusCode) {
        verifyStatusCode(expectedStatusCode);
        verifyContentType(ContentType.JSON);
        ProblemDetailsDTO problem = as(ProblemDetailsDTO.class);

        SoftAssert softly = new SoftAssert();
        softly.assertNotNull(problem.getTitle(), "A problem document must carry a title.");
        softly.assertNotNull(problem.getType(), "A problem document must carry a type.");
        softly.assertEquals(problem.getStatus(), Integer.valueOf(expectedStatusCode),
                "The status inside the problem document must match the HTTP status.");
        // Asserted here so that a test reading problem.getErrors() downstream fails
        // with this message rather than with a NullPointerException.
        softly.assertNotNull(problem.getErrors(),
                "A validation problem document must name the fields it rejected, but 'errors' was absent.");
        softly.assertAll();

        LOGGER.info("Rejected as expected: {}", problem.getErrors());
        return problem;
    }

    /**
     * Asserts the response matches what the API's published OpenAPI document says
     * this operation returns: the status has to be one the contract declares, and
     * the body has to satisfy the declared schema.
     * <p>
     * This is the check hand-written assertions cannot replace. They verify the
     * fields somebody thought to name; the contract covers every field, its type and
     * its nullability, and rejects undocumented ones. A page count that quietly
     * becomes a quoted string still deserializes into an {@code Integer}, so only
     * this assertion notices.
     */
    @Step("Verify the response matches the published contract")
    public RestCommonValidations verifyMatchesContract() {
        List<String> violations = contractViolations();
        assertTrue(violations.isEmpty(), rest.context().getLastRequestMethod() + " "
                + rest.context().getLastRequestPath() + " does not match the published contract ("
                + ApiContract.SPECIFICATION + "):\n  - " + String.join("\n  - ", violations));
        return this;
    }

    /**
     * The ways the last response departs from the published contract, as readable
     * messages, asserting nothing.
     * <p>
     * This is how a test states a gap it already knows about. This API's document,
     * for instance, declares no error responses and no body on a write, while the
     * API returns both; those gaps are asserted by name in the contract suite rather
     * than left as failures nobody can act on.
     */
    public List<String> contractViolations() {
        String path = rest.context().getLastRequestPath();
        Method method = rest.context().getLastRequestMethod();
        assertNotNull(path, "No request has been sent yet, so there is no operation to check.");

        ValidationReport report = ApiContract.validateResponse(
                path, Request.Method.valueOf(method.name()), RestAssuredResponse.of(getLastResponse()));

        return report.getMessages().stream().map(ValidationReport.Message::getMessage).toList();
    }

    public <T> T as(Class<T> type) {
        return Json.deserialize(getLastResponse().asString(), type);
    }

    public <T> List<T> asListOf(Class<T> elementType) {
        return Json.deserializeList(getLastResponse().asString(), elementType);
    }
}
