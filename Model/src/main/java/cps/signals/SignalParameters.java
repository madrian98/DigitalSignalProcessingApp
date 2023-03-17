package cps.signals;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data @AllArgsConstructor public class SignalParameters {

    private double average;
    private double absoluteAverage;
    private double averagePower;
    private double variance;
    private double effectivePower;

    public SignalParameters() {
    }

    public static SignalParameters measure(Signal signal) {
        SignalParameters measurement = new SignalParameters();
        measurement.average = SignalParamCalculator.meanValue(signal);
        measurement.absoluteAverage = SignalParamCalculator.absoluteMeanValue(signal);
        measurement.averagePower = SignalParamCalculator.averagePower(signal);
        measurement.variance = SignalParamCalculator.variance_values(signal);
        measurement.effectivePower = SignalParamCalculator.RMS_Value(signal);
        return measurement;
    }

    public static SignalParameters measure(Function<Double, Double> function, double startInNs, double endInNs) {
        SignalParameters measurement = new SignalParameters();
        measurement.average = SignalParamCalculator.meanValue(function, startInNs, endInNs);
        measurement.absoluteAverage = SignalParamCalculator.absoluteMeanValue(function, startInNs, endInNs);
        measurement.averagePower = SignalParamCalculator.averagePower(function, startInNs, endInNs);
        measurement.variance = SignalParamCalculator.variance_values(function, startInNs, endInNs);
        measurement.effectivePower = SignalParamCalculator.RMS_Value(function, startInNs, endInNs);
        return measurement;
    }

}
