package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.infrastructure.event.LoggableEvent;

public record SerialConnectionEstablishedEvent(
        String portName,
        int baudRate
) implements LoggableEvent { }
