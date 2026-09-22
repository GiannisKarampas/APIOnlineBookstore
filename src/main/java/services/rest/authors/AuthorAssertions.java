package services.rest.authors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.asserts.SoftAssert;

import io.qameta.allure.Step;

/**
 * What this suite expects of an author from the seeded data. As with
 * {@link services.rest.books.BookAssertions}, these are expectations about the
 * demonstration data rather than the contract's own rules, which are checked by
 * {@code RestCommonValidations.verifyMatchesContract()}.
 */
public final class AuthorAssertions {
    private AuthorAssertions() {
    }

    @Step("Verify the author meets the seeded expectations")
    public static void assertMeetsSeededExpectations(AuthorDTO author) {
        assertNotNull(author, "Expected an author but got nothing.");

        SoftAssert softly = new SoftAssert();
        softly.assertNotNull(author.getId(), "An author must carry an id: " + author);
        softly.assertNotNull(author.getIdBook(), "An author must be attributed to a book: " + author);
        softly.assertNotNull(author.getFirstName(), "An author must carry a first name: " + author);
        softly.assertNotNull(author.getLastName(), "An author must carry a last name: " + author);
        if (author.getId() != null) {
            softly.assertTrue(author.getId() > 0, "An author id must be positive but was " + author.getId());
        }
        softly.assertAll();
    }

    @Step("Verify every author meets the seeded expectations")
    public static void assertAllMeetSeededExpectations(List<AuthorDTO> authors) {
        assertNotNull(authors, "Expected a collection of authors but got nothing.");
        assertFalse(authors.isEmpty(), "The API returned no authors at all.");
        authors.forEach(AuthorAssertions::assertMeetsSeededExpectations);
    }

    @Step("Verify the ids are unique")
    public static void assertIdsAreUnique(List<AuthorDTO> authors) {
        Set<Integer> uniqueIds = authors.stream().map(AuthorDTO::getId).collect(Collectors.toSet());
        assertEquals(uniqueIds.size(), authors.size(), "The API returned duplicate author ids.");
    }

    @Step("Verify the API echoed the submitted entity unchanged")
    public static void assertEchoes(AuthorDTO actual, AuthorDTO submitted) {
        assertNotNull(actual, "The write returned no author at all.");
        assertEquals(actual, submitted, "The API did not echo the submitted author unchanged.");
    }
}
