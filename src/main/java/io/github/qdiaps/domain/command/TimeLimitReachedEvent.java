package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;
import io.github.qdiaps.domain.event.LoggableEvent;

public record TimeLimitReachedEvent(
        float timeLimit
) implements DomainEvent, LoggableEvent {
}
