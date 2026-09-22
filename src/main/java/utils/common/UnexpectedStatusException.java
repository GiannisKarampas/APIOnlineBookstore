package utils.common;

import lombok.Getter;

/**
 * Raised when a response carries a status the test did not expect.
 * <p>
 * It exists so that the status is available as a number rather than only as prose in
 * a message. Deciding whether a failure is worth retrying by pattern-matching on
 * assertion text is guesswork that breaks the moment a message is reworded; a caller
 * that needs to know what came back can ask.
 *
 * @see TransientFailures
 */
@Getter
public class UnexpectedStatusException extends AssertionError {

    private static final long serialVersionUID = 1L;

    private final transient int expectedStatus;
    private final transient int actualStatus;

    public UnexpectedStatusException(int expectedStatus, int actualStatus, String responseBody) {
        super("Expected status " + expectedStatus + " but the API answered " + actualStatus
                + ". Body was: " + responseBody);
        this.expectedStatus = expectedStatus;
        this.actualStatus = actualStatus;
    }
}
