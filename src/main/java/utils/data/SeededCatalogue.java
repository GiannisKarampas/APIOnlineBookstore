package utils.data;

import java.util.List;

/**
 * The data the Online Bookstore API ships with, and how this suite divides it.
 * <p>
 * Suites run {@code parallel="classes"}, so a class that writes to a record another
 * class is reading would be a race waiting to happen. Every seeded id a test touches
 * is therefore declared here and owned by exactly one kind of test: the read-only
 * ids are never written to, and each mutating class gets ids of its own.
 * <p>
 * This API happens to be stateless, which would hide such a race today. The division
 * is kept anyway, so that pointing the suite at a real bookstore does not turn a
 * green run into an intermittent one.
 */
public final class SeededCatalogue {

    private SeededCatalogue() {
    }

    /**
     * Books, seeded with ids 1 to 200.
     */
    public static final class Books {

        /**
         * Read concurrently by several classes. No test may write to these.
         * <p>
         * A {@code List} rather than an array: a {@code public static final int[]} is
         * only final in its reference, and any caller could quietly reassign an
         * element and change what every other test reads.
         */
        public static final List<Integer> READ_ONLY = List.of(1, 50, 100, 200);

        /** The lowest seeded id, used where a test just needs one that exists. */
        public static final int FIRST = READ_ONLY.get(0);

        /** Owned by the update tests; no read test asserts on it. */
        public static final int OWNED_BY_UPDATE_TESTS = 101;

        /** Owned by the delete tests; no read test asserts on it. */
        public static final int OWNED_BY_DELETE_TESTS = 102;

        private Books() {
        }
    }

    /**
     * Authors. Ids are not contiguous, so only the ones verified to exist are listed.
     */
    public static final class Authors {

        /**
         * Read concurrently by several classes. No test may write to these.
         * <p>
         * The collection endpoint returns a set that is regenerated per call, so its
         * length varies; these ids were each confirmed to resolve individually.
         */
        public static final List<Integer> READ_ONLY = List.of(1, 50, 100, 200);

        /** The lowest seeded id, used where a test just needs one that exists. */
        public static final int FIRST = READ_ONLY.get(0);

        /** Owned by the update tests; no read test asserts on it. */
        public static final int OWNED_BY_UPDATE_TESTS = 500;

        /** Owned by the delete tests; no read test asserts on it. */
        public static final int OWNED_BY_DELETE_TESTS = 501;

        private Authors() {
        }
    }
}
