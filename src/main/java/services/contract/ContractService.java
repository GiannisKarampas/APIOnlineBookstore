package services.contract;

import static domain.RestEndpointEnum.OPENAPI_SPECIFICATION;

import io.restassured.response.Response;
import services.ApiService;
import utils.service.implementation.Rest;

/**
 * Reaches the API's own OpenAPI document, so that the contract the suite validates
 * against can itself be checked for drift.
 */
public class ContractService extends ApiService {

    public ContractService(Rest rest) {
        super(rest);
    }

    /**
     * Fetches the OpenAPI document the API publishes right now.
     */
    public Response getPublishedSpecification() {
        return rest.getRequest(OPENAPI_SPECIFICATION, "");
    }
}
