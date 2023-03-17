package cps.operations;

import cps.signals.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static cps.operations.Utility.toFrequency;
import static java.lang.Math.sin;

public abstract class FIRFilter {


    protected abstract double modulate(int index);

    protected abstract double getK(final double sampleFrequency, final double frequency);

    public List<Signal> filter(final Signal signal, final int M, final double frequency, final ApplyWindow applyWindow) {
        Signal filterImpulseResponse = FilterImpulseResponse(signal, M, frequency, applyWindow);
        Signal filteredSignal = filter(signal, filterImpulseResponse, M);
        ArrayList<Signal> signals = new ArrayList<>();
        signals.add(filterImpulseResponse);
        signals.add(filteredSignal);
        return signals;
    }

    private Signal FilterImpulseResponse(final Signal signal, final int M, final double frequency, final ApplyWindow applyWindow)
    {
        double signalSamplingFrequency = toFrequency(signal.getSamplingPeriod());
        var K = getK(signalSamplingFrequency, frequency);

        List<Double> newValues = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            double newSample;

            if (i == (M - 1) / 2) {
                newSample = 2.0 / K;
            } else {
                newSample = sin((2.0 * Math.PI * (i - (M - 1) / 2)) / K) / (Math.PI * (i - (M - 1) / 2));
            }
            newSample *= applyWindow.apply(i, M);
            var multiplicant = modulate(i - (M - 1) / 2);
             newSample *= multiplicant;
            newValues.add(newSample);
        }

        return new Signal(Signal.Type.DISCRETE, signal.getSamplingPeriod().multipliedBy(M), signal.getSamplingPeriod(),  newValues);
    }

    private Signal filter(final Signal signal, final Signal filterImpulseResponse, final int M) {
        Signal convolution = Filters.convolute(filterImpulseResponse, signal);

        List<Double> newConvolutionValues = new ArrayList<>();

        var size = convolution.getSamples().size() - M + 1;

        for (int i = 0; i < size; i++) {
            newConvolutionValues.add(convolution.getSamples().get(i + (M - 1) / 2));
        }
        var duration  = convolution.getDurationInNs().minus(convolution.getSamplingPeriod().multipliedBy(M - 1));
        return new Signal(signal.getType(), duration, signal.getSamplingPeriod(), newConvolutionValues);
    }


}
