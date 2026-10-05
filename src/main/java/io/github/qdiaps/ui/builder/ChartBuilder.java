package io.github.qdiaps.ui.builder;

import io.github.qdiaps.infrastructure.config.Config;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.PanHandlerFX;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ChartBuilder {
    private final JFreeChart chart;
    private final XYPlot plot;
    private final NumberAxis axisX;
    private final NumberAxis axisY1;

    private ReadOnlyDoubleProperty maxTimeProperty;

    public ChartBuilder(String title) {
        this.chart = ChartFactory.createXYLineChart(
                title,
                "Час (сек)",
                "Температура (°C)",
                null
        );

        this.plot = chart.getXYPlot();
        this.axisX = (NumberAxis) plot.getDomainAxis();
        this.axisY1 = (NumberAxis) plot.getRangeAxis();

        applyNordTheme();
    }

    public ChartBuilder withTempSeries(XYSeries seriesTz, XYSeries seriesTt) {
        var datasetTemp = new XYSeriesCollection();
        datasetTemp.addSeries(seriesTz);
        datasetTemp.addSeries(seriesTt);
        plot.setDataset(0, datasetTemp);

        var renderTemp = new XYLineAndShapeRenderer(true, false);

        renderTemp.setSeriesPaint(0, Color.decode("#5e81ac"));
        renderTemp.setSeriesStroke(0, new BasicStroke(
                1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f
        ));

        renderTemp.setSeriesPaint(1, Color.decode("#bf616a"));
        renderTemp.setSeriesStroke(1, new BasicStroke(2.5f));

        plot.setRenderer(0, renderTemp);

        return this;
    }

    public ChartBuilder withPwmSeries(XYSeries seriesOut) {
        var datasetPwm = new XYSeriesCollection();
        datasetPwm.addSeries(seriesOut);
        plot.setDataset(1, datasetPwm);

        var axisY2 = new NumberAxis("Вихідний сигнал");

        var boldAxisFont = new Font("SansSerif", java.awt.Font.BOLD, 12);
        axisY2.setLabelFont(boldAxisFont);
        axisY2.setTickLabelPaint(Color.decode("#2e3440"));
        axisY2.setLabelPaint(Color.decode("#2e3440"));

        plot.setRangeAxis(1, axisY2);
        plot.mapDatasetToRangeAxis(1, 1);

        var renderPwm = new XYLineAndShapeRenderer(true, false);
        renderPwm.setSeriesPaint(0, Color.decode("#a3be8c"));
        renderPwm.setSeriesStroke(0, new BasicStroke(1.5f));

        plot.setRenderer(1, renderPwm);

        return this;
    }

    public ChartBuilder withMaxTime(ReadOnlyDoubleProperty maxTimeProperty) {
        this.maxTimeProperty = maxTimeProperty;
        return this;
    }

    public ChartViewer build() {
        axisX.setRange(0.0, getMaxTime());

        var viewer = new ChartViewer(chart);
        ChartCanvas canvas = viewer.getCanvas();

        setupInteractivity(viewer, canvas);

        return viewer;
    }

    private double getMaxTime() {
        return maxTimeProperty != null ? maxTimeProperty.get() : Config.DEFAULT_CHART_MAX_TIME;
    }

    private void applyNordTheme() {
        var nordBgBase = Color.decode("#fafafc");
        var nordGrid = Color.decode("#d8dee9");
        var nordText = Color.decode("#2e3440");

        chart.setBackgroundPaint(nordBgBase);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(nordGrid);
        plot.setRangeGridlinePaint(nordGrid);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(nordBgBase);
            chart.getLegend().setFrame(BlockBorder.NONE);
        }

        var boldAxisFont = new Font("SansSerif", java.awt.Font.BOLD, 12);
        axisY1.setLabelFont(boldAxisFont);
        axisX.setLabelFont(boldAxisFont);

        chart.getTitle().setPaint(nordText);
        axisX.setTickLabelPaint(nordText);
        axisX.setLabelPaint(nordText);
        axisY1.setTickLabelPaint(nordText);
        axisY1.setLabelPaint(nordText);

        plot.setDomainPannable(true);
        plot.setRangePannable(false);

        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairPaint(Color.decode("#d08770"));
    }

    private void setupInteractivity(ChartViewer viewer, ChartCanvas canvas) {
        removeDefaultHandler(canvas, "zoom");
        removeDefaultHandler(canvas, "scroll");
        removeDefaultHandler(canvas, "pan");

        canvas.addMouseHandler(new PanHandlerFX("pan", false, false, false, false));

        final Tooltip coordTooltip = new Tooltip();
        coordTooltip.setAutoHide(true);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            Rectangle2D dataArea = viewer.getRenderingInfo().getPlotInfo().getDataArea();

            if (dataArea != null && dataArea.contains(event.getX(), event.getY())) {
                double xValue = axisX.java2DToValue(event.getX(), dataArea, plot.getDomainAxisEdge());
                plot.setDomainCrosshairValue(xValue);

                double tzVal  = findNearestY(plot.getDataset(0), 0, xValue);
                double ttVal  = findNearestY(plot.getDataset(0), 1, xValue);
                double outVal = findNearestY(plot.getDataset(1), 0, xValue);

                String tooltipText = String.format(
                        "Час: %.1f с\nt_з: %.1f °C\nt_п: %.1f °C\nВих: %.1f",
                        xValue, tzVal, ttVal, outVal
                );

                coordTooltip.setText(tooltipText);

                if (!coordTooltip.isShowing()) {
                    coordTooltip.show(canvas, event.getScreenX() + 15, event.getScreenY() + 15);
                } else {
                    coordTooltip.setX(event.getScreenX() + 15);
                    coordTooltip.setY(event.getScreenY() + 15);
                }
            } else {
                if (coordTooltip.isShowing()) coordTooltip.hide();
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (coordTooltip.isShowing()) coordTooltip.hide();
        });

        canvas.setOnScroll(event -> {
            double delta = event.getDeltaY() != 0 ? event.getDeltaY() : event.getDeltaX();
            if (delta == 0) return;

            double factor = (delta > 0) ? 0.90 : 1.10;
            plot.zoomDomainAxes(factor, viewer.getRenderingInfo().getPlotInfo(), new Point2D.Double(event.getX(), event.getY()));
            event.consume();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                axisX.setRange(0.0, getMaxTime());
            }
        });

        axisX.addChangeListener(event -> {
            double currentMax = getMaxTime();
            double lower = axisX.getLowerBound();
            double upper = axisX.getUpperBound();
            double length = upper - lower;

            if (length > currentMax) {
                axisX.setRange(0.0, currentMax);
                return;
            }
            if (lower < 0.0) {
                axisX.setRange(0.0, length);
            } else if (upper > currentMax) {
                axisX.setRange(currentMax - length, currentMax);
            }
        });
    }

    private void removeDefaultHandler(ChartCanvas canvas, String handlerId) {
        var handler = canvas.getMouseHandler(handlerId);
        if (handler != null) canvas.removeMouseHandler(handler);
    }

    private double findNearestY(XYDataset dataset, int seriesIndex, double targetX) {
        if (dataset == null || dataset.getItemCount(seriesIndex) == 0) return 0.0;

        int index = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i < dataset.getItemCount(seriesIndex); i++) {
            double x = dataset.getXValue(seriesIndex, i);
            double diff = Math.abs(x - targetX);
            if (diff < minDiff) {
                minDiff = diff;
                index = i;
            }
        }
        return dataset.getYValue(seriesIndex, index);
    }
}
