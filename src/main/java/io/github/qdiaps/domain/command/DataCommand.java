package io.github.qdiaps.domain.command;

import io.github.qdiaps.domain.event.DomainEvent;

import java.util.Optional;

public class DataCommand implements Command {
    @Override
    public Optional<DomainEvent> execute(String[] payload) {
        throw new UnsupportedOperationException();
    }
}
