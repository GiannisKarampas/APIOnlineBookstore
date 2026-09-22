package domain;


import domain.interfaces.IEndpoint;
import lombok.Getter;

/**
 * Every REST resource of the Online Bookstore API, declared once so that no test
 * hard-codes a URL. Paths containing {@code {id}} are resolved through path parameters.
 */
public enum RestEndpointEnum implements IEndpoint {
    API("/api/v1"),
    BOOKS(API.getPath() + "/Books"),
    BOOK_BY_ID(BOOKS.getPath() + "/{id}"),
    AUTHORS(API.getPath() + "/Authors"),
    AUTHOR_BY_ID(AUTHORS.getPath() + "/{id}"),
    /** The API's own OpenAPI document. Outside /api/v1, hence the absolute path. */
    OPENAPI_SPECIFICATION("/swagger/v1/swagger.json");

    @Getter
    private final String path;

    RestEndpointEnum(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

}
