package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.event.LoggableEvent;

public record DataPointAddedEvent(
        float time,
        float tempTz,
        float tempTt,
        float out
) implements DomainEvent, LoggableEvent {}
