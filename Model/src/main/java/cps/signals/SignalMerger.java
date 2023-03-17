package cps.signals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.lang.Math.abs;

public class SignalMerger {

    private static void validate(Signal lhs, Signal rhs)
    {
        assert lhs.getType().equals(rhs.getType());
        assert lhs.getDurationInNs().equals(rhs.getDurationInNs());
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        assert lhs.getSamples().size() == rhs.getSamples().size();
    }

    public static Signal add(Signal lhs, Signal rhs) {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) + rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal subtract(Signal lhs, Signal rhs) {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) - rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal multiply(Signal lhs, Signal rhs) {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) * rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal divide(Signal lhs, Signal rhs) {
        validate(lhs, rhs);
        Signal inversion = inverse(rhs);
        return multiply(lhs, inversion);
    }

    private static Signal inverse(Signal instance)
    {
        final double ZERO = 10e-9;
        List<Double> resultSamples = instance.getSamples().stream().map(x -> abs(x) < ZERO ? 0.0 : 1.0 / x).collect(Collectors.toList());
        return new Signal(instance.getType(), instance.getDurationInNs(), instance.getSamplingPeriod(), resultSamples);

    }

}
