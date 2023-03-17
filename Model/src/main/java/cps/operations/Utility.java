package cps.operations;

import java.time.Duration;

public class Utility {

    private final static int NANOSECONDS_TO_SECONDS = 1_000_000_000;

    public static double toFrequency(Duration period) {
        var periodInNano = period.getNano();
        return NANOSECONDS_TO_SECONDS * (1.0 / periodInNano);
    }
}
