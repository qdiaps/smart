package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.domain.event.Event;
import io.github.qdiaps.domain.event.FailedEvent;
import io.github.qdiaps.domain.event.LoggableEvent;

public interface SerialEvent extends Event {
    record ConnectionEstablished(String portName, int baudRate) implements SerialEvent, LoggableEvent {}
    record ConnectionFailed(String reason) implements SerialEvent, FailedEvent {}
    record ConnectionLost(String reason) implements SerialEvent, FailedEvent {}
    record Disconnected() implements SerialEvent, LoggableEvent {}

    record DataReceived(String data) implements SerialEvent, Event {}
    record DataSent(String data) implements SerialEvent, LoggableEvent{}
    record DataSendFailed(String reason) implements SerialEvent, FailedEvent {}
}
