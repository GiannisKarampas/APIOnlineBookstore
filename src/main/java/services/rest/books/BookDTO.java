package services.rest.books;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A book as exposed by {@code /api/v1/Books}.
 * <p>
 * Every field is boxed on purpose: a missing field has to stay {@code null} after
 * deserialization so that tests can tell "absent" apart from "zero".
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {

    private Integer id;
    private String title;
    private String description;
    private Integer pageCount;
    private String excerpt;
    private OffsetDateTime publishDate;
}
