package io.github.qdiaps.ui.controller;

import atlantafx.base.theme.Styles;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.config.Config;
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
        CHECKING,
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
    private boolean isInitialHandshake = false;

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
        eventBus.subscribe(SerialEvent.PingSuccess.class, e -> Platform.runLater(this::onPingSuccess));
        eventBus.subscribe(SerialEvent.PingFailed.class, e -> Platform.runLater(this::onPingFailed));
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
            baud = Config.DEFAULT_BAUD_RATE;

        connectionService.connect(port, baud);
    }

    private void onConnectionEstablished() {
        currentStatus = ConnectionStatus.CHECKING;
        isInitialHandshake = true;
        status.setText("Перевірка зв'язку...");
        connectionService.checkConnection();
    }

    private void onPingSuccess() {
        currentStatus = ConnectionStatus.CONNECTED;
        setConnectedUI();

        if (isInitialHandshake) {
            isInitialHandshake = false;
            notifier.showInfo("Успішно підключено до Arduino. Розпочато обмін даними.");
        } else {
            notifier.showInfo("Arduino успішно відповів на перевірочний сигнал. " +
                    "З'єднання активне та працює в штатному режимі.");
        }
    }

    private void onPingFailed() {
        if (isInitialHandshake) {
            connectionService.disconnect();
            notifier.showError("Пристрій не розпізнано. Порт закрито через відсутність відповіді. " +
                    "Перевірте скетч та налаштування порту.");
        } else {
            currentStatus = ConnectionStatus.CONNECTED;
            setConnectedUI();
            notifier.showError("Arduino не надав відповіді на запит (таймаут). " +
                    "Перевірте фізичне підключення кабелю або стан живлення плати.");
        }
    }

    private void disconnect() {
        isInitialHandshake = false;
        currentStatus = ConnectionStatus.DISCONNECTING;
        connectionButton.setDisable(true);
        status.setText("Від'єднання...");
        connectionService.disconnect();
    }

    private void onConnectionFailed(SerialEvent.ConnectionFailed event) {
        currentStatus = ConnectionStatus.DISCONNECTED;
        isInitialHandshake = false;
        setDefaultUI();

        if (event.reason().equals("Invalid port name: null"))
            notifier.showWarn("Будь ласка, оберіть COM-порт.");
        else
            notifier.showWarn("Не вдалося підключитися до Arduino. Перевірте правильність вибору COM-порту та швидкості передачі (Baud rate).");
    }

    private void onConnectionLost() {
        currentStatus = ConnectionStatus.DISCONNECTED;
        isInitialHandshake = false;
        setDefaultUI();
        notifier.showWarn("Зв'язок з Arduino несподівано розірвано. Перевірте фізичне підключення кабелю до пристрою.");
    }

    private void onDisconnected() {
        currentStatus = ConnectionStatus.DISCONNECTED;
        setDefaultUI();
        if (!isInitialHandshake) {
            notifier.showInfo("Зв'язок з Arduino успішно завершено. Порт закрито.");
        }
        isInitialHandshake = false;
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

    private void setConnectedUI() {
        connectionButton.setDisable(false);
        connectionButton.setText("Відключити");
        connectionButton.getStyleClass().removeAll(Styles.DANGER, Styles.SUCCESS);
        connectionButton.getStyleClass().add(Styles.DANGER);
        status.setText("Підключено");

        portName.setDisable(false);
        baudRate.setDisable(false);
    }
}
