package io.github.qdiaps.ui.controller;

import atlantafx.base.theme.Styles;
import io.github.qdiaps.infrastructure.event.EventBus;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import io.github.qdiaps.service.SerialConnectionService;
import io.github.qdiaps.ui.Notifier;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TopPanelController {
    private enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    private final SerialConnectionService connectionService;
    private final EventBus eventBus;
    private final Notifier notifier;

    @FXML
    private ComboBox<String> portName;
    @FXML
    private ComboBox<Integer> baudRate;
    @FXML
    private Button connectionButton;
    @FXML
    private Label status;

    private ConnectionStatus currentStatus = ConnectionStatus.DISCONNECTED;

    @FXML
    private void initialize() {
        portName.setOnShowing(e -> updatePorts());
        baudRate.getItems().addAll(
                300, 1200, 2400, 4800, 9600,
                14400, 19200, 38400, 57600, 115200,
                230400, 250000, 500000, 1000000
        );
        connectionButton.setOnAction(e -> onClickConnection());

        eventBus.subscribe(SerialEvent.ConnectionEstablished.class, e -> Platform.runLater(this::onConnectionEstablished));
        eventBus.subscribe(SerialEvent.ConnectionFailed.class, e -> Platform.runLater(() -> onConnectionFailed(e)));
        eventBus.subscribe(SerialEvent.ConnectionLost.class, e -> Platform.runLater(this::onConnectionLost));
        eventBus.subscribe(SerialEvent.Disconnected.class, e -> Platform.runLater(this::onDisconnected));
    }

    private void updatePorts() {
        portName.getItems().clear();
        portName.getItems().addAll(connectionService.getAvailablePorts());
    }

    private void onClickConnection() {
        switch (currentStatus) {
            case DISCONNECTED -> connect();
            case CONNECTED -> disconnect();
        }
    }

    private void connect() {
        currentStatus = ConnectionStatus.CONNECTING;
        connectionButton.setDisable(true);
        status.setText("Підключення...");

        portName.setDisable(true);
        baudRate.setDisable(true);

        String port = portName.getValue();
        Integer baud = baudRate.getValue();
        if (baud == null)
            baud = 9600;

        connectionService.connect(port, baud);
    }

    private void disconnect() {
        currentStatus = ConnectionStatus.DISCONNECTING;
        connectionButton.setDisable(true);
        status.setText("Від'єднання...");
        connectionService.disconnect();
    }

    private void onConnectionEstablished() {
        currentStatus = ConnectionStatus.CONNECTED;
        connectionButton.setDisable(false);
        connectionButton.setText("Відключити");
        connectionButton.getStyleClass().removeAll(Styles.DANGER, Styles.SUCCESS);
        connectionButton.getStyleClass().add(Styles.DANGER);
        status.setText("Підключено");

        portName.setDisable(false);
        baudRate.setDisable(false);

        notifier.showInfo("Успішно підключено до Arduino. Розпочато обмін даними.");
    }

    private void onConnectionFailed(SerialEvent.ConnectionFailed event) {
        currentStatus = ConnectionStatus.DISCONNECTED;
        setDefaultUI();

        if (event.reason().equals("Invalid port name: null"))
            notifier.showWarn("Будь ласка, оберіть COM-порт.");
        else
            notifier.showWarn("Не вдалося підключитися до Arduino. Перевірте правильність вибору COM-порту та швидкості передачі (Baud rate).");
    }

    private void onConnectionLost() {
        currentStatus = ConnectionStatus.DISCONNECTED;
        setDefaultUI();
        notifier.showWarn("Зв'язок з Arduino несподівано розірвано. Перевірте фізичне підключення кабелю до пристрою.");
    }

    private void onDisconnected() {
        currentStatus = ConnectionStatus.DISCONNECTED;
        setDefaultUI();
        notifier.showInfo("Зв'язок з Arduino успішно завершено. Порт закрито.");
    }

    private void setDefaultUI() {
        connectionButton.setDisable(false);
        connectionButton.setText("Підключити");
        connectionButton.getStyleClass().removeAll(Styles.DANGER, Styles.SUCCESS);
        connectionButton.getStyleClass().add(Styles.SUCCESS);
        status.setText("Немає зв'язку");

        portName.setDisable(false);
        baudRate.setDisable(false);
    }
}
