package base;

import services.contract.ContractService;
import services.rest.authors.AuthorsService;
import services.rest.books.BooksService;
import utils.BaseTest;

/**
 * The base class every Online Bookstore test extends.
 * <p>
 * It adds nothing to the generic {@link BaseTest} except a way of naming a step and
 * reaching the right service in one call, so a test body reads as a list of
 * business actions rather than as framework plumbing.
 */
public abstract class BookstoreTest extends BaseTest {

    /**
     * Opens a reported test step and returns the Books service to act on.
     *
     * @param stepDescription what this step does, as it should read in the report
     */
    protected BooksService books(String stepDescription) {
        return step(stepDescription).rest().service().books();
    }

    /**
     * Opens a reported test step and returns the contract service, for the checks
     * that treat the API's published OpenAPI document as the thing under test.
     *
     * @param stepDescription what this step does, as it should read in the report
     */
    protected ContractService contract(String stepDescription) {
        return step(stepDescription).rest().service().contract();
    }

    /**
     * Opens a reported test step and returns the Authors service to act on.
     *
     * @param stepDescription what this step does, as it should read in the report
     */
    protected AuthorsService authors(String stepDescription) {
        return step(stepDescription).rest().service().authors();
    }
}
