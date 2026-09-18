package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.event.FailedEvent;

public record DataPointDroppedEvent(
        String reason,
        String command
) implements DomainEvent, FailedEvent {}
