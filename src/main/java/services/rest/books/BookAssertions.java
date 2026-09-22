package services.rest.books;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.asserts.SoftAssert;

import io.qameta.allure.Step;

/**
 * What this suite expects of a book from the seeded catalogue.
 * <p>
 * These are <em>not</em> schema validity. The published contract declares no
 * {@code required} array and marks {@code title} as nullable, so a book with a null
 * title is contract-valid; schema conformance is
 * {@code RestCommonValidations.verifyMatchesContract()}, which checks the document's
 * own rules. What follows is a stricter, separate expectation about the demonstration
 * data the API ships with: two hundred real-looking books, each with an identity, a
 * title and a page count.
 * <p>
 * Three ideas that are easy to run together and are kept apart here:
 * <ul>
 *   <li>the property may be absent from the payload entirely;</li>
 *   <li>the property may be present and explicitly null;</li>
 *   <li>the property's value satisfies a rule about the business, such as an id
 *       being positive.</li>
 * </ul>
 * Only the third is asserted below, and only for the seeded catalogue. The
 * negative-page-count test shows the API itself enforces none of it.
 * <p>
 * Kept apart from {@link BooksService} so that "how a book is fetched" and "what this
 * suite expects of one" can change independently.
 */
public final class BookAssertions {
    private BookAssertions() {
    }

    /**
     * Every field a book in the seeded catalogue is expected to carry.
     * <p>
     * {@code description} and {@code excerpt} are deliberately not checked: they
     * carry no meaning worth asserting, and the contract permits them to be null.
     */
    @Step("Verify the book meets the catalogue's expectations")
    public static void assertMeetsCatalogueExpectations(BookDTO book) {
        assertNotNull(book, "Expected a book but got nothing.");

        SoftAssert softly = new SoftAssert();
        softly.assertNotNull(book.getId(), "A catalogue book is expected to carry an id: " + book);
        softly.assertNotNull(book.getTitle(), "A catalogue book is expected to carry a title, though the contract permits null: " + book);
        softly.assertNotNull(book.getPageCount(), "A book must carry a page count: " + book);
        softly.assertNotNull(book.getPublishDate(), "A book must carry a publish date: " + book);
        if (book.getId() != null) {
            softly.assertTrue(book.getId() > 0, "A book id must be positive but was " + book.getId());
        }
        if (book.getPageCount() != null) {
            softly.assertTrue(book.getPageCount() >= 0, "A page count cannot be negative: " + book.getPageCount());
        }
        softly.assertAll();
    }

    @Step("Verify every book meets the catalogue's expectations")
    public static void assertAllMeetCatalogueExpectations(List<BookDTO> books) {
        assertNotNull(books, "Expected a collection of books but got nothing.");
        assertFalse(books.isEmpty(), "The bookstore returned an empty catalogue.");
        books.forEach(BookAssertions::assertMeetsCatalogueExpectations);
    }

    @Step("Verify the ids are unique")
    public static void assertIdsAreUnique(List<BookDTO> books) {
        Set<Integer> uniqueIds = books.stream().map(BookDTO::getId).collect(Collectors.toSet());
        assertEquals(uniqueIds.size(), books.size(),
                "The catalogue contains duplicate book ids: " + duplicateIdsOf(books));
    }

    /**
     * Asserts the API returned exactly the book it was handed. Used after a write,
     * where the only guarantee this demo API gives is that it echoes the payload back.
     */
    @Step("Verify the API echoed the submitted entity unchanged")
    public static void assertEchoes(BookDTO actual, BookDTO submitted) {
        assertNotNull(actual, "The write returned no book at all.");
        assertEquals(actual, submitted, "The API did not echo the submitted book unchanged.");
    }

    /**
     * Asserts a book with the given id is present in the collection.
     * <p>
     * Compared through {@link Objects#equals}: a book with a missing id is a failure
     * to report, not a {@code NullPointerException} to debug.
     */
    @Step("Verify the collection contains id {expectedId}")
    public static void assertContainsId(List<BookDTO> books, int expectedId) {
        assertTrue(books.stream().anyMatch(book -> Objects.equals(book.getId(), expectedId)),
                "No book with id " + expectedId + " in a catalogue of " + books.size() + " books.");
    }

    /**
     * Names the ids that appear more than once. Ids are mapped to a string first,
     * because a null id would otherwise make the grouping throw while assembling a
     * failure message, hiding the duplicate it was called to report.
     */
    private static String duplicateIdsOf(List<BookDTO> books) {
        return books.stream()
                .collect(Collectors.groupingBy(book -> String.valueOf(book.getId()), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
    }
}
