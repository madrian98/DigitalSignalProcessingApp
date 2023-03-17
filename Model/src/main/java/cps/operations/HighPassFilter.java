package cps.operations;

import cps.operations.FIRFilter;

public class HighPassFilter extends FIRFilter {
    @Override
    protected double modulate(int index)  {
        return Math.pow(-1, index);
    }

    @Override
    protected double getK(double sampleFrequency, double frequency) {
        return  (sampleFrequency / (sampleFrequency / 2.0 - frequency));

    }
}
