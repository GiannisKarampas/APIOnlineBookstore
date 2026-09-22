package utils.factories;

import services.contract.ContractService;
import services.rest.authors.AuthorsService;
import services.rest.books.BooksService;
import utils.factories.interfaces.IRestServiceFactory;
import utils.service.implementation.Rest;

/**
 * Builds the resource services once per transport and hands the same instances out,
 * so every step of a test talks to the service holding that test's context.
 */
public class RestServiceObjectFactory implements IRestServiceFactory {

    private final BooksService booksService;
    private final AuthorsService authorsService;
    private final ContractService contractService;

    public RestServiceObjectFactory(Rest rest) {
        this.booksService = new BooksService(rest);
        this.authorsService = new AuthorsService(rest);
        this.contractService = new ContractService(rest);
    }

    @Override
    public BooksService books() {
        return booksService;
    }

    @Override
    public ContractService contract() {
        return contractService;
    }

    @Override
    public AuthorsService authors() {
        return authorsService;
    }
}
