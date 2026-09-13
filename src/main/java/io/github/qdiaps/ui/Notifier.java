package io.github.qdiaps.ui;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public class Notifier {
    private VBox container;

    public void init(VBox container) {
        Objects.requireNonNull(container);

        this.container = container;
    }

    public void showInfo(String message) {
        var notification = new Notification(message);
        notification.getStyleClass().addAll(Styles.ELEVATED_2, Styles.SUCCESS);
        showWithTimeout(notification, 3);
    }

    public void showWarn(String message) {
        var notification = new Notification(message);
        notification.getStyleClass().addAll(Styles.ELEVATED_2, Styles.WARNING);
        showWithTimeout(notification, 5);
    }

    public void showError(String message) {
        var notification = new Notification(message);
        notification.getStyleClass().addAll(Styles.ELEVATED_2, Styles.DANGER);

        notification.setOnClose(e -> container.getChildren().remove(notification));
        container.getChildren().add(notification);
    }

    public void showConfirmationDialog(
            String title, String message,
            String firstButtonName, String secondButtonName,
            Runnable onClickFirstButton, Runnable onClickSecondButton
    ) {
        var dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        var firstButton = new ButtonType(firstButtonName, ButtonBar.ButtonData.OK_DONE);
        var secondButton = new ButtonType(secondButtonName, ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(firstButton, secondButton);

        dialog.showAndWait().ifPresent(button -> {
            if (button == firstButton && onClickFirstButton != null)
                onClickFirstButton.run();
            else if (button == secondButton && onClickSecondButton != null)
                onClickSecondButton.run();
        });
    }

    private void showWithTimeout(Notification notification, int seconds) {
        notification.setOnClose(e -> container.getChildren().remove(notification));
        container.getChildren().add(notification);

        var pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> container.getChildren().remove(notification));
        pause.play();
    }
}
