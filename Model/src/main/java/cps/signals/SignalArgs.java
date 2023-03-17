package cps.signals;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;

@Getter @Setter @Builder public class SignalArgs implements Serializable {

    private Signal.Type type;
    private String signalName;
    private double amplitude;
    private double periodInNs;
    private double initialTimeInNs;
    private double kw;
    private int Ns;
    private Duration duration;
    private Duration samplingFrequency;
    private double probability;

    //parameters
    private double averageValue;
    private double averageAbsoulteValue;
    private double averagePowerValue;
    private double varianceValue;
    private double effectivePowerValue;
}
