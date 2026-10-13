package io.github.qdiaps.service;

import io.github.qdiaps.domain.command.Command;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class MessageRouteService {
    private final EventBus eventBus;
    private final ReliableSerialService reliableService;
    private final Map<String, Command> commandRegistry;
    private final List<String> ackCommandRegistry;

    public MessageRouteService(
            EventBus eventBus, ReliableSerialService reliableService,
            Map<String, Command> commandRegistry, List<String> ackCommandRegistry
    ) {
        Objects.requireNonNull(eventBus);
        Objects.requireNonNull(reliableService);
        Objects.requireNonNull(commandRegistry);
        Objects.requireNonNull(ackCommandRegistry);

        this.eventBus = eventBus;
        this.reliableService = reliableService;
        this.commandRegistry = commandRegistry;
        this.ackCommandRegistry = ackCommandRegistry;

        this.eventBus.subscribe(SerialEvent.DataReceived.class, this::route);
    }

    private void route(SerialEvent.DataReceived event) {
        String rawMessage = event.data();
        if (rawMessage == null) {
            log.warn("Received empty or null serial message");
            postRouteFailedEvent(null);
            return;
        } else if (ackCommandRegistry.contains(rawMessage)) {
            log.debug("Routing ack message: '{}'", rawMessage);
            reliableService.confirmAck(rawMessage);
            return;
        }

        String[] parts = rawMessage.split(":", 2);
        if (parts.length != 2) {
            log.warn("Malformed serial message format (expected 'cmd:payload'): '{}'", rawMessage);
            postRouteFailedEvent(rawMessage);
            return;
        }

        Command command = commandRegistry.get(parts[0]);
        if (command == null) {
            log.warn("Unknown command in serial message: '{}'", parts[0]);
            postRouteFailedEvent(rawMessage);
            return;
        }

        String[] payload = parts[1].split(";");
        log.debug("Executing command '{}' with payload: {}", parts[0], parts[1]);
        command.execute(payload).ifPresent(eventBus::post);
    }

    private void postRouteFailedEvent(String rawMessage) {
        eventBus.post(new MessageRouteFailedEvent("Message is incorrect: " + rawMessage));
    }
}
