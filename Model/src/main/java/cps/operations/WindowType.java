package cps.operations;

import cps.operations.ApplyWindow;

import static java.lang.Math.cos;

public class WindowType {

    public static final String RECTANGUIAR_WINDOW = "RECTANGULAR_WINDOW";
    public static final String HAMMING_WINDOW = "HAMMING_WINDOW";

    private static ApplyWindow createRectangularWindow() {
        return (n, M) -> 1.0;
    }

    private static ApplyWindow createHammingWindow()
    {
        return (n, M) -> 0.53836 - 0.46164 * cos( (2 * Math.PI * n) / M);
    }

    public static ApplyWindow create(String windowFunction) {
        switch (windowFunction)
        {
            case RECTANGUIAR_WINDOW:
                return createRectangularWindow();

            case HAMMING_WINDOW:
                return createHammingWindow();
            default:
                throw new IllegalArgumentException("Funkcja okna " +
                        windowFunction + " nie istnieje");
        }
    }

}
