package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.LoggableEvent;

public record DataSentEvent(
        String data
) implements LoggableEvent {}
