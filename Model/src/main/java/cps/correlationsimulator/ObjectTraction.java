package cps.correlationsimulator;

import lombok.Getter;

import java.time.Duration;

public class ObjectTraction {

    @Getter
    private double constantSpeedInMetersPerSecond;

    private double distanceInMetersFromObserver;

    public ObjectTraction(double constantSpeedInMetersPerSecond, double initialDistanceInMetersFromObserver) {
        this.constantSpeedInMetersPerSecond = constantSpeedInMetersPerSecond;
        distanceInMetersFromObserver = initialDistanceInMetersFromObserver;
    }

    public double getDistanceSinceStart(Duration elapsedTimeSinceStart) {
        assert !elapsedTimeSinceStart.isNegative();

        return distanceInMetersFromObserver + constantSpeedInMetersPerSecond * elapsedTimeSinceStart.toSeconds();
    }

}
