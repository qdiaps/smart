package io.github.qdiaps.ui.controller;

import io.github.qdiaps.ui.Notifier;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MainController {
    private final Notifier notifier;

    @FXML
    private VBox notificationContainer;

    @FXML
    private void initialize() {
        notifier.init(notificationContainer);
    }
}
