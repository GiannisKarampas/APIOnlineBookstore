package models.errors;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * The RFC 7807 problem document the API returns for rejected requests, e.g. a
 * non-numeric id or a payload whose field types do not match the schema.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemDetailsDTO {

    private String type;
    private String title;
    private Integer status;
    private String traceId;
    private Map<String, List<String>> errors;
}
