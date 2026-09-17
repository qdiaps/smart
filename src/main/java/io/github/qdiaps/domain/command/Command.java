package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;

import java.util.Optional;

public interface Command {
    Optional<DomainEvent> execute(String[] payload);
}
