package io.github.qdiaps.infrastructure.serial;

import io.github.qdiaps.domain.event.Event;

public interface SerialEvent extends Event {
    record ConnectionEstablished(String portName, int baudRate) implements SerialEvent {}
    record ConnectionFailed(String reason) implements SerialEvent {}
    record ConnectionLost(String reason) implements SerialEvent {}
    record Disconnected() implements SerialEvent {}

    record DataReceived(String data) implements SerialEvent, Event {}
    record DataSent(String data) implements SerialEvent {}
    record DataSendFailed(String reason) implements SerialEvent {}

    record PingSuccess() implements SerialEvent {}
    record PingFailed(String reason) implements SerialEvent {}
}
