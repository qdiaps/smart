package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.model.DataModel;
import io.github.qdiaps.domain.model.DataPoint;
import io.github.qdiaps.domain.model.ExperimentSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DataCommand implements Command {
    private final DataModel data;
    private final ExperimentSession session;

    private boolean timeLimitReached = false;

    @Override
    public Optional<DomainEvent> execute(String[] payload) {
        if (payload == null || payload.length != 3) {
            log.warn("Invalid data payload: {}", Arrays.toString(payload));
            return getIncorrectFormatEvent(payload);
        } else if (session.isPaused()) {
            log.debug("Data point dropped: experiment is paused");
            return Optional.of(new DataPointDroppedEvent(
                    "Experiment is paused", "data:" + Arrays.toString(payload)
            ));
        }

        float time = (float) session.getElapsedSeconds();
        if (!data.canAddPoint(time)) {
            session.setElapsedTime(data.getMaxTime());
            if (!timeLimitReached) {
                timeLimitReached = true;
                log.info("Time limit reached ({} s), incoming points will be dropped", data.getMaxTime());
                return Optional.of(new TimeLimitReachedEvent(data.getMaxTime()));
            }
            log.debug("Data point dropped: time limit reached");
            return Optional.of(new DataPointDroppedEvent(
                    "Time limit reached", "data:" + Arrays.toString(payload)
            ));
        }

        timeLimitReached = false;

        float tempTz, tempTt, out;
        try {
            tempTz = Float.parseFloat(payload[0]);
            tempTt = Float.parseFloat(payload[1]);
            out = Float.parseFloat(payload[2]);
        } catch (NullPointerException | NumberFormatException e) {
            log.warn("Failed to parse data point numbers: {}", Arrays.toString(payload));
            return getIncorrectFormatEvent(payload);
        }

        if (!hasValidRanges(tempTz, tempTt, out)) {
            log.warn("Data point values out of acceptable range (0-100): {}", Arrays.toString(payload));
            return Optional.of(new DataPointDroppedEvent(
                    "Values are out of acceptable range (0-100)", "data:" + Arrays.toString(payload)
            ));
        }

        var point = new DataPoint(time, tempTz, tempTt, out);
        data.addPoint(point);

        return Optional.of(new DataPointAddedEvent(point));
    }

    private Optional<DomainEvent> getIncorrectFormatEvent(String[] payload) {
        return Optional.of(new DataPointDroppedEvent(
                "Incorrect format", "data:" + Arrays.toString(payload)
        ));
    }

    private boolean hasValidRanges(float tempTz, float tempTt, float out) {
        if (
                (tempTz < 0 || tempTz > 100)
                || (tempTt < 0 || tempTt > 100)
                || (out < 0 || out > 100)
        )
            return false;

        return true;
    }
}
