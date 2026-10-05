package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.Event;
import io.github.qdiaps.domain.event.LoggableEvent;
import io.github.qdiaps.domain.model.DataPoint;

import java.util.List;

public interface SessionEvent extends Event {
    record PointsCleared(List<DataPoint> points) implements SessionEvent, LoggableEvent {
        @Override
        public String toString() {
            return "PointsCleared[count=" + (points != null ? points.size() : 0) + "]";
        }
    }

    record PointsLoaded(List<DataPoint> points) implements SessionEvent, LoggableEvent {
        @Override
        public String toString() {
            return "PointsLoaded[count=" + (points != null ? points.size() : 0) + "]";
        }
    }

    record PointsTruncated(float timeLimit, int removedCount) implements SessionEvent, LoggableEvent {}

    record TimeLimitChanged(float newMaxTime) implements SessionEvent, LoggableEvent {}

    record Paused() implements SessionEvent, LoggableEvent {}

    record Resumed() implements SessionEvent, LoggableEvent {}

    record Stopped() implements SessionEvent, LoggableEvent {}
}
