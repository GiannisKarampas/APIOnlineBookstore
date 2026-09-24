package utils.service.implementation;


import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.MULTIPART;

import java.io.File;
import java.util.Map;

import domain.interfaces.IEndpoint;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.ContextData;
import utils.common.Json;
import utils.config.EnvDataConfig;
import utils.factories.RestServiceObjectFactory;
import utils.factories.interfaces.IRestServiceFactory;
import utils.service.interfaces.IRestService;

/**
 * The REST transport: it builds requests against the environment's base URI, sends
 * them, and records the response so the following assertion step can read it.
 * <p>
 * It deliberately knows nothing about books or authors. Resource knowledge lives in
 * the services handed out by {@link #service()}.
 */
public class Rest implements IRestService {

    public static final String REST_CONTENT_TYPE = "application/json";

    private final ContextData contextData;
    private final IRestServiceFactory restServiceObjectFactory;
    private final EnvDataConfig envDataConfig = new EnvDataConfig();
    private final String baseUri;
    private final Filter[] extraFilters;
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public Rest() {
        this(new EnvDataConfig().getRestApiUrl());
    }

    /**
     * Points this transport at a specific address instead of the environment's.
     * <p>
     * Exists so a test can drive the real request-building path against a local stub
     * and inspect what actually went out. The alternative — setting a system property
     * — would change the address for every test running in parallel.
     *
     * @param baseUri the address to send to, without a trailing slash
     */
    public Rest(String baseUri) {
        this(baseUri, new Filter[0]);
    }

    /**
     * Adds filters to every request this transport sends.
     * <p>
     * Exists so a test can intercept a request and inspect what would have gone out,
     * without a socket. Capturing at this point is what makes the check meaningful:
     * it sees the specification as it is actually sent, not as it was built.
     *
     * @param baseUri the address to send to, without a trailing slash
     * @param filters filters appended after the framework's own
     */
    public Rest(String baseUri, Filter... filters) {
        this.baseUri = baseUri;
        this.extraFilters = filters.clone();
        this.contextData = new ContextData();
        this.restServiceObjectFactory = new RestServiceObjectFactory(this);
        this.requestSpec = getRequestSpec();
        this.responseSpec = getResponseSpec();
    }

    @Override
    public IRestServiceFactory service() {
        return this.restServiceObjectFactory;
    }

    /**
     * The data carried between the steps of a single test, most importantly the
     * response of the last call.
     */
    @Override
    public ContextData context() {
        return this.contextData;
    }

    // --------------------------------------------------------------------- //
    // Convenience calls used by the resource services
    // --------------------------------------------------------------------- //

    public Response getRequest(IEndpoint basePath, String route) {
        return getRequest(requestSpec, basePath, route, null, null, null, null);
    }

    public Response getRequestWithPathParams(IEndpoint basePath, String route, String pathParameterName, Object pathParameterValue) {
        return getRequest(requestSpec, basePath, route, null, Map.of(pathParameterName, pathParameterValue), null, null);
    }

    public Response postRequest(IEndpoint basePath, String route, Object body) {
        return postRequest(requestSpec, basePath, route, body, null, null, null);
    }

    /**
     * Posts under a content type other than the JSON default, so a test can check
     * how the API rejects a media type it does not accept.
     * <p>
     * Builds a fresh specification rather than overriding the shared one. Rest
     * Assured's {@code contentType} mutates the specification in place and hands the
     * same instance back, so overriding the shared one would silently change the
     * content type of every later request made by the same service.
     */
    public Response postRequest(IEndpoint basePath, String route, Object body, ContentType contentType) {
        return postRequest(getRequestSpec(contentType), basePath, route, body, null, null, null);
    }

    public Response putRequest(IEndpoint basePath, String route, Object body, String pathParameterName, Object pathParameterValue) {
        return putRequest(requestSpec, basePath, route, body, Map.of(pathParameterName, pathParameterValue), null, null);
    }

    /**
     * Puts under a content type other than the JSON default. Builds a fresh
     * specification for the same reason {@link #postRequest} does.
     */
    public Response putRequest(IEndpoint basePath, String route, Object body, ContentType contentType,
                               String pathParameterName, Object pathParameterValue) {
        return putRequest(getRequestSpec(contentType), basePath, route, body,
                Map.of(pathParameterName, pathParameterValue), null, null);
    }

    public Response deleteRequest(IEndpoint basePath, String route, String pathParameterName, Object pathParameterValue) {
        return deleteRequest(requestSpec, basePath, route, null, Map.of(pathParameterName, pathParameterValue), null, null);
    }

    /**
     * Sends a PATCH at a collection endpoint, which takes no path parameter.
     */
    public Response patchRequest(IEndpoint basePath, String route, Object body) {
        return sendRequest(createRequest(requestSpec, basePath, route, body, null, null, null), Method.PATCH, route);
    }

    /**
     * Sends a PATCH. The bookstore API implements no PATCH operation, so this exists
     * for the test that checks an unsupported method is refused rather than ignored.
     */
    public Response patchRequest(IEndpoint basePath, String route, Object body, String pathParameterName, Object pathParameterValue) {
        return sendRequest(createRequest(requestSpec, basePath, route, body,
                Map.of(pathParameterName, pathParameterValue), null, null), Method.PATCH, route);
    }

    // --------------------------------------------------------------------- //
    // Transport
    // --------------------------------------------------------------------- //

    @Override
    public Response getRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                               Map<String, Object> pathParams, Map<String, Object> queryParams, File file) {
        return sendRequest(createRequest(requestSpec, basePath, route, body, pathParams, queryParams, file), Method.GET, route);
    }

    @Override
    public Response postRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                                Map<String, Object> pathParams, Map<String, Object> queryParams, File file) {
        return sendRequest(createRequest(requestSpec, basePath, route, body, pathParams, queryParams, file), Method.POST, route);
    }

    @Override
    public Response putRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                               Map<String, Object> pathParams, Map<String, Object> queryParams, File file) {
        return sendRequest(createRequest(requestSpec, basePath, route, body, pathParams, queryParams, file), Method.PUT, route);
    }

    @Override
    public Response deleteRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                                  Map<String, Object> pathParams, Map<String, Object> queryParams, File file) {
        return sendRequest(createRequest(requestSpec, basePath, route, body, pathParams, queryParams, file), Method.DELETE, route);
    }

    @Override
    public RequestSpecification createRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                                              Map<String, Object> pathParams, Map<String, Object> queryParams, File file) {
        RequestSpecification request = RestAssured.given(requestSpec)
                .basePath(basePath.getPath());
        // Kept templated, so the contract check can name the operation afterwards.
        this.contextData.setLastRequestPath(basePath.getPath() + route);
        if (body != null) {
            request.body(body);
        }
        if (pathParams != null && !pathParams.isEmpty()) {
            request.pathParams(pathParams);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        if (file != null && file.exists()) {
            request.contentType(MULTIPART);
            request.multiPart("file", file);
            if (body != null) {
                request.multiPart("payload", Json.serialize(body), REST_CONTENT_TYPE);
            }
        }
        return request;
    }

    /**
     * Sends the request and stores it, with its response, on the context, so that the
     * next step of the test can assert on it without having to pass it around.
     * <p>
     * The call is wrapped in the reported step rather than merely preceded by one.
     * A step that opens and closes before the request is sent would show a duration
     * of zero and would not contain the request and response attachments, which is
     * exactly the part of the report worth reading when something failed.
     */
    @Override
    public Response sendRequest(RequestSpecification request, Method method, String route) {
        this.contextData.setLastRequestMethod(method);
        String stepName = this.contextData.consumeStepDescription()
                .orElseGet(() -> method + " " + this.contextData.getLastRequestPath());

        Response response = Allure.step(stepName, () -> RestAssured.given(request)
                .when()
                .request(method, route)
                .then()
                .spec(responseSpec)
                .extract()
                .response());

        this.contextData.setLastResponse(response);
        return response;
    }

    // --------------------------------------------------------------------- //
    // Specifications
    // --------------------------------------------------------------------- //

    /**
     * A specification built afresh for this call, with the content type overridden.
     * Safe to mutate precisely because it belongs to no one else.
     */
    public RequestSpecification getRequestSpec(ContentType contentType) {
        return getRequestSpec().contentType(contentType);
    }

    /**
     * A new request specification carrying the environment's base URI, JSON by
     * default, and full request/response logging into the Allure report.
     * <p>
     * Built per call rather than cached, so that a caller overriding something on it
     * cannot affect anybody else's request.
     */
    public RequestSpecification getRequestSpec() {
        // Certificates must validate, in every environment. Nothing here relaxes TLS:
        // a suite that accepts any certificate cannot tell a correctly served API from
        // one behind a broken or substituted one.
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(JSON)
                // Stated rather than left as */*: these tests are about the JSON
                // representation, and the request should say so.
                .setAccept(JSON)
                .setConfig(restAssuredConfig())
                .addFilter(new AllureRestAssured())
                .addFilters(java.util.List.of(extraFilters))
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .log(LogDetail.HEADERS)
                .log(LogDetail.PARAMS)
                .log(LogDetail.BODY)
                .build();
    }

    /**
     * Intentionally empty: status codes are asserted per test, never globally, so a
     * test can expect a 400 without fighting the specification.
     */
    public ResponseSpecification getResponseSpec() {
        return new ResponseSpecBuilder().build();
    }

    /**
     * Binds Rest Assured to the framework's single {@link Json} mapper, so that a DTO
     * is serialized into a request and read back out of a response by the same rules.
     */
    private RestAssuredConfig restAssuredConfig() {
        return RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> Json.mapper()))
                .logConfig(LogConfig.logConfig().enablePrettyPrinting(true))
                // Without these a hung service consumes the whole CI budget and the
                // build times out with no useful message, instead of one test failing
                // quickly and saying which call never came back.
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", envDataConfig.getConnectTimeoutInMillis())
                        .setParam("http.socket.timeout", envDataConfig.getSocketTimeoutInMillis())
                        .setParam("http.connection-manager.timeout", (long) envDataConfig.getConnectTimeoutInMillis()));
    }
}
