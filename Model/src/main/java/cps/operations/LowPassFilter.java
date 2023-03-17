package cps.operations;

import cps.operations.FIRFilter;

public class LowPassFilter extends FIRFilter {
    @Override
    protected double modulate(int index) {
        return 1.0;
    }

    @Override
    protected double getK(double sampleFrequency, double frequency)  {
        return  (sampleFrequency / frequency);
    }
}
