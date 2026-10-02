package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.serial.Serial;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class SerialConnectionService {
    private final Serial serial;
    private final ReliableSerialService reliableService;
    private final EventBus eventBus;

    public List<String> getAvailablePorts() {
        return serial.getPorts();
    }

    public void connect(String portName, int baudRate) {
        CompletableFuture.runAsync(() -> serial.openPort(portName, baudRate));
    }

    public void disconnect() {
        CompletableFuture.runAsync(serial::closePort);
    }

    public void checkConnection() {
        CompletableFuture.runAsync(() -> {
            reliableService.sendWithRetry("ping", "pong", Config.PING_MAX_RETRIES, Config.PING_TIMEOUT_MS)
                    .thenRun(() -> eventBus.post(new SerialEvent.PingSuccess()))
                    .exceptionally(ex -> {
                        eventBus.post(new SerialEvent.PingFailed("No pong received from controller"));
                        return null;
                    });
        });
    }
}
