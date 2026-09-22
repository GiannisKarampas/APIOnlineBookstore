package utils.service.interfaces;

import java.io.File;
import java.util.Map;

import domain.interfaces.IEndpoint;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utils.factories.interfaces.IRestServiceFactory;

/**
 * The REST transport of the framework: it knows how to build and send a request,
 * and nothing about the resources it is pointed at.
 */
public interface IRestService extends IBaseService {

    /**
     * The factory that hands out the resource services built on top of this transport.
     */
    IRestServiceFactory service();

    Response getRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                        Map<String, Object> pathParams, Map<String, Object> queryParams, File file);

    Response postRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                         Map<String, Object> pathParams, Map<String, Object> queryParams, File file);

    Response putRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                        Map<String, Object> pathParams, Map<String, Object> queryParams, File file);

    Response deleteRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                           Map<String, Object> pathParams, Map<String, Object> queryParams, File file);

    /**
     * Assembles a request from the pieces a caller supplied, leaving out whatever is absent.
     */
    RequestSpecification createRequest(RequestSpecification requestSpec, IEndpoint basePath, String route, Object body,
                                       Map<String, Object> pathParams, Map<String, Object> queryParams, File file);

    Response sendRequest(RequestSpecification request, Method method, String route);
}
