package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.PidParameters;
import io.github.qdiaps.infrastructure.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class PidService {
    private final ReliableSerialService reliableService;
    private final EventBus eventBus;

    public CompletableFuture<Void> sendParameters(PidParameters parameters) {
        Objects.requireNonNull(parameters, "parameters cannot be null");

        String command = parameters.toCommandString();
        log.info("Sending PID parameters: tempTz={}, Kp={}, Ki={}, Kd={}",
                parameters.tempTz(), parameters.kp(), parameters.ki(), parameters.kd());

        return reliableService.sendWithRetry(command, "pid:ack", Config.PID_MAX_RETRIES, Config.PID_TIMEOUT_MS)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PID parameters successfully acknowledged by device");
                        eventBus.post(new PidEvent.ParametersUpdated(parameters));
                    } else {
                        log.warn("Failed to deliver PID parameters: device did not acknowledge");
                    }
                });
    }
}
