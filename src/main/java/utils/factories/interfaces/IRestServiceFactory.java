package utils.factories.interfaces;

import services.contract.ContractService;
import services.rest.authors.AuthorsService;
import services.rest.books.BooksService;

/**
 * Hands out the service objects a test talks to. Adding a resource to the API means
 * adding one method here and one service class, leaving the transport untouched.
 */
public interface IRestServiceFactory {

    BooksService books();

    ContractService contract();

    AuthorsService authors();
}
