package io.github.qdiaps.domain.model;

import io.github.qdiaps.infrastructure.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataModel {
    private final List<DataPoint> points = new ArrayList<>();
    private float maxTime;

    public DataModel() {
        setMaxTime((float) Config.DEFAULT_CHART_MAX_TIME);
    }

    public DataModel(float maxTime) {
        setMaxTime(maxTime);
    }

    public synchronized boolean canAddPoint(float time) {
        return time >= 0 && time <= maxTime;
    }

    public synchronized void addPoint(DataPoint point) {
        Objects.requireNonNull(point, "DataPoint cannot be null");
        if (!canAddPoint(point.time())) {
            return;
        }
        points.add(point);
    }

    public synchronized List<DataPoint> getPoints() {
        return List.copyOf(points);
    }

    public synchronized void clear() {
        points.clear();
    }

    public synchronized void loadPoints(List<DataPoint> loadedPoints) {
        Objects.requireNonNull(loadedPoints, "loadedPoints cannot be null");
        points.clear();
        points.addAll(loadedPoints);
    }

    public synchronized float getLastPointTime() {
        if (points.isEmpty()) {
            return 0.0f;
        }

        return points.getLast().time();
    }

    public synchronized int truncatePointsAfter(float timeLimit) {
        int initialSize = points.size();
        points.removeIf(p -> p.time() > timeLimit);
        return initialSize - points.size();
    }

    public synchronized float getMaxTime() {
        return maxTime;
    }

    public synchronized void setMaxTime(float maxTime) {
        if (maxTime <= 0) {
            throw new IllegalArgumentException("maxTime must be positive: " + maxTime);
        }
        this.maxTime = maxTime;
    }

    public synchronized int size() {
        return points.size();
    }

    public synchronized boolean isEmpty() {
        return points.isEmpty();
    }
}
