package services.rest.authors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An author as exposed by {@code /api/v1/Authors}.
 * <p>
 * {@code idBook} is the book the author is attributed to; the API models the
 * relationship on the author side only.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorDTO {

    private Integer id;
    private Integer idBook;
    private String firstName;
    private String lastName;
}
