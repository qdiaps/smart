package io.github.qdiaps.infrastructure.serial;

import com.fazecast.jSerialComm.*;
import io.github.qdiaps.domain.event.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class Serial {
    private final EventBus eventBus;

    private SerialPort port;
    private boolean isReadyToRead;

    public List<String> getPorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toList();
    }

    public void openPort(String portName, int baudRate) {
        if (port != null && port.isOpen()) {
            eventBus.post(new SerialEvent.ConnectionFailed("Port is already open"));
            return;
        }

        SerialPort tempPort;
        try {
            tempPort = SerialPort.getCommPort(portName);
        } catch (SerialPortInvalidPortException e) {
            eventBus.post(new SerialEvent.ConnectionFailed("Invalid port name: " + portName));
            return;
        }

        port = tempPort;

        if (!port.setBaudRate(baudRate)) {
            eventBus.post(new SerialEvent.ConnectionFailed("Invalid baudRate: " + baudRate));
            return;
        }

        if (!port.openPort()) {
            eventBus.post(new SerialEvent.ConnectionFailed("Port is currently in use by another process"));
            return;
        }

        setupPortListener();

        CompletableFuture.runAsync(() -> {
            if (port != null && port.isOpen()) {
                port.flushIOBuffers();
                isReadyToRead = true;
                eventBus.post(new SerialEvent.ConnectionEstablished(portName, baudRate));
            }
        }, CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));
    }

    public void closePort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            port = null;
            isReadyToRead = false;
            eventBus.post(new SerialEvent.Disconnected());
        }
    }

    public void send(String rawData) {
        if (port == null || !port.isOpen() || !isReadyToRead) {
            eventBus.post(new SerialEvent.DataSendFailed("Port is not connected"));
            return;
        }

        var data = rawData + "\n";
        try {
            OutputStream out = port.getOutputStream();

            out.write(data.getBytes());
            out.flush();

            eventBus.post(new SerialEvent.DataSent(rawData));
        } catch (Exception e) {
            eventBus.post(new SerialEvent.DataSendFailed("Write error: " + e.getMessage()));
        }
    }

    private void setupPortListener() {
        port.addDataListener(new SerialPortMessageListener() {
            @Override
            public byte[] getMessageDelimiter() {
                return new byte[] { '\n' };
            }

            @Override
            public boolean delimiterIndicatesEndOfMessage() {
                return true;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED
                        | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if ((event.getEventType() & SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) > 0) {
                    if (port != null) {
                        port.closePort();
                        port = null;
                        isReadyToRead = false;
                    }
                    eventBus.post(new SerialEvent.ConnectionLost("Device disconnected unexpectedly"));
                } else if ((event.getEventType() & SerialPort.LISTENING_EVENT_DATA_RECEIVED) > 0) {
                    if (!isReadyToRead)
                        return;

                    String data = new String(event.getReceivedData()).trim();
                    eventBus.post(new SerialEvent.DataReceived(data));
                }
            }
        });
    }
}
