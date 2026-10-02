package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.DataModel;
import io.github.qdiaps.domain.model.DataPoint;
import io.github.qdiaps.domain.model.ExperimentSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class SessionService implements AutoCloseable {
    private final DataModel dataModel;
    private final ExperimentSession session;
    private final EventBus eventBus;

    public void resetSession() {
        dataModel.clear();
        session.reset();
        eventBus.post(new SessionEvent.PointsCleared());
        log.info("Session points and timer reset");
    }

    public void loadSession(List<DataPoint> loadedPoints, float maxTime) {
        Objects.requireNonNull(loadedPoints, "loadedPoints cannot be null");
        if (maxTime < 0)
            throw new IllegalArgumentException("maxTime must be positive: " + maxTime);

        dataModel.loadPoints(loadedPoints);

        if (dataModel.getMaxTime() != maxTime) {
            dataModel.setMaxTime(maxTime);
            eventBus.post(new SessionEvent.TimeLimitChanged(maxTime));
        }

        eventBus.post(new SessionEvent.PointsLoaded(dataModel.getPoints()));
    }

    public boolean canSetMaxTimeWithoutLoss(float newMaxTime) {
        var lastTime = dataModel.getLastPointTime();
        return newMaxTime >= lastTime;
    }

    public void setMaxTime(float newMaxTime, boolean forceTruncate) {
        if (!canSetMaxTimeWithoutLoss(newMaxTime)) {
            if (!forceTruncate) {
                throw new IllegalStateException(
                        "Cannot reduce maxTime to " + newMaxTime + " without loss. Points exist beyond this time."
                );
            }
            int removedCount = dataModel.truncatePointsAfter(newMaxTime);
            eventBus.post(new SessionEvent.PointsTruncated(newMaxTime, removedCount));
        }

        dataModel.setMaxTime(newMaxTime);
        eventBus.post(new SessionEvent.TimeLimitChanged(newMaxTime));
    }

    public float getMaxTime() {
        return dataModel.getMaxTime();
    }

    public List<DataPoint> getPoints() {
        return dataModel.getPoints();
    }

    public float getLastPointTime() {
        return dataModel.getLastPointTime();
    }

    public int getPointCount() {
        return dataModel.size();
    }

    @Override
    public void close() {
        resetSession();
    }
}
