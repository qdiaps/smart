package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.model.DataPoint;

public record DataPointAddedEvent(
        float time,
        float tempTz,
        float tempTt,
        float out
) implements DomainEvent {
    public DataPointAddedEvent(DataPoint point) {
        this(point.time(), point.tempTz(), point.tempTt(), point.out());
    }

    public DataPoint toDataPoint() {
        return new DataPoint(time, tempTz, tempTt, out);
    }
}
