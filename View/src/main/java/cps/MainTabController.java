package cps;

import cps.operations.*;
import cps.signals.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static cps.operations.Utility.*;
import static java.lang.Math.min;

public class MainTabController {

    public static final ObservableList<String> AVAILABLE_SIGNAL_OPERATIONS = FXCollections.observableArrayList("+",
            "-",
            "*",
            "/",
            "Splot"
            , "Korelacja",
            "Korelacja przez splot");

    public static final ObservableList<String> FILTER_TYPES = FXCollections.observableArrayList("Dolnoprzepustowy",
            "Środkowoprzepustowy",
            "Górnoprzepustowy");

    public static final ObservableList<String> WINDOW_TYPES = FXCollections.observableArrayList(
            "Prostokątne",
            "Hamming"
    );

    public static final Map<String, String> WINDOW_TYPES_TO_FACTORY_MAP = new HashMap<>();

    private Stage stage;

    private Signal
            signal,
            sampledSignal,
            quantizedSignal,
            interpolatedSignal,
            reconstructedSignal,
            filterHResponse;

    private Histogram histogram;

    @FXML
    private LineChart<Number, Number> chart;
    @FXML
    private ComboBox signalOperationList, filterTypeComboBox, windowTypeComboBox;
    @FXML
    private Label averageValueLabel, averageAbsoluteValueLabel, averagePowerValueLabel, varianceValueLabel, effectivePowerValueLabel;
    @FXML
    private TextField samplingValue,
            bitsValue,
            interpolationFrequencyTextField,
            probesValue,
            sincFq,
            mTextField,
            foTextField;
    @FXML
    private BarChart<Number, Number> histogramChart;
    @FXML
    private Slider histogramBinsSlider;
    @FXML
    private GenerateSignal basicGenerateSignal, extraGenerateSignal;

    private int histogramBins = 10;
    private Signal filteredSignal;

    @FXML
    public void initialize() {
        initializeAllComboBox();

        removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser();

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        histogramChart.setCategoryGap(0);

        histogramChart.setBarGap(0);
        histogramChart.setAnimated(false);
        histogramChart.setLegendVisible(false);

        basicGenerateSignal.onSignalChosen();
        extraGenerateSignal.onSignalChosen();

        // Maps
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(0), WindowType.RECTANGUIAR_WINDOW);
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(1), WindowType.HAMMING_WINDOW);
    }

    private void initializeAllComboBox() {
        initializeComboBox(signalOperationList, AVAILABLE_SIGNAL_OPERATIONS);
        initializeComboBox(filterTypeComboBox, FILTER_TYPES);
        initializeComboBox(windowTypeComboBox, WINDOW_TYPES);
    }

    private void initializeComboBox(ComboBox comboBox, ObservableList<String> content) {
        comboBox.getItems().addAll(content);
        comboBox.setValue(content.get(0));
    }


    @FXML
    public void filter() {
        try {

            final int filterM = getFilterM();
            final double filterFrequency =  getFilterFrequency();
            final ApplyWindow filterApplyWindow = getFilterWindowFunction();
            final Signal signal = getFilteredSignal();
            FIRFilter filter = createFIRFilter();
            var signals = filter.filter(signal, filterM, filterFrequency, filterApplyWindow);
            filterHResponse = signals.get(0);
            filteredSignal = signals.get(1);
            plotSignal(filteredSignal, true);
            drawHistogram(filteredSignal);

        } catch (IllegalArgumentException e )
        {
            onSignalCreationException(e);
        }
    }

    private FIRFilter createFIRFilter() throws IllegalArgumentException {
        String type = (String) filterTypeComboBox.getSelectionModel().getSelectedItem();
        FIRFilter firFilter = null;
        if (type.equals(FILTER_TYPES.get(0))) {
            firFilter = new LowPassFilter();
        } else if (type.equals(FILTER_TYPES.get(1))) {
            firFilter = new BandPassFilter();
        } else if (type.equals(FILTER_TYPES.get(2))) {
            firFilter = new HighPassFilter();
        } else {
            throw new IllegalArgumentException("Zły rodzaj filtra " + type);
        }

        return firFilter;
    }

    private Signal getFilteredSignal() {
        return new Signal(signal);
    }

    private ApplyWindow getFilterWindowFunction() {
        String type = (String) windowTypeComboBox.getSelectionModel().getSelectedItem();
        String correspondingNameInFactory = WINDOW_TYPES_TO_FACTORY_MAP.get(type);
        return WindowType.create(correspondingNameInFactory);
    }

    private double getFilterFrequency() throws IllegalArgumentException {
        var frequency = foTextField.getText();
        return Double.parseDouble(frequency);
    }

    private int getFilterM() throws IllegalArgumentException{
        var m = mTextField.getText();
        return Integer.parseInt(m);
    }

    @FXML
    public void sample() {
        double samplingFrequencyInHz = Double.valueOf(samplingValue.getText());
        Duration samplingPeriodInNs = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));
        Function<Double, Double> function = basicGenerateSignal.creatFunction();
        Duration durationInNs = Duration.ofNanos((long) (basicGenerateSignal.getDurationInSeconds() * 1_000_000_000));
        interpolatedSignal = quantizedSignal = sampledSignal = Signal.createContinousSignal(function, durationInNs, samplingPeriodInNs);
        plotSignal(sampledSignal, true);
        drawHistogram(sampledSignal);
        clearSignalMeasurements();
    }


    @FXML
    public void display() {
        try {
            Function<Double, Double> function = basicGenerateSignal.creatFunction();
            if (function == null) {
                plotSignal(signal, true);
                drawHistogram(signal);
                return;
            }


            SignalArgs args = basicGenerateSignal.getSignalArgs();

            Duration durationInNs = Duration.ofNanos((long) (basicGenerateSignal.getDurationInSeconds() * 1_000_000_000));
            signal = Signal.create(basicGenerateSignal.getSignalType(), function, durationInNs, args.getSamplingFrequency());
            plotSignal(signal, true);
            drawHistogram(signal);
            clearSignalMeasurements();
            SignalParameters signalParameters = measure(signal, function, durationInNs);
            displaySignalMeasurement(signalParameters);

        } catch (IllegalArgumentException exception) {
            onSignalCreationException(exception);
        }
    }

    @FXML
    public void loadDistanceSimulation() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CorrelationDistanceSimulator.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene nscene = new Scene(root);
        Stage tStatge = new Stage();
        tStatge.setTitle("Analiza korelacyjna");
        tStatge.setScene(nscene);
        tStatge.show();
    }

    SignalParameters measure(Signal signal, Function<Double, Double> function, Duration durationInNs) {
        if (signal.getType() == Signal.Type.CONTINUOUS) {
            return SignalParameters.measure(function, 0, durationInNs.toNanos());
        } else {
            return SignalParameters.measure(signal);
        }
    }

    private void plotSignal(Signal signal, boolean clearChart) {
        XYChart.Series series = new XYChart.Series();

        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();

        double singlePointDurationInSeconds = signal.getDurationInNs().toNanos() / 1_000_000_000D;
        if (signal.getSamples().size() != 1) {
            singlePointDurationInSeconds /= min(NUMBER_OF_PIXELS_IN_CHART, signal.getSamples().size() - 1);
        }

        double step = 1.0;
        if (signal.getSamples().size() > NUMBER_OF_PIXELS_IN_CHART)
            step = signal.getSamples().size() / NUMBER_OF_PIXELS_IN_CHART;

        double current = 0.0;
        for (int j = 0; current < signal.getSamples().size(); current += step, j++) {
            double y = signal.getSamples().get((int) current);
            series.getData().add(new XYChart.Data(singlePointDurationInSeconds * j, y));
        }

        if (clearChart) chart.getData().clear();
        chart.getData().add(series);
    }

    @FXML
    private void saveToFile() {
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        File file = fileChooser.showSaveDialog(this.stage);

        if (file == null || signal == null) {
            return;
        }

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();

        Float f = Float.parseFloat(basicGenerateSignal.map(GenerateSignal.Field.T1).getParameterValue().getText());
        ReaderWriter.writeBinary(file, f, (long)toFrequency(signal.getSamplingPeriod()), signal);

    }

    @FXML
    public void loadSignal() {
        try {
            signal = loadFromFile();


            plotSignal(signal, true);
            drawHistogram(signal);
            SignalParameters signalParameters = SignalParameters.measure(signal);
            displaySignalMeasurement(signalParameters);

            basicGenerateSignal.displaySignal(signal, "Sygnal z pliku");
        } catch (IOException e) {
            onSignalCreationException(e);
        }
    }

    private Signal loadFromFile() throws IOException {
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał");

        File file = fileChooser.showOpenDialog(this.stage);

        if (file == null) {
            throw new IOException("Error");
        }

        return ReaderWriter.readBinary(file);


    }

    @FXML
    void add() {
        loadSignalsAndApplyOperator(SignalMerger::add);
    }

    @FXML
    void subtract() {
        loadSignalsAndApplyOperator(SignalMerger::subtract);
    }

    @FXML
    void multiply() {
        loadSignalsAndApplyOperator(SignalMerger::multiply);
    }

    @FXML
    void divide() {
        loadSignalsAndApplyOperator(SignalMerger::divide);
    }

    @FXML
    void convolute() {loadSignalsAndApplyOperator(Filters::convolute);}

    @FXML
    void correlate() {loadSignalsAndApplyOperator(Filters::correlate);}

    private void loadSignalsAndApplyOperator(BiFunction<Signal, Signal, Signal> operator) {
        try {
            Signal lhs = loadFromFile();
            Signal rhs = loadFromFile();
            signal = operator.apply(lhs, rhs);


            plotSignal(signal, true);
            drawHistogram(signal);
            SignalParameters signalParameters = SignalParameters.measure(signal);
            displaySignalMeasurement(signalParameters);

            basicGenerateSignal.displaySignal(signal, "Sygnal z pliku");
        } catch (IOException e) {
            onSignalCreationException(e);
        }
    }

    @FXML
    public void onExecuteButton() {
        String operation = (String) signalOperationList.getSelectionModel().getSelectedItem();
        BiFunction<Signal, Signal, Signal> operator;

        switch (operation) {
            case "+":
                operator = SignalMerger::add;
                break;

            case "-":
                operator = SignalMerger::subtract;
                break;

            case "*":
                operator = SignalMerger::multiply;
                break;

            case "/":
                operator = SignalMerger::divide;
                break;

            case "Splot":
                operator = Filters::convolute;
                break;

            case "Korelacja":
                operator = Filters::correlate;
                break;

            case "Korelacja przez splot":
                operator = Filters::correlateByConvolution;
                break;

            default:
                throw new UnsupportedOperationException("Error");
        }

        try {
            Function lhsFunction = basicGenerateSignal.creatFunction();
            Function rhsFunction = extraGenerateSignal.creatFunction();

            double durationInSeconds = Double.valueOf(basicGenerateSignal.getDurationInSeconds());
            Duration durationInNs = Duration.ofNanos((long) (durationInSeconds * 1_000_000_000L));

            long samplingFrequencyInHz = basicGenerateSignal.getSamplingFrequencyInHz();
            final Duration USER_SAMPLING_RATE = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

            Signal.Type type = basicGenerateSignal.getSignalType();

            Signal lhs = Signal.create(type, lhsFunction, durationInNs, USER_SAMPLING_RATE);
            Signal rhs = Signal.create(type, rhsFunction, durationInNs, USER_SAMPLING_RATE);

            signal = operator.apply(lhs, rhs);



            plotSignal(signal, true);
            drawHistogram(signal);
            SignalParameters signalParameters = SignalParameters.measure(signal);
            displaySignalMeasurement(signalParameters);

        } catch (NumberFormatException exception) {
            onSignalCreationException(exception);
        }
    }


    private void drawHistogram(Signal signal) {
        histogram = new Histogram(signal, histogramBins);

        XYChart.Series series1 = new XYChart.Series();

        double currentRange = histogram.getMin();
        final double columnWidth = (histogram.getMax() - histogram.getMin()) / histogram.getBins();
        for (int i = 0; i < histogram.getBins(); i++) {
            //TODO: ADD HISTOGRAM COLUMN LENGTH TO THE HISTOGRAM CLASS
            String result = String.format("%.2f", currentRange);
            series1.getData().add(new XYChart.Data(result, histogram.getFrequencyList().get(i)));
            currentRange += columnWidth;
        }

        histogramChart.getData().clear();
        histogramChart.getData().add(series1);
    }



    private void onSignalCreationException(Exception e) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Invalid input");
        errorAlert.setContentText(e.getMessage() + "\n" + e.getCause());
        errorAlert.showAndWait();
    }

    private void removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser() {
        extraGenerateSignal.setArrangement(SignalGenerator.UNIFORM_NOISE, GenerateSignal.Field.AMPLITUDE);
        extraGenerateSignal.setArrangement(SignalGenerator.GAUSSIAN_NOISE, GenerateSignal.Field.AMPLITUDE);
        extraGenerateSignal.setArrangement(SignalGenerator.SINUSOID_SIGNAL, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.PERIOD,
                GenerateSignal.Field.T1);
        extraGenerateSignal.setArrangement(SignalGenerator.SINUSOID_ONE_SIDE_SIGNAL, GenerateSignal.Field.AMPLITUDE,
                GenerateSignal.Field.PERIOD, GenerateSignal.Field.T1);
        extraGenerateSignal.setArrangement(SignalGenerator.SINUSOID_BOTH_SIDE_SIGNAL, GenerateSignal.Field.AMPLITUDE,
                GenerateSignal.Field.PERIOD, GenerateSignal.Field.T1);
        extraGenerateSignal.setArrangement(SignalGenerator.UNITARY_SIGNAL, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.T1);
        extraGenerateSignal.setArrangement(SignalGenerator.RECTANGLE_SINGAL, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.PERIOD,
                GenerateSignal.Field.T1, GenerateSignal.Field.KW);
        extraGenerateSignal.setArrangement(SignalGenerator.SYMETRIC_RECTANGLE_SIGNAL, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.PERIOD,
                GenerateSignal.Field.T1, GenerateSignal.Field.KW);
        extraGenerateSignal.setArrangement(SignalGenerator.TRIANGLE_SIGNAL, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.PERIOD,
                GenerateSignal.Field.T1, GenerateSignal.Field.KW);
        extraGenerateSignal.setArrangement(SignalGenerator.UNITARY_IMPULSE, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.NS);
        extraGenerateSignal.setArrangement(SignalGenerator.IMPULSE_NOISE, GenerateSignal.Field.AMPLITUDE, GenerateSignal.Field.PROBABILITY);
    }

    private void displaySignalMeasurement(SignalParameters signalParameters) {
        averageValueLabel.setText(String.format("%.2f", signalParameters.getAverage()));
        averageAbsoluteValueLabel.setText(String.format("%.2f", signalParameters.getAbsoluteAverage()));
        averagePowerValueLabel.setText(String.format("%.2f", signalParameters.getAveragePower()));
        varianceValueLabel.setText(String.format("%.2f", signalParameters.getVariance()));
        effectivePowerValueLabel.setText(String.format("%.2f", signalParameters.getEffectivePower()));
    }



    private void clearSignalMeasurements() {
        averageValueLabel.setText("");
        averageAbsoluteValueLabel.setText("");
        averagePowerValueLabel.setText("");
        varianceValueLabel.setText("");
        effectivePowerValueLabel.setText("");

    }
    @FXML
    public void showH() {
        if (filterHResponse != null) {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ImpulseResponse.fxml"));
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Scene nscene = new Scene(root);
            Stage tStatge = new Stage();
            tStatge.setTitle("Odpowiedź impulsowa filtru");
            tStatge.setScene(nscene);
            tStatge.show();

            var controller = (ImpulseResponse)fxmlLoader.getController();
            controller.plot(filterHResponse);
        }
    }

    @FXML
    public void onHistogramBinsChanged() {
        int newHistogramBins = (int) histogramBinsSlider.getValue();
        if (newHistogramBins != histogramBins) {
            histogramBins = newHistogramBins;
            if (signal != null) {
                drawHistogram(signal);
            }
        }
    }

}
