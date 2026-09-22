package TS_API_Contract;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static utils.TestGroups.CONTRACT;
import static utils.TestGroups.EDGE_CASE;
import static utils.TestGroups.PROVIDER_BEHAVIOUR;
import static utils.TestGroups.REGRESSION;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;

import base.BookstoreTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import services.contract.ApiContract;
import utils.common.Json;
import utils.data.BookFactory;
import utils.data.SeededCatalogue.Books;

/**
 * The API's published OpenAPI document as the thing under test.
 * <p>
 * The endpoint suites assert that each response matches the contract. This class
 * checks the contract itself: that the committed snapshot still matches what the API
 * publishes, and that the document describes the behaviour the API actually has.
 * <p>
 * Two places where it does not are asserted here by name. They are findings about
 * the provider's documentation, not defects in this suite, and writing them down as
 * tests means that the day the provider corrects the document, these go red and say
 * which assertion to relax.
 */
@Epic("Online Bookstore API")
@Feature("Contract")
@Story("OpenAPI document")
public class TC_API_CONTRACT_01_OpenApiContract extends BookstoreTest {

    /**
     * The parts of the document this suite actually depends on. A change to Users,
     * Activities or CoverPhotos is none of the bookstore's business.
     */
    private static final List<String> RELIED_UPON_PATHS = List.of(
            "/api/v1/Books", "/api/v1/Books/{id}", "/api/v1/Authors", "/api/v1/Authors/{id}");
    private static final List<String> RELIED_UPON_SCHEMAS = List.of("Book", "Author");

    @Severity(SeverityLevel.CRITICAL)
    @Test(groups = {REGRESSION, CONTRACT},
            description = "The committed snapshot still matches the published document where this suite relies on it")
    public void theSnapshotMatchesThePublishedDocument() {
        contract("Fetch the OpenAPI document the API publishes now").getPublishedSpecification();

        String published = contract("Verify the document is served").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .getLastResponse()
                    .asString());

        // Compared as parsed trees, so formatting and key order cannot fail the check.
        JsonNode publishedTree = Json.deserialize(published, JsonNode.class);
        JsonNode snapshotTree = Json.deserialize(ApiContract.readSpecification(), JsonNode.class);

        // Scoped to the operations and schemas this suite asserts against. Comparing
        // the whole document would let an unrelated change to Users or Activities
        // fail a bookstore check, which is noise rather than signal.
        List<String> drifted = new ArrayList<>();
        for (String path : RELIED_UPON_PATHS) {
            if (!nodesMatch(publishedTree.path("paths").path(path), snapshotTree.path("paths").path(path))) {
                drifted.add("paths" + path);
            }
        }
        for (String schema : RELIED_UPON_SCHEMAS) {
            JsonNode publishedSchema = publishedTree.path("components").path("schemas").path(schema);
            JsonNode snapshotSchema = snapshotTree.path("components").path("schemas").path(schema);
            if (!nodesMatch(publishedSchema, snapshotSchema)) {
                drifted.add("schema " + schema);
            }
        }

        if (!drifted.isEmpty()) {
            fail("The published contract has changed where this suite depends on it, so the committed "
                    + "snapshot is stale. Drifted: " + String.join(", ", drifted)
                    + ". Review what moved, refresh src/main/resources/" + ApiContract.SPECIFICATION
                    + ", then re-run. Every assertion about response shape rests on that file.");
        }
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {REGRESSION, CONTRACT},
            description = "A documented operation is checked against its schema, not merely its status code")
    public void aDocumentedOperationIsCheckedAgainstItsSchema() {
        books("Request book " + Books.FIRST).getBookById(Books.FIRST);

        books("Verify the response satisfies the published Book schema").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyMatchesContract());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {REGRESSION, CONTRACT, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Finding: the contract documents no error responses, although the API returns them")
    public void theContractDocumentsNoErrorResponses() {
        books("Request a book that does not exist").getBookById(999_999);
        books("Verify the API answers 404").validate(checks -> checks.verifyStatusCode(SC_NOT_FOUND));

        List<String> violations = books("Measure that response against the contract").validate(checks -> checks
                    .contractViolations());

        assertTrue(violations.stream().anyMatch(violation -> violation.contains("Response status 404 not defined")),
                "The contract was expected to be silent about 404, so that the gap is on record. "
                        + "It now reports: " + violations);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(groups = {REGRESSION, CONTRACT, EDGE_CASE, PROVIDER_BEHAVIOUR},
            description = "Finding: the contract declares no body on a write, although the API returns one")
    public void theContractDeclaresNoBodyOnAWrite() {
        books("Submit a new book").createBook(BookFactory.aValidBook());
        books("Verify the API answers 200 with the book echoed back").validate(checks -> checks
                    .verifyStatusCode(SC_OK)
                    .verifyContentTypeIsJson());

        List<String> violations = books("Measure that response against the contract").validate(checks -> checks
                    .contractViolations());

        assertTrue(violations.stream().anyMatch(violation -> violation.contains("No response body is expected")),
                "POST /api/v1/Books declares no response content in the contract while returning the book it "
                        + "was given, so the gap is on record. The contract now reports: " + violations);
    }

    /**
     * Whether two nodes are identical, treating "absent from both" as a match so a
     * path this suite does not use cannot fail the comparison by being missing.
     */
    private boolean nodesMatch(JsonNode published, JsonNode snapshot) {
        return published.equals(snapshot);
    }
}
