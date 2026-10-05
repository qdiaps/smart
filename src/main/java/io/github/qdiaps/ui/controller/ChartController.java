package io.github.qdiaps.ui.controller;

import io.github.qdiaps.domain.command.DataPointAddedEvent;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.DataPoint;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.service.SessionEvent;
import io.github.qdiaps.ui.builder.ChartBuilder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.XYSeries;

@RequiredArgsConstructor
public class ChartController {
    private final ChartBuilder chartBuilder;
    private final EventBus eventBus;

    @FXML
    private StackPane chartContainer;

    private final DoubleProperty maxTimeProperty = new SimpleDoubleProperty(Config.DEFAULT_CHART_MAX_TIME);
    private NumberAxis axisX;

    private XYSeries seriesTz;
    private XYSeries seriesTt;
    private XYSeries seriesOut;

    @FXML
    private void initialize() {
        seriesTz = new XYSeries("t_з (°C)");
        seriesTt = new XYSeries("t_п (°C)");
        seriesOut = new XYSeries("Вих");

        ChartViewer chart = chartBuilder
                .withTempSeries(seriesTz, seriesTt)
                .withPwmSeries(seriesOut)
                .withMaxTime(maxTimeProperty)
                .build();

        this.axisX = (NumberAxis) chart.getChart().getXYPlot().getDomainAxis();

        chartContainer.getChildren().add(chart);
        chart.prefWidthProperty().bind(chartContainer.widthProperty());
        chart.prefHeightProperty().bind(chartContainer.heightProperty());

        eventBus.subscribe(DataPointAddedEvent.class, event -> onDataPointAdded(event.toDataPoint()));
        eventBus.subscribe(SessionEvent.PointsCleared.class, this::onPointsCleared);
        eventBus.subscribe(SessionEvent.TimeLimitChanged.class, e -> onTimeLimitChanged(e.newMaxTime()));
        eventBus.subscribe(SessionEvent.PointsTruncated.class, e -> onPointsTruncated(e.timeLimit()));
    }

    private void onDataPointAdded(DataPoint point) {
        Platform.runLater(() -> {
            seriesTz.add(point.time(), point.tempTz());
            seriesTt.add(point.time(), point.tempTt());
            seriesOut.add(point.time(), point.out());
        });
    }

    private void onPointsCleared(SessionEvent.PointsCleared event) {
        Platform.runLater(() -> {
            seriesTz.clear();
            seriesTt.clear();
            seriesOut.clear();
        });
    }

    private void onTimeLimitChanged(float newMaxTime) {
        Platform.runLater(() -> {
            maxTimeProperty.set(newMaxTime);
            if (axisX != null) {
                axisX.setRange(0.0, newMaxTime);
            }
        });
    }

    private void onPointsTruncated(float timeLimit) {
        Platform.runLater(() -> {
            truncateSeries(seriesTz, timeLimit);
            truncateSeries(seriesTt, timeLimit);
            truncateSeries(seriesOut, timeLimit);
        });
    }

    private void truncateSeries(XYSeries series, float timeLimit) {
        for (int i = series.getItemCount() - 1; i >= 0; i--) {
            if (series.getX(i).doubleValue() > timeLimit) {
                series.remove(i);
            } else {
                break;
            }
        }
    }
}
