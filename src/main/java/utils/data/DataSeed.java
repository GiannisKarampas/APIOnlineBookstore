package utils.data;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The seed behind every generated value in a run.
 * <p>
 * Random test data finds things fixed data does not, but only if a failure can be
 * investigated afterwards. The seed is chosen once, reported at the start of the run,
 * and can be forced back in with {@code -Ddata.seed=...}.
 * <p>
 * This <em>aids</em> reproduction rather than guaranteeing it. Classes run in
 * parallel and share a generator per factory, so which test draws which value depends
 * on scheduling; running one failing test on its own changes the sequence again; and
 * publish dates come from the clock, not the seed. Reusing a seed reproduces the pool
 * of values, not necessarily the exact payload a given test saw. Guaranteeing that
 * would mean deriving a generator per scenario and injecting a fixed clock.
 */
public final class DataSeed {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeed.class);
    private static final long SEED = resolve();

    private DataSeed() {
    }

    public static long value() {
        return SEED;
    }

    /**
     * A generator for this run. Each caller gets its own, seeded from the run's seed,
     * so one factory drawing more values than usual cannot shift what another
     * produces.
     */
    public static Random newGenerator(String purpose) {
        return new Random(SEED + purpose.hashCode());
    }

    private static long resolve() {
        String configured = System.getProperty("data.seed");
        long seed = configured == null || configured.isBlank()
                ? ThreadLocalRandom.current().nextLong()
                : Long.parseLong(configured.trim());
        LOGGER.info("Generated test data uses seed {}. Replay this run with -Ddata.seed={}", seed, seed);
        return seed;
    }
}
