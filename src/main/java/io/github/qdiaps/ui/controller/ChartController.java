package io.github.qdiaps.ui.controller;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
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

//        eventBus.subscribe(SerialEvent.ConnectionEstablished.class, e -> startSimulation());
    }

    public void startSimulation() {
        double[][] hardcodedData = {
                // Время, T_зад, T_тек, PWM(0-255)
                {0,   50.0, 20.0, 255.0}, {2,   50.0, 20.2, 255.0}, {4,   50.0, 20.6, 255.0},
                {6,   50.0, 21.1, 255.0}, {8,   50.0, 21.7, 255.0}, {10,  50.0, 22.5, 255.0},
                {12,  50.0, 23.4, 255.0}, {14,  50.0, 24.5, 255.0}, {16,  50.0, 25.8, 255.0},
                {18,  50.0, 27.2, 255.0}, {20,  50.0, 28.0, 255.0}, {22,  50.0, 29.5, 255.0},
                {24,  50.0, 31.2, 255.0}, {26,  50.0, 33.0, 255.0}, {28,  50.0, 34.8, 255.0},
                {30,  50.0, 36.5, 255.0}, {32,  50.0, 38.4, 240.0}, {34,  50.0, 40.3, 230.0},
                {36,  50.0, 42.2, 215.0}, {38,  50.0, 44.1, 205.0}, {40,  50.0, 46.0, 200.0},
                {42,  50.0, 47.5, 150.0}, {44,  50.0, 49.0, 100.0}, {46,  50.0, 50.4, 50.0},
                {48,  50.0, 51.5, 10.0},  {50,  50.0, 52.5, 0.0},   {52,  50.0, 53.3, 0.0},
                {54,  50.0, 54.0, 0.0},   {56,  50.0, 54.6, 0.0},   {58,  50.0, 55.0, 0.0},
                {60,  50.0, 55.2, 0.0},   {62,  50.0, 55.0, 0.0},   {64,  50.0, 54.7, 0.0},
                {66,  50.0, 54.3, 0.0},   {68,  50.0, 54.0, 0.0},   {70,  50.0, 53.8, 0.0},
                {72,  50.0, 53.0, 20.0},  {74,  50.0, 52.2, 40.0},  {76,  50.0, 51.4, 60.0},
                {78,  50.0, 50.9, 75.0},  {80,  50.0, 50.5, 80.0},  {82,  50.0, 49.9, 100.0},
                {84,  50.0, 49.3, 120.0}, {86,  50.0, 48.8, 140.0}, {88,  50.0, 48.5, 150.0},
                {90,  50.0, 48.2, 160.0}, {92,  50.0, 48.0, 170.0}, {94,  50.0, 47.7, 175.0},
                {96,  50.0, 47.5, 180.0}, {98,  50.0, 47.5, 180.0}, {100, 50.0, 47.5, 180.0},
                {105, 50.0, 48.0, 150.0}, {110, 50.0, 48.8, 120.0}, {115, 50.0, 49.6, 85.0},
                {120, 50.0, 50.6, 50.0},  {125, 50.0, 51.0, 35.0},  {130, 50.0, 51.2, 30.0},
                {135, 50.0, 51.0, 45.0},  {140, 50.0, 50.8, 60.0},  {145, 50.0, 50.5, 75.0},
                {150, 50.0, 50.2, 85.0},  {155, 50.0, 49.9, 92.0},  {160, 50.0, 49.8, 95.0},
                {170, 50.0, 49.9, 92.0},  {180, 50.0, 50.0, 90.0},  {190, 50.0, 50.0, 90.0},
                {200, 50.0, 50.0, 90.0},  {220, 50.0, 50.0, 90.0},  {240, 50.0, 50.0, 90.0},
                {260, 50.0, 50.0, 90.0},  {280, 50.0, 50.0, 90.0},  {300, 50.0, 50.0, 90.0}
        };

        Thread simulationThread = new Thread(() -> {
            for (double[] row : hardcodedData) {
                double time = row[0];
                double tz = row[1];
                double tt = row[2];

                double rawPwm = row[3];
                double pwmPercent = (rawPwm / 255.0) * 100.0;

                Platform.runLater(() -> {
                    seriesTz.add(time, tz);
                    seriesTt.add(time, tt);
                    seriesOut.add(time, pwmPercent);
                });

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        simulationThread.setDaemon(true);
        simulationThread.start();
    }
}
