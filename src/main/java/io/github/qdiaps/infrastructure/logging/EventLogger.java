package io.github.qdiaps.infrastructure.logging;

import io.github.qdiaps.infrastructure.event.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class EventLogger {
    public EventLogger(EventBus eventBus) {
        Objects.requireNonNull(eventBus);

        eventBus.subscribeToAll(event -> {
            log.debug("Event => {}", event);
        });
    }
}
