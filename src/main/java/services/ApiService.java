package services;

import java.util.function.Function;

import io.qameta.allure.Allure;
import services.rest.RestCommonValidations;
import utils.service.implementation.Rest;

/**
 * What every service in this suite shares: the transport it sends through, and the
 * way a test asserts on whatever came back.
 * <p>
 * Only the genuinely common part lives here. Knowledge of a resource — its paths, the
 * shape of its payloads, the deliberately malformed input its edge cases need — stays
 * in the service that owns it, so adding a resource still means adding a class rather
 * than editing this one.
 */
public abstract class ApiService {

    /**
     * The transport this service sends through. Exposed to subclasses because
     * building a request is exactly what they are for.
     */
    protected final Rest rest;

    private final RestCommonValidations validations;

    protected ApiService(Rest rest) {
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
     *
     * @param checks what to assert, given the validations for the last response
     * @param <T>    whatever the caller wants back, typically a DTO or nothing
     */
    public <T> T validate(Function<RestCommonValidations, T> checks) {
        String stepName = rest.context().consumeStepDescription().orElse("Verify the response");
        return Allure.step(stepName, () -> checks.apply(validations));
    }
}
