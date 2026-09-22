package utils;

/**
 * The TestNG groups the suites select on. Declared as constants so a typo in a
 * group name fails at compile time instead of silently running nothing.
 */
public final class TestGroups {

    /** A short, business-critical subset, meant to gate a deployment. */
    public static final String SMOKE = "smoke";
    /** The full set of checks, run nightly and on every pull request. */
    public static final String REGRESSION = "regression";
    /** The documented, well-behaved usage of an endpoint. */
    public static final String HAPPY_PATH = "happy-path";
    /** Malformed input, unknown ids, boundary values and other abuse. */
    public static final String EDGE_CASE = "edge-case";
    /** Checks the API against its own published OpenAPI document. */
    public static final String CONTRACT = "contract";
    /**
     * Pins behaviour the provider currently has, rather than behaviour it owes us:
     * the writes that do not persist, the contract that omits its error responses,
     * the values accepted without validation.
     * <p>
     * A failure here means the provider changed, not that this suite regressed. Kept
     * separable so that a pipeline can report the two differently, and so nobody
     * debugging a red build mistakes one for the other.
     */
    public static final String PROVIDER_BEHAVIOUR = "provider-behaviour";
    /**
     * Tests of the framework itself rather than of the API: request isolation, the
     * retry policy, the execution context, the summary report. They make no network
     * calls, and they exist because every defect they cover survived a full green
     * run of the API suite.
     */
    public static final String FRAMEWORK = "framework";
    public static final String BOOKS = "books";
    public static final String AUTHORS = "authors";

    private TestGroups() {
    }
}
