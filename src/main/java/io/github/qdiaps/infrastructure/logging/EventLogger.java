package io.github.qdiaps.infrastructure.logging;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.event.FailedEvent;
import io.github.qdiaps.domain.event.LoggableEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class EventLogger {
    public EventLogger(EventBus eventBus) {
        Objects.requireNonNull(eventBus);

        eventBus.subscribeToAll(event -> {
            switch (event) {
                case LoggableEvent e -> log.info("Info event | {}", e);
                case FailedEvent e -> log.warn("Failed event | {}", e);
                default -> log.debug("Debug event | {}", event);
            }
        });
    }
}
