package utils.data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.github.javafaker.Faker;

import services.rest.books.BookDTO;

/**
 * Builds book payloads for tests.
 * <p>
 * Ids are drawn from a range well above the seeded catalogue, so a generated book can
 * never be confused with one the API ships with. Publish dates are truncated to whole
 * seconds because the API drops sub-second precision when it echoes a payload back,
 * which would otherwise break an equality assertion.
 */
public final class BookFactory {
    /** The first id above the seeded catalogue, reserved for generated data. */
    public static final int GENERATED_ID_FLOOR = 100_000;

    private static final Random RANDOM = DataSeed.newGenerator("books");
    private static final Faker FAKER = new Faker(RANDOM);

    private BookFactory() {
    }

    public static BookDTO aValidBook() {
        return aBookWithId(aGeneratedId());
    }

    public static BookDTO aBookWithId(int id) {
        return BookDTO.builder()
                .id(id)
                .title(FAKER.book().title())
                .description(FAKER.lorem().sentence())
                .pageCount(FAKER.number().numberBetween(1, 1_500))
                .excerpt(FAKER.lorem().paragraph())
                .publishDate(nowInWholeSeconds())
                .build();
    }

    /**
     * A book whose text fields are at the far end of what a caller might send,
     * used to check the API neither truncates nor rejects them silently.
     */
    public static BookDTO aBookWithOversizedText(int characters) {
        return aValidBook().toBuilder()
                .title("T".repeat(characters))
                .description("D".repeat(characters))
                .excerpt("E".repeat(characters))
                .build();
    }

    public static int aGeneratedId() {
        return GENERATED_ID_FLOOR + RANDOM.nextInt(Integer.MAX_VALUE - GENERATED_ID_FLOOR);
    }

    private static OffsetDateTime nowInWholeSeconds() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }
}
