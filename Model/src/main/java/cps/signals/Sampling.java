package cps.signals;

import lombok.Getter;

import java.time.Duration;
import java.util.List;


public class Sampling extends Signal {

    @Getter
    private List<Duration> sampleTimePoints;

    public Sampling(Signal signal, List<Duration> sampleTimePoints) {
        super(signal.getType(), signal.getDurationInNs(), signal.getSamplingPeriod(), signal.getSamples());
        this.sampleTimePoints = sampleTimePoints;
    }
}
