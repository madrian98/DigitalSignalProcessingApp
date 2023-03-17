package cps.correlationsimulator;

import lombok.Setter;

import java.time.Duration;
import java.util.TimerTask;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Transmitter extends TimerTask {

    private Function<Duration, Double> function;
    private Duration transmmisionPeriod;

    @Setter
    private BiFunction<Duration, Double, Void> callback;

    private Duration time = Duration.ZERO;

    public Transmitter(Function<Duration, Double> function, BiFunction<Duration, Double, Void> callback, Duration transmmisionPeriod) {
        this.function = function;
        this.callback = callback;
        this.transmmisionPeriod = transmmisionPeriod;
    }

    @Override
    public void run() {
        var emittedValue = function.apply(time);
        callback.apply(time, emittedValue);
        time = time.plus(transmmisionPeriod);
    }
}
