package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;

public record DataPointDroppedEvent(
        String reason,
        String rawData
) implements DomainEvent {}
