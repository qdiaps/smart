package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.FailedEvent;

public record DataSendFailedEvent(
        String reason
) implements FailedEvent {}
