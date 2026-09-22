package utils.service.implementation;

import models.ContextData;
import utils.service.interfaces.IBaseService;

/**
 * The entry point a test reaches through {@code step(...)}: it owns the REST
 * transport and exposes the context that transport records into.
 */
public class WebService implements IBaseService {

    private final Rest rest;

    public WebService() {
        this.rest = new Rest();
    }

    /**
     * The one context for this test.
     * <p>
     * Deliberately delegates to the transport rather than holding a second instance:
     * a description written here has to be the one the transport reads when it names
     * the request, and the response recorded there has to be the one the assertions
     * read back.
     */
    @Override
    public ContextData context() {
        return rest.context();
    }

    /**
     * Returns the REST service associated with this web service.
     *
     * @return the REST service
     */
    public Rest rest() {
        return this.rest;
    }
}
