package cps;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import cps.signals.*;

import static java.lang.Math.min;

public class ImpulseResponse {

    @FXML
    LineChart<Number, Number> impulseResponseChart;

    @FXML
    public void initialize() {
        impulseResponseChart.setAnimated(false);
        impulseResponseChart.setLegendVisible(false);
    }

    public void plot(Signal signal) {
        impulseResponseChart
                .getScene()
                .getStylesheets()
                .clear();


        XYChart.Series series = new XYChart.Series();

        final double NUMBER_OF_PIXELS_IN_CHART = impulseResponseChart.getXAxis().getWidth();

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

        impulseResponseChart.getData().clear();
        impulseResponseChart.getData().add(series);
    }

}
