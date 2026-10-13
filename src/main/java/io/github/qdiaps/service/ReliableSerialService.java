package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class ReliableSerialService implements AutoCloseable {
    private final SerialSendService sendService;
    private final EventBus eventBus;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ReliableSerialService-scheduler");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, CompletableFuture<Void>> pendingAcks = new ConcurrentHashMap<>();

    public ReliableSerialService(SerialSendService sendService, EventBus eventBus) {
        Objects.requireNonNull(sendService);
        Objects.requireNonNull(eventBus);

        this.sendService = sendService;
        this.eventBus = eventBus;
    }

    public void close() {
        scheduler.shutdown();
    }

    public void confirmAck(String ackKey) {
        CompletableFuture<Void> future = pendingAcks.remove(ackKey);
        if (future != null) {
            log.debug("Acknowledgment '{}' confirmed", ackKey);
            future.complete(null);
        }
    }

    public CompletableFuture<Void> sendWithRetry(String message, String expectedAck, int maxRetries, long timeoutMs) {
        log.debug("Sending message '{}' expecting ack '{}' (max retries: {}, timeout: {} ms)",
                message, expectedAck, maxRetries, timeoutMs);
        CompletableFuture<Void> future = new CompletableFuture<>();
        pendingAcks.put(expectedAck, future);

        attemptSend(message, expectedAck, maxRetries, timeoutMs, future);

        return future;
    }

    private void attemptSend(
            String message, String expectedAck, int retriesLeft, long timeoutMs,
            CompletableFuture<Void> future
    ) {
        if (future.isDone()) return;

        if (retriesLeft < 0) {
            log.warn("Command '{}' failed: no response '{}' received within timeout", message, expectedAck);
            future.completeExceptionally(new TimeoutException());
            eventBus.post(new MessageDeliveryFailedEvent(
                    "No response '" + expectedAck + "' was received for the command '" + message + "'"
            ));
            pendingAcks.remove(expectedAck);
            return;
        }

        sendService.send(message);

        scheduler.schedule(() ->
                attemptSend(message, expectedAck, retriesLeft - 1, timeoutMs, future),
                timeoutMs, TimeUnit.MILLISECONDS
        );
    }
}
