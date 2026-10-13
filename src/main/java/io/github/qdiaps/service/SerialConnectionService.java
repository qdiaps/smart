package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.serial.Serial;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class SerialConnectionService {
    private final Serial serial;
    private final ReliableSerialService reliableService;
    private final EventBus eventBus;

    public List<String> getAvailablePorts() {
        return serial.getPorts();
    }

    public void connect(String portName, int baudRate) {
        log.info("Connecting to {} at {} baud...", portName, baudRate);
        CompletableFuture.runAsync(() -> serial.openPort(portName, baudRate));
    }

    public void disconnect() {
        log.info("Disconnecting from serial port...");
        CompletableFuture.runAsync(serial::closePort);
    }

    public void checkConnection() {
        log.debug("Checking device connection (ping)...");
        CompletableFuture.runAsync(() -> {
            reliableService.sendWithRetry("ping", "pong", Config.PING_MAX_RETRIES, Config.PING_TIMEOUT_MS)
                    .thenRun(() -> {
                        log.info("Device responded to ping successfully");
                        eventBus.post(new SerialEvent.PingSuccess());
                    })
                    .exceptionally(ex -> {
                        log.warn("Device ping failed: no pong received from controller");
                        eventBus.post(new SerialEvent.PingFailed("No pong received from controller"));
                        return null;
                    });
        });
    }
}
