package io.github.qdiaps.ui.controller;

import io.github.qdiaps.domain.command.DataPointAddedEvent;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.DataPoint;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.service.SessionEvent;
import io.github.qdiaps.ui.builder.ChartBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.XYSeries;

@RequiredArgsConstructor
public class ChartController {
    private final ChartBuilder chartBuilder;
    private final EventBus eventBus;

    @FXML
    private StackPane chartContainer;

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
                .withMaxTime(Config.DEFAULT_CHART_MAX_TIME)
                .build();

        chartContainer.getChildren().add(chart);
        chart.prefWidthProperty().bind(chartContainer.widthProperty());
        chart.prefHeightProperty().bind(chartContainer.heightProperty());

        eventBus.subscribe(DataPointAddedEvent.class, event -> onDataPointAdded(event.toDataPoint()));
        eventBus.subscribe(SessionEvent.PointsCleared.class, this::onPointsCleared);
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
}
