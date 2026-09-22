package utils.enums;

import lombok.Getter;

/**
 * The environments the suite can be pointed at. Each one corresponds to a properties
 * file under {@code src/main/resources/config} and a Maven profile of the same name.
 */
@Getter
public enum Environment {
    /** A copy of the API hosted by the developer, on localhost. */
    LOCAL("local"),
    /** The public FakeRestAPI sandbox, which is the only hosted instance there is. */
    DEV("dev");

    private final String name;

    Environment(String name) {
        this.name = name;
    }

    public static Environment fromString(String name) {
        for (Environment env : Environment.values()) {
            if (env.name.equals(name)) {
                return env;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}
