package cps.signals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

import static java.lang.Math.*;

public class SignalParamCalculator {

    private final static int INTEGRATION_STEPS = 100;

    public static double meanValue(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, function);
        return integral / duration;
    }

    public static double meanValue(Signal signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).sum() / (double) signal.getSamples().size();
    }

    public static double absoluteMeanValue(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> abs(function.apply(x)));
        return integral / duration;
    }

    public static double absoluteMeanValue(Signal signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(java.lang.Math::abs).sum() /
                (double) signal.getSamples().size();
    }

    public static double averagePower(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> pow(function.apply(x), 2));
        return integral / duration;
    }

    public static double averagePower(Signal signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(i -> pow(i, 2)).sum() /
                (double) signal.getSamples().size();
    }

    public static double variance_values(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double average = meanValue(function, startInNs, endInNs);
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> pow(function.apply(x) - average, 2));
        return integral / duration;
    }

    public static double variance_values(Signal signal) {
        double average = meanValue(signal);
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(i -> pow(i - average, 2)).sum() /
                (double) signal.getSamples().size();
    }

    public static double RMS_Value(Function<Double, Double> function, double startInNs, double endInNs) {
        assert startInNs != endInNs;
        return sqrt(averagePower(function, startInNs, endInNs));
    }

    public static double RMS_Value(Signal signal) {
        return sqrt(averagePower(signal));
    }


    public static Double round_values(Double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private static double integrate(double a, double b, int N, Function<Double, Double> function) {
        double h = (b - a) / N;
        double sum = 0.5 * (function.apply(a) + function.apply(b));
        for (int i = 1; i < N; i++) {
            double x = a + h * i;
            sum += function.apply(x);
        }
        return sum * h;
    }

}
