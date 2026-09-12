package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.Event;

public record DataReceivedEvent(
        String rawLine
) implements Event {}
