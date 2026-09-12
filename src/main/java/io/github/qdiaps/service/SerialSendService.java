package io.github.qdiaps.service;

import io.github.qdiaps.infrastructure.serial.Serial;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SerialSendService {
    private final Serial serial;

    public void send(String rawData) {
        serial.send(rawData);
    }
}
