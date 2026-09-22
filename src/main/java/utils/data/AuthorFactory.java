package utils.data;

import java.util.Random;

import com.github.javafaker.Faker;

import services.rest.authors.AuthorDTO;

/**
 * Builds author payloads for tests, following the same id convention as
 * {@link BookFactory}: generated ids sit above the seeded data.
 */
public final class AuthorFactory {
    public static final int GENERATED_ID_FLOOR = 100_000;

    private static final Random RANDOM = DataSeed.newGenerator("authors");
    private static final Faker FAKER = new Faker(RANDOM);

    private AuthorFactory() {
    }

    public static AuthorDTO aValidAuthor() {
        return anAuthorWithId(aGeneratedId());
    }

    public static AuthorDTO anAuthorWithId(int id) {
        return AuthorDTO.builder()
                .id(id)
                .idBook(FAKER.number().numberBetween(1, 200))
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .build();
    }

    /**
     * An author whose names are at the far end of what a caller might send, used to
     * check the API neither truncates nor rejects them silently.
     */
    public static AuthorDTO anAuthorWithOversizedText(int characters) {
        return aValidAuthor().toBuilder()
                .firstName("F".repeat(characters))
                .lastName("L".repeat(characters))
                .build();
    }

    public static int aGeneratedId() {
        return GENERATED_ID_FLOOR + RANDOM.nextInt(Integer.MAX_VALUE - GENERATED_ID_FLOOR);
    }
}
