package cps.signals;

import java.util.Random;
import java.util.function.Function;

import static java.lang.Math.*;

public class SignalGenerator {

    public static final String UNIFORM_NOISE = "S1";
    public static final String GAUSSIAN_NOISE = "S2";
    public static final String SINUSOID_SIGNAL = "S3";
    public static final String SINUSOID_ONE_SIDE_SIGNAL = "S4";
    public static final String SINUSOID_BOTH_SIDE_SIGNAL = "S5";
    public static final String RECTANGLE_SINGAL = "S6";
    public static final String SYMETRIC_RECTANGLE_SIGNAL = "S7";
    public static final String TRIANGLE_SIGNAL = "S8";
    public static final String UNITARY_SIGNAL = "S9";
    public static final String UNITARY_IMPULSE = "S10";
    public static final String IMPULSE_NOISE = "S11";

    public static Function<Double, Double> createFunction(String function, SignalArgs args) {
        switch (function) {
            case UNIFORM_NOISE:
                return createUniformNoise(args.getAmplitude());

            case GAUSSIAN_NOISE:
                return createGaussianNoise(args.getAmplitude());

            case SINUSOID_SIGNAL:
                return createSinusoidSignal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case SINUSOID_ONE_SIDE_SIGNAL:
                return createSinusoid_One_Side_Signal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case SINUSOID_BOTH_SIDE_SIGNAL:
                return createSinusoid_Both_Sides_Signal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case RECTANGLE_SINGAL:
                return createRectangleSignal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case SYMETRIC_RECTANGLE_SIGNAL:
                return createSymmetricRectangleSignal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case TRIANGLE_SIGNAL:
                return createTriangleSignal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case UNITARY_SIGNAL:
                return createUnitSignal(args.getAmplitude(), args.getInitialTimeInNs());

            case UNITARY_IMPULSE:
                return createUnitaryImpulse(args.getAmplitude(), args.getNs());

            case IMPULSE_NOISE:
                return createImpulseNoise(args.getAmplitude(), args.getProbability());
                
            default:
                throw new IllegalArgumentException(function + " Nieznany sygnał");
        }
    }

    private static Function<Double, Double> createUniformNoise(double amplitude) {
        return x -> {
            Random random = new Random();
            return amplitude * (random.nextDouble() * 2.0 - 1.0);
        };
    }

    private static Function<Double, Double> createGaussianNoise(double amplitude) {
        return x -> {
            Random random = new Random();
            return amplitude * random.nextGaussian();
        };
    }

    private static Function<Double, Double> createSinusoidSignal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            return amplitude * sin(angleVelocity * argument);
        };
    }

    private static Function<Double, Double> createSinusoid_One_Side_Signal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            double left = sin(angleVelocity * argument);
            double right = abs(left);
            return 0.5 * amplitude * (left + right);
        };
    }

    private static Function<Double, Double> createSinusoid_Both_Sides_Signal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            return amplitude * abs(sin(angleVelocity * argument));
        };
    }

    private static Function<Double, Double> createRectangleSignal(double amplitude, double period, double initialTime, double kw) {
        return x -> {

            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : 0;
        };
    }

    private static Function<Double, Double> createSymmetricRectangleSignal(double amplitude, double period, double initialTime,
                                                                           double kw)
    {
        return x -> {
            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : -amplitude;
        };
    }

    private static Function<Double, Double> createTriangleSignal(double amplitude, double period, double initialTime, double kw) {
        return x ->
        {
            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            if (integer >= kMin && integer < kMax) {
                return (amplitude / (kw * period)) * (x - integer * period - initialTime);
            } else {
                integer = floor(kMax - kw);
                return (-amplitude / (period * (1.0 - kw))) * (x - integer * period - initialTime) + (amplitude / (1.0 - kw));
            }
        };
    }

    private static Function<Double, Double> createUnitSignal(double amplitude, double initialTime) {
        return x -> {
            if (x > initialTime) {
                return amplitude;
            } else if (x < initialTime) {
                return 0.0;
            } else {
                return 0.5 * amplitude;
            }
        };
    }

    private static Function<Double, Double> createUnitaryImpulse(double amplitude, int Ns) {
        return n -> n - Ns == 0 ? amplitude : 0;
    }

    private static Function<Double, Double> createImpulseNoise(double amplitude, double probability) {
        return n -> {
            Random random = new Random();
            double threshold = random.nextDouble();
            if (threshold >= 0 && threshold <= probability) {
                return amplitude;
            } else {
                return 0.0;
            }
        };
    }
    private SignalGenerator() {
    }


}
