package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;

public record TimeLimitReachedEvent(
        float timeLimit
) implements DomainEvent {
}
