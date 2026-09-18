package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.model.DataModel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class DataCommand implements Command {
    private final DataModel data;

    @Override
    public Optional<DomainEvent> execute(String[] payload) {
        if (payload == null || payload.length != 3)
            return getIncorrectFormatEvent(payload);
        else if (!data.canAddPoint())
            return Optional.of(new DataPointDroppedEvent(
                    "Cannot add new point", "data:" + Arrays.toString(payload)
            ));

        float tempTz, tempTt, out;
        try {
            tempTz = Float.parseFloat(payload[0]);
            tempTt = Float.parseFloat(payload[1]);
            out = Float.parseFloat(payload[2]);
        } catch (NullPointerException | NumberFormatException e) {
            return getIncorrectFormatEvent(payload);
        }

        if (!hasValidRanges(tempTz, tempTt, out))
            return Optional.of(new DataPointDroppedEvent(
                    "Values are out of acceptable range (0-100)", "data:" + Arrays.toString(payload)
            ));

        // TODO: add arguments...
        data.addPoint();

        // TODO: add time
        return Optional.of(new DataPointAddedEvent(0.0f, tempTz, tempTt, out));
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
