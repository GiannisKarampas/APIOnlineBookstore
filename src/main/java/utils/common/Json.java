package utils.common;

import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.SneakyThrows;

/**
 * The single JSON mapper of the framework.
 * <p>
 * The same instance is handed to Rest Assured, so a payload is serialized exactly
 * the way a response is read back and a round-trip comparison stays meaningful.
 */
public final class Json {
    private static final ObjectMapper MAPPER = createMapper();

    private Json() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    @SneakyThrows
    public static String serialize(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public static <T> T deserialize(String json, Class<T> type) {
        return MAPPER.readValue(json, type);
    }

    @SneakyThrows
    public static <T> List<T> deserializeList(String json, Class<T> elementType) {
        return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                // The API keeps adding fields to its schema; unknown ones must not break a test.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
