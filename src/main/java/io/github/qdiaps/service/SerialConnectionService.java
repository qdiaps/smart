package io.github.qdiaps.service;

import io.github.qdiaps.infrastructure.serial.Serial;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SerialConnectionService {
    private final Serial serial;

    public List<String> getAvailablePorts() {
        return serial.getPorts();
    }

    public void connect(String portName, int baudRate) {
        serial.openPort(portName, baudRate);
    }

    public void disconnect() {
        serial.closePort();
    }
}
