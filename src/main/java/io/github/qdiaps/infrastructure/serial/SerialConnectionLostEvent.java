package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.FailedEvent;

public record SerialConnectionLostEvent(
        String reason
) implements FailedEvent {}
