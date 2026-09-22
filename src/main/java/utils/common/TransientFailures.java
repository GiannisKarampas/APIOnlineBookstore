package utils.common;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import org.apache.http.ConnectionClosedException;
import org.apache.http.NoHttpResponseException;

/**
 * Decides whether a failure is worth trying again.
 * <p>
 * The rule is narrow on purpose: a failure is transient only when the evidence says
 * the request never got a considered answer, or got one the service itself labels as
 * "ask again later". Everything else — a wrong status, a payload that will not parse,
 * a certificate that does not validate, a fixture that is not on disk — is
 * deterministic and is reported the first time it happens.
 * <p>
 * Casting the net wider is tempting and wrong. Retrying a deterministic defect
 * triples the feedback loop, triples the load on the service, and converts a
 * reproducible bug into an intermittent one that nobody trusts enough to act on.
 */
public final class TransientFailures {

    /**
     * Statuses that mean "ask again later" rather than "your request was wrong".
     * <p>
     * 500 is deliberately absent. A generic server error is far more often a real
     * defect than a passing squall, and retrying it hides exactly the thing worth
     * reporting. The gateway family and 429 carry an explicit promise of transience.
     */
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 502, 503, 504);

    /**
     * Failures at the transport level, where no considered answer was received.
     * <p>
     * Listed individually rather than as {@link java.io.IOException}, which would
     * also catch a missing fixture ({@code FileNotFoundException}), a body that will
     * not parse (Jackson's exceptions) and a certificate that does not validate
     * ({@code SSLException}) — all deterministic, none worth a second attempt. A
     * certificate failure in particular should be loud: it usually means the suite is
     * pointed somewhere unintended.
     */
    private static final List<Class<? extends Throwable>> TRANSPORT_FAILURES = List.of(
            SocketException.class,
            SocketTimeoutException.class,
            UnknownHostException.class,
            NoHttpResponseException.class,
            ConnectionClosedException.class);

    private TransientFailures() {
    }

    /**
     * Whether this failure, or anything that caused it, looks transient.
     */
    public static boolean isTransient(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof UnexpectedStatusException status) {
                return RETRYABLE_STATUSES.contains(status.getActualStatus());
            }
            for (Class<? extends Throwable> transportFailure : TRANSPORT_FAILURES) {
                if (transportFailure.isInstance(cause)) {
                    return true;
                }
            }
        }
        return false;
    }
}
