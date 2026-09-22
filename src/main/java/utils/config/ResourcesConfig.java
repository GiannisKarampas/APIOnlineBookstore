package utils.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Locates the configuration file of the environment under test.
 * <p>
 * Surefire passes it as the {@code env.properties} system property, derived from the
 * active Maven profile. When the tests are started straight from an IDE that property
 * is absent, so the default environment is used and the run still works.
 */
public class ResourcesConfig {

    private static final String ENV_PROPERTIES = "env.properties";
    private static final String DEFAULT_ENV_PROPERTIES = "src/main/resources/config/dev.properties";

    public String getEnvironmentProperties() {
        String configured = System.getProperty(ENV_PROPERTIES, DEFAULT_ENV_PROPERTIES);
        return Path.of(getProjectRoot(), configured).toString();
    }

    /**
     * The environment name as it appears in the configuration file name, e.g. {@code uat}.
     */
    public String getEnvironmentName() {
        String fileName = Path.of(getEnvironmentProperties()).getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public String getProjectRoot() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }
}
