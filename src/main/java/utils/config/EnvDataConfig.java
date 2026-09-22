package utils.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import utils.enums.Environment;

/**
 * Reads the settings of the environment the run was started against.
 * <p>
 * Which file that is comes from the active Maven profile, so switching environment
 * is {@code -Plocal} and never a code change.
 */
public class EnvDataConfig {

    /**
     * Loaded once per run, keyed by file.
     * <p>
     * Every accessor used to reopen the file, and a fresh transport is built per test
     * method, so a suite of ninety tests opened the same handful of bytes ninety
     * times. The contents cannot change mid-run, so reading them once is both faster
     * and more honest about what they are.
     */
    private static final Map<String, Properties> LOADED = new ConcurrentHashMap<>();

    protected final ResourcesConfig resourcesConfig;

    public EnvDataConfig() {
        this.resourcesConfig = new ResourcesConfig();
    }

    /**
     * How many times a failing test is re-run before it is reported as failed.
     * The bookstore API is a shared public sandbox, so an occasional timeout is
     * expected and should not be read as a defect.
     */
    public int getRetry() {
        return Integer.parseInt(getEnvProperties().getProperty("retry"));
    }

    /**
     * The base URI every request is sent to, without a trailing slash.
     * <p>
     * {@code -DbaseUrl=...} wins over the environment file, so a pipeline can point
     * the suite at a freshly deployed instance whose address is not known in advance
     * without adding a profile for it.
     */
    public String getRestApiUrl() {
        String override = System.getProperty("baseUrl");
        String configured = override != null && !override.isBlank()
                ? override
                : getEnvProperties().getProperty("rest.url");
        return removeTrailingSlash(configured);
    }

    /**
     * How long to wait for the connection to be established, in milliseconds.
     */
    public int getConnectTimeoutInMillis() {
        return readInt("http.connect.timeout.millis", 10_000);
    }

    /**
     * How long to wait between packets once connected, in milliseconds. This is the
     * one that catches a service which accepts a connection and then never answers.
     */
    public int getSocketTimeoutInMillis() {
        return readInt("http.socket.timeout.millis", 30_000);
    }

    /**
     * Whether this environment may accept certificates that do not validate. False
     * everywhere it is not explicitly turned on.
     */
    public boolean isRelaxedTlsAllowed() {
        return Boolean.parseBoolean(getEnvProperties().getProperty("tls.relaxed", "false"));
    }

    private int readInt(String key, int fallback) {
        String value = getEnvProperties().getProperty(key);
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
    }

    public Environment getEnvironment() {
        return Environment.fromString(resourcesConfig.getEnvironmentName());
    }

    private Properties getEnvProperties() {
        return LOADED.computeIfAbsent(resourcesConfig.getEnvironmentProperties(), EnvDataConfig::load);
    }

    private static Properties load(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFile);
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read the environment configuration at " + propertiesFile, e);
        }
        return properties;
    }

    private String removeTrailingSlash(String url) {
        if (url == null) {
            throw new IllegalStateException("No 'rest.url' is configured for environment " + getEnvironment()
                    + ", and no -DbaseUrl was supplied.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
