package cps;

import cps.operations.Filters;
import cps.signals.Signal;

import cps.signals.SignalGenerator;
import cps.signals.SignalArgs;
import cps.correlationsimulator.ObjectTraction;
import cps.correlationsimulator.Transmitter;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.IntStream;


public class CorrelationDistanceSimulator {

    public static final double INITIAL_DISTANCE_IN_METERS = 1.0;

    private List<Double> samples = new ArrayList();

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart, correlationChart;

    @FXML
    private TextField timeUnitTextField,
            probingSignalFrequencyTextField,
            bufferSizeTextField,
            objectSpeedInMetersPerSecond,
            realDistanceInMetersTextField,

            reportPeriodTextField,
            samplingPeriodTextField,
            signalSpeedInMetersPerSecondTextField;


    @FXML
    public void initialize() {
        transmittedSignalChart.setAnimated(false);
        transmittedSignalChart.setLegendVisible(false);
        transmittedSignalChart.getYAxis().setTickLabelsVisible(false);
        transmittedSignalChart.getYAxis().setOpacity(0);
        transmittedSignalChart.getXAxis().setTickLabelsVisible(false);
        transmittedSignalChart.getXAxis().setOpacity(0);

        receivedSignalChart.setAnimated(false);
        receivedSignalChart.setLegendVisible(false);
        receivedSignalChart.getYAxis().setTickLabelsVisible(false);
        receivedSignalChart.getYAxis().setOpacity(0);
        receivedSignalChart.getXAxis().setTickLabelsVisible(false);
        receivedSignalChart.getXAxis().setOpacity(0);

        correlationChart.setAnimated(false);
        correlationChart.setLegendVisible(false);
        correlationChart.getYAxis().setTickLabelsVisible(false);
        correlationChart.getYAxis().setOpacity(0);
        correlationChart.getXAxis().setTickLabelsVisible(false);
        correlationChart.getXAxis().setOpacity(0);
    }


    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> seriesConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> receivedSignaSeriesQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> correlationChartSeriesQueue = new ConcurrentLinkedQueue<>();

    private XYChart.Series<Number, Number> bufferedSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> bufferReceivedSignalSeries = new XYChart.Series<>();

    private volatile SimpleDoubleProperty realDistanceToTrackedObjectInMeters = new SimpleDoubleProperty(1.0);

    private ObjectTraction objectTraction;

    private Timer timer = new Timer();
    private Transmitter transmitter;

    private AnimationTimer animationTimer;

    private Duration timeUnit;
    private Duration reportPeriod;
    private Duration previousUpdateTime;
    private Duration samplingPeriod;

    double signalPropagationSpeedInMeters = 0.0;

    private XYChart.Series<Number, Number> shiftSeries(double value) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();


        if (bufferedSeries.getData().isEmpty()) {
            return series;
        }

        final int size = bufferedSeries.getData().size();
        IntStream.range(1, size).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(),
                        bufferedSeries.getData().get(x).getYValue())));


        series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(size - 1).toMillis(),
                        value));

        return series;
    }

    private Duration getTimeUnit() {
        int MILLIS_TO_SECONDS = 1000;
        var text = timeUnitTextField.getText();
        return Duration.ofMillis((long) (Double.valueOf(text) * MILLIS_TO_SECONDS));
    }

    private void startTransmittingSignal(Function<Duration, Double> function) {
        realDistanceToTrackedObjectInMeters.set(INITIAL_DISTANCE_IN_METERS);
        transmittedSignalChart.getData().clear();
        receivedSignalChart.getData().clear();
        correlationChart.getData().clear();
        seriesConcurrentLinkedQueue.clear();
        receivedSignaSeriesQueue.clear();
        correlationChartSeriesQueue.clear();

        timeUnit = getTimeUnit();
        samplingPeriod = getSamplingPeriod();
        signalPropagationSpeedInMeters = getSignalPropagationSpeedInMetersPerSecond();


        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        IntStream.range(0, getBufferSize()).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        transmittedSignalChart.getData().add(series);

        bufferedSeries = series;

        XYChart.Series<Number, Number> receivedSeries = new XYChart.Series<>();
        IntStream.range(0, getBufferSize()).forEach(x -> receivedSeries.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        receivedSignalChart.getData().add(receivedSeries);

        bufferReceivedSignalSeries = receivedSeries;

        XYChart.Series<Number, Number> correlationSeries = new XYChart.Series<>();
        IntStream.range(0, getBufferSize()).forEach(x -> correlationSeries.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        correlationChart.getData().add(correlationSeries);

        realDistanceInMetersTextField.textProperty().bind(realDistanceToTrackedObjectInMeters.asString());

        objectTraction = new ObjectTraction(getObjectSpeedInMetersPerSecond(),
                getRealDistanceToTrackedObjectInMeters());


        transmitter = new Transmitter(function, this::updateChart, samplingPeriod);
        timer = new Timer();
        timer.scheduleAtFixedRate(transmitter, 0, timeUnit.toMillis());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!seriesConcurrentLinkedQueue.isEmpty()) {
                    var series = seriesConcurrentLinkedQueue.remove();
                    transmittedSignalChart.getData().set(0, series);
                }

                if (!receivedSignaSeriesQueue.isEmpty()) {
                    var series = receivedSignaSeriesQueue.remove();
                    receivedSignalChart.getData().set(0, series);
                }

                if (!correlationChartSeriesQueue.isEmpty()) {
                    var series = correlationChartSeriesQueue.remove();
                    correlationChart.getData().set(0, series);
                }
            }
        };
        animationTimer.start();
    }

    private Void updateChart(Duration duration, Double value) {
        var newSeries = shiftSeries(value);
        seriesConcurrentLinkedQueue.add(newSeries);
        bufferedSeries = newSeries;

        double val = objectTraction.getDistanceSinceStart(duration);

        Platform.runLater(() -> realDistanceToTrackedObjectInMeters.set(val));



        samples.add(value);

        listener(duration);

        tryCalculateDistanceByCorrelation(duration);

        return null;
    }

    private void tryCalculateDistanceByCorrelation(Duration duration) {
        if (isDistanceUpdateTime()) {
            List<Double> transmitedValues = new ArrayList<>();
            List<Double> receivedValues = new ArrayList<>();

            transmittedSignalChart.getData().get(0).getData().forEach(x -> transmitedValues.add(x.getYValue().doubleValue()));
            receivedSignalChart.getData().get(0).getData().forEach(
                    x -> receivedValues.add(x.getYValue().doubleValue())
            );


            Signal lhs = new Signal(Signal.Type.DISCRETE,
                    null,
                    samplingPeriod,
                    transmitedValues);

            Signal rhs = new Signal(Signal.Type.DISCRETE,
                    null,
                    samplingPeriod,
                    receivedValues
            );

            var correlation = Filters.correlate(lhs, rhs);
            var series = new XYChart.Series<Number, Number>();
            IntStream.range(0, correlation.getSamples().size()).forEach(x -> series.getData().
                    add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), correlation.getSamples().get(x))));
            correlationChartSeriesQueue.add(series);

        }
    }


    private Duration getSamplingPeriod() {
        double frequency = Double.valueOf(samplingPeriodTextField.getText());
        var period = 1.0 / frequency;
        return Duration.ofMillis((long) (period * 1000));
    }

    private boolean isDistanceUpdateTime() {
        Duration now = Duration.ofMillis(System.currentTimeMillis());

        if (now.minus(previousUpdateTime).compareTo(reportPeriod) > 0) {
            previousUpdateTime = now;
            return true;
        }

        return false;
    }


    @FXML
    public void start() {

        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(getProbingSignalPeriodInNs()).initialTimeInNs(0).build();
        var sineFunction = SignalGenerator.createFunction(SignalGenerator.SINUSOID_SIGNAL, args);

        var secondSignalArgs = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(getProbingSignalPeriodInNs() / 10).initialTimeInNs(0).build();
        var secondSineFunction = SignalGenerator.createFunction(SignalGenerator.SINUSOID_SIGNAL, secondSignalArgs);


        Function<Duration, Double> wrapper = (Duration x) -> {
            double timeInNanos = (double) x.toNanos();
            return sineFunction.apply(timeInNanos) * secondSineFunction.apply(timeInNanos);
        };

        reportPeriod = getReportPeriodInTextField();
        previousUpdateTime = Duration.ofMillis(System.currentTimeMillis());

        startTransmittingSignal(wrapper);
    }
    @FXML
    public void stop() {
        timer.cancel();
        animationTimer.stop();
    }



    private double getProbingSignalPeriodInNs() {
        double frequency = Double.valueOf(probingSignalFrequencyTextField.getText());
        var period = 1.0 / frequency;
        return period * 1_000_000_000;
    }

    private int getBufferSize() {
        return Integer.valueOf(bufferSizeTextField.getText());
    }

    private double getObjectSpeedInMetersPerSecond() {
        return Double.valueOf(objectSpeedInMetersPerSecond.getText());
    }

    private double getSignalPropagationSpeedInMetersPerSecond() {
        return Double.valueOf(signalSpeedInMetersPerSecondTextField.getText());
    }

    private double getRealDistanceToTrackedObjectInMeters() {
        return realDistanceToTrackedObjectInMeters.get();
    }

    private void listener(Duration duration) {

        int lastReceivedSampleIndex = (int) (
                (duration.toMillis() / 1000.0 - 2 * objectTraction.getDistanceSinceStart(duration) / signalPropagationSpeedInMeters) /
                        (samplingPeriod.toMillis() / 1000.0));


        if (lastReceivedSampleIndex >= 0 && lastReceivedSampleIndex < samples.size()) {
            XYChart.Series<Number, Number> receivedSeries = new XYChart.Series<>();

            if (bufferReceivedSignalSeries.getData().isEmpty()) {
                return;
            }

            IntStream.range(1, getBufferSize()).forEach(x -> receivedSeries.getData().add(
                    new XYChart.Data<>(x * timeUnit.toMillis(), bufferReceivedSignalSeries.getData().get(x).getYValue())
            ));

            receivedSeries.getData().add(new XYChart.Data((getBufferSize() - 1) * timeUnit.toMillis(), samples.get(lastReceivedSampleIndex)));

            bufferReceivedSignalSeries = receivedSeries;

            receivedSignaSeriesQueue.add(receivedSeries);
        }
    }

    Duration getReportPeriodInTextField() {
        int MILLIS_TO_SECONDS = 1000;
        var text = reportPeriodTextField.getText();
        return Duration.ofMillis((long) (Double.valueOf(text) * MILLIS_TO_SECONDS));
    }
}
