package cps;

import cps.signals.SignalGenerator;
import cps.signals.Signal;
import cps.signals.SignalArgs;
import cps.operations.Utility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class GenerateSignal extends VBox {

    private static final ObservableList<String> AVAILABLE_SIGNALS = FXCollections.observableArrayList("(1)Szum o rozkładzie jednostajnym",
            "(2)Szum gaussowski", "(3)Sygnał sinusoidalny", "(4)Sygnał sinusoidalny wyprostowany jednopołówkowo",
            "(5)Sygnał sinusoidalny wyprsotowany dwupołówkowo", "(6)Sygnał prostokątny", "(7)Sygnał prostokątny symetryczny", "(8)Sygnał trójkątny",
            "(9)Skok jednostkowy", "(10)Impuls jednostkowy", "(11)Szum impulsowy");

    private static final Map<String, String> LABEL_TO_SIGNAL_MAP = new HashMap<>();

    static {
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(0), SignalGenerator.UNIFORM_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(1), SignalGenerator.GAUSSIAN_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(2), SignalGenerator.SINUSOID_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(3), SignalGenerator.SINUSOID_ONE_SIDE_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(4), SignalGenerator.SINUSOID_BOTH_SIDE_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(5), SignalGenerator.RECTANGLE_SINGAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(6), SignalGenerator.SYMETRIC_RECTANGLE_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(7), SignalGenerator.TRIANGLE_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(8), SignalGenerator.UNITARY_SIGNAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(9), SignalGenerator.UNITARY_IMPULSE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(10), SignalGenerator.IMPULSE_NOISE);
    }

    private final Map<String, Runnable> signalNameToSignalParametersLayoutMap = new HashMap<>();

    @FXML private ComboBox<String> signalList;

    @FXML
    private SignalParameter amplitudeSignalParameter, periodSignalParameter, t1SignalParameter, durationSignalParameter, kwSignalParameter, nsSignalParameter, samplingFrequencySignalParameter, probabilitySignalParameter;

    public GenerateSignal() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/GenerateSignal.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        signalList.getItems().addAll(AVAILABLE_SIGNALS);

        initializeSignalParameters();
        initializeLayout();

        signalList.setValue(AVAILABLE_SIGNALS.get(2));

    }

    @FXML public void onSignalChosen() {
        String selection = signalList.getSelectionModel().getSelectedItem();
        String selectedSignal = LABEL_TO_SIGNAL_MAP.get(selection);
        Runnable layoutRearrangement = signalNameToSignalParametersLayoutMap.get(selectedSignal);
        if (layoutRearrangement != null) {
            layoutRearrangement.run();
        }
    }



    private void initializeSignalParameters() {
        amplitudeSignalParameter.getParameterName().setText("A: ");
        amplitudeSignalParameter.getParameterValue().setText("1.0");
        periodSignalParameter.getParameterName().setText("T: ");
        periodSignalParameter.getParameterValue().setText("0.05");
        t1SignalParameter.getParameterName().setText("t1: ");
        t1SignalParameter.getParameterValue().setText("0.1");
        durationSignalParameter.getParameterName().setText("d: ");
        durationSignalParameter.getParameterValue().setText("1");
        kwSignalParameter.getParameterName().setText("kw: ");
        kwSignalParameter.getParameterValue().setText("0.5");
        nsSignalParameter.getParameterName().setText("ns");
        nsSignalParameter.getParameterValue().setText("5");
        samplingFrequencySignalParameter.getParameterName().setText("fs");
        samplingFrequencySignalParameter.getParameterValue().setText("1000");
        probabilitySignalParameter.getParameterName().setText("p");
        probabilitySignalParameter.getParameterValue().setText("0.5");
    }

    private void initializeLayout() {
        Runnable layoutRearrangement0 = () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement1 = () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(periodSignalParameter);
            getChildren().add(t1SignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement2 = () -> {
            layoutRearrangement1.run();
            getChildren().add(kwSignalParameter);
        };

        Runnable layoutRearrangement3 = () -> {
            layoutRearrangement0.run();
            getChildren().add(nsSignalParameter);
        };

        Runnable layoutRearrangement4 = () -> {
            layoutRearrangement0.run();
            getChildren().add(probabilitySignalParameter);
        };

        signalNameToSignalParametersLayoutMap.put(SignalGenerator.UNIFORM_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.GAUSSIAN_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.SINUSOID_SIGNAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.SINUSOID_ONE_SIDE_SIGNAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.SINUSOID_BOTH_SIDE_SIGNAL, layoutRearrangement1);

        signalNameToSignalParametersLayoutMap.put(SignalGenerator.UNITARY_SIGNAL, () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(t1SignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);

        });

        signalNameToSignalParametersLayoutMap.put(SignalGenerator.RECTANGLE_SINGAL, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.SYMETRIC_RECTANGLE_SIGNAL, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalGenerator.TRIANGLE_SIGNAL, layoutRearrangement2);

        signalNameToSignalParametersLayoutMap.put(SignalGenerator.UNITARY_IMPULSE, layoutRearrangement3);

        signalNameToSignalParametersLayoutMap.put(SignalGenerator.IMPULSE_NOISE, layoutRearrangement4);
    }

    private void removeAllSignalParameters() {
        getChildren().removeAll(amplitudeSignalParameter, periodSignalParameter, t1SignalParameter, durationSignalParameter,
                kwSignalParameter, nsSignalParameter, samplingFrequencySignalParameter, probabilitySignalParameter);
    }

    public SignalParameter map(Field field) {
        switch (field) {
            case AMPLITUDE:
                return amplitudeSignalParameter;
            case PERIOD:
                return periodSignalParameter;
            case T1:
                return t1SignalParameter;
            case DURATION:
                return durationSignalParameter;
            case KW:
                return kwSignalParameter;
            case NS:
                return nsSignalParameter;
            case SAMPLING_FREQUENCY:
                return samplingFrequencySignalParameter;
            case PROBABILITY:
                return probabilitySignalParameter;

            default:
                throw new IllegalArgumentException("Sprawdź pole" + field);
        }
    }

    public enum Field {
        AMPLITUDE, PERIOD, T1, DURATION, KW, NS, SAMPLING_FREQUENCY, PROBABILITY
    }
    public Function<Double, Double> creatFunction() throws IllegalArgumentException {
        try {
            SignalArgs args = getSignalArgs();
            String selection = signalList.getSelectionModel().getSelectedItem();
            String function = LABEL_TO_SIGNAL_MAP.get(selection);

            if (function == null) {
                return null;
            }

            return SignalGenerator.createFunction(function, args);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public SignalArgs getSignalArgs() throws IllegalArgumentException {
        String name = signalList.getSelectionModel().getSelectedItem();

        double amplitude = Double.parseDouble(amplitudeSignalParameter.getParameterValue().getText());

        double periodInSeconds = Double.valueOf(periodSignalParameter.getParameterValue().getText());
        Duration periodInNs = Duration.ofNanos((long) (periodInSeconds * 1_000_000_000L));

        double initialTimeInSeconds = Double.valueOf(t1SignalParameter.getParameterValue().getText());
        Duration initialTimeInNs = Duration.ofNanos((long) (initialTimeInSeconds * 1_000_000_000L));

        double samplingFrequencyInHz = Double.valueOf(samplingFrequencySignalParameter.getParameterValue().getText());
        Duration samplingFrequencyInNs = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

        int ns = Integer.parseInt(nsSignalParameter.getParameterValue().getText());

        double probability = Double.parseDouble(probabilitySignalParameter.getParameterValue().getText());

        double kw = Double.parseDouble(kwSignalParameter.getParameterValue().getText());

        return SignalArgs.builder()
                .signalName(name)
                .amplitude(amplitude)
                .periodInNs(periodInNs.toNanos())
                .initialTimeInNs(initialTimeInNs.toNanos())
                .kw(kw)
                .Ns(ns)
                .probability(probability)
                .samplingFrequency(samplingFrequencyInNs)
                .build();
    }

    public double getDurationInSeconds() throws IllegalArgumentException {
        try {
            return Double.valueOf(durationSignalParameter.getParameterValue().getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public long getSamplingFrequencyInHz() throws IllegalArgumentException {
        try {
            return Long.parseLong(samplingFrequencySignalParameter.getParameterValue().getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Signal.Type getSignalType() {
        String name = signalList.getSelectionModel().getSelectedItem();
        name = LABEL_TO_SIGNAL_MAP.get(name);

        if (name.equals(SignalGenerator.UNITARY_IMPULSE) || name.equals(SignalGenerator.IMPULSE_NOISE)) {
            return Signal.Type.DISCRETE;
        } else {
            return Signal.Type.CONTINUOUS;
        }
    }


    public void setArrangement(String signal, Field... field) {
        if (signalNameToSignalParametersLayoutMap.containsKey(signal)) {
            signalNameToSignalParametersLayoutMap.remove(signal);
            Runnable layoutRearrangement = () -> {
                removeAllSignalParameters();
                Arrays.stream(field).forEach(x -> getChildren().add(map(x)));
            };
            signalNameToSignalParametersLayoutMap.put(signal, layoutRearrangement);
        }
    }

    public void displaySignal(Signal signal, String title) {
        removeAllSignalParameters();
        getChildren().add(durationSignalParameter);
        getChildren().add(samplingFrequencySignalParameter);

        double durationInSeconds = (signal.getDurationInNs().toNanos() / 1_000_000_000.0);
        long samplingFrequency = (long) Utility.toFrequency(signal.getSamplingPeriod());
        durationSignalParameter.getParameterValue().setText(String.format(Locale.US, "%.2f", durationInSeconds));
        samplingFrequencySignalParameter.getParameterValue().setText(String.format(Locale.US, "%d", samplingFrequency));

        signalList.setValue(title);
    }
}
