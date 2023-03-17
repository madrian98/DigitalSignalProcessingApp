package cps.operations;

public class BandPassFilter extends FIRFilter {
    @Override
    protected double modulate(int index)  {
        return 2.0 * Math.sin(Math.PI * index / 2.0);
    }

    @Override
    protected double getK(double sampleFrequency, double frequency) {
        return  ((4 * sampleFrequency) / (sampleFrequency - 4 * frequency));
    }
}
