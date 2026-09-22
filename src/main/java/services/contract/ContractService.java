package services.contract;

import static domain.RestEndpointEnum.OPENAPI_SPECIFICATION;

import java.util.function.Function;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import services.rest.RestCommonValidations;
import utils.service.implementation.Rest;

/**
 * Reaches the API's own OpenAPI document, so that the contract the suite validates
 * against can itself be checked for drift.
 */
public class ContractService {

    private final Rest rest;
    private final RestCommonValidations validations;

    public ContractService(Rest rest) {
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


    /**
     * Fetches the OpenAPI document the API publishes right now.
     */
    public Response getPublishedSpecification() {
        return rest.getRequest(OPENAPI_SPECIFICATION, "");
    }
}
