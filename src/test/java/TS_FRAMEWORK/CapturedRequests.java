package TS_FRAMEWORK;

import java.util.ArrayList;
import java.util.List;

import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Records what each request would have sent and answers it without a socket.
 * <p>
 * The filter runs at the point the request is dispatched, so what it sees is the
 * specification as actually sent rather than as built a moment earlier — which is the
 * distinction that matters, since the defect this guards against was one call quietly
 * altering the specification the next one used.
 * <p>
 * It never calls {@code ctx.next(...)}, so nothing is transmitted and no port is
 * bound. An earlier version of this test ran a real loopback server, which is fine on
 * a developer's machine and fails on a build agent that forbids binding.
 */
final class CapturedRequests implements Filter {

    private final List<String> contentTypes = new ArrayList<>();

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        synchronized (contentTypes) {
            contentTypes.add(requestSpec.getContentType());
        }
        return new ResponseBuilder()
                .setStatusCode(200)
                .setContentType("application/json")
                .setBody("{}")
                .build();
    }

    /**
     * The Content-Type each request carried, in order.
     */
    List<String> contentTypes() {
        synchronized (contentTypes) {
            return List.copyOf(contentTypes);
        }
    }
}
