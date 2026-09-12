package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.FailedEvent;

public record SerialConnectionFailedEvent(
        String reason
) implements FailedEvent {}
