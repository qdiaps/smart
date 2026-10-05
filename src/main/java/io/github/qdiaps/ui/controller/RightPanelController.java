package io.github.qdiaps.ui.controller;

import io.github.qdiaps.domain.command.DataPointAddedEvent;
import io.github.qdiaps.domain.command.TimeLimitReachedEvent;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.service.SessionEvent;
import io.github.qdiaps.service.SessionService;
import io.github.qdiaps.ui.Notifier;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RightPanelController {
    private final SessionService sessionService;
    private final EventBus eventBus;
    private final Notifier notifier;

    @FXML
    private TextField timeScaleField;
    @FXML
    private Button applyTimeScaleButton;
    @FXML
    private Button resetButton;

    @FXML
    private void initialize() {
        updateFieldWithCurrentMaxTime();

        applyTimeScaleButton.setOnAction(e -> onApplyTimeScale());
        timeScaleField.setOnAction(e -> onApplyTimeScale());

        resetButton.setDisable(sessionService.getPointCount() == 0);
        resetButton.setOnAction(e -> onResetData());

        eventBus.subscribe(SessionEvent.TimeLimitChanged.class, e ->
                Platform.runLater(this::updateFieldWithCurrentMaxTime)
        );

        eventBus.subscribe(TimeLimitReachedEvent.class, e ->
                Platform.runLater(() -> notifier.showWarn(
                        String.format("Досягнуто ліміту шкали часу (%.0f с). Нові точки не записуються.", e.timeLimit())
                ))
        );

        eventBus.subscribe(DataPointAddedEvent.class, e -> {
            if (resetButton.isDisabled()) {
                Platform.runLater(() -> resetButton.setDisable(false));
            }
        });

        eventBus.subscribe(SessionEvent.PointsLoaded.class, e ->
                Platform.runLater(() -> resetButton.setDisable(e.points().isEmpty()))
        );

        eventBus.subscribe(SessionEvent.PointsCleared.class, e ->
                Platform.runLater(() -> resetButton.setDisable(true))
        );
    }

    private void updateFieldWithCurrentMaxTime() {
        float maxTime = sessionService.getMaxTime();
        if (maxTime == (long) maxTime) {
            timeScaleField.setText(String.valueOf((long) maxTime));
        } else {
            timeScaleField.setText(String.valueOf(maxTime));
        }
    }

    private void onApplyTimeScale() {
        String text = timeScaleField.getText();
        if (text == null || text.isBlank()) {
            notifier.showWarn("Будь ласка, введіть значення шкали часу.");
            updateFieldWithCurrentMaxTime();
            return;
        }

        float newMaxTime;
        try {
            newMaxTime = Float.parseFloat(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            notifier.showWarn("Некоректний формат числа для шкали часу.");
            updateFieldWithCurrentMaxTime();
            return;
        }

        if (newMaxTime <= 0) {
            notifier.showWarn("Шкала часу має бути більшою за 0.");
            updateFieldWithCurrentMaxTime();
            return;
        }

        if (sessionService.canSetMaxTimeWithoutLoss(newMaxTime)) {
            sessionService.setMaxTime(newMaxTime, false);
            notifier.showInfo(String.format("Шкалу часу успішно змінено на %.0f с.", newMaxTime));
        } else {
            notifier.showConfirmationDialog(
                    "Зміна шкали часу",
                    String.format(
                            "Остання точка записана на %.1f с. Якщо ви зменшите шкалу до %.0f с, усі точки після цього часу буде безповоротно видалено. Продовжити?",
                            sessionService.getLastPointTime(), newMaxTime
                    ),
                    "Так, обрізати",
                    "Скасувати",
                    () -> {
                        sessionService.setMaxTime(newMaxTime, true);
                        notifier.showWarn(String.format("Шкалу часу зменшено до %.0f с. Зайві точки обрізано.", newMaxTime));
                    },
                    this::updateFieldWithCurrentMaxTime
            );
        }
    }

    private void onResetData() {
        if (sessionService.getPointCount() == 0) {
            return;
        }

        notifier.showConfirmationDialog(
                "Скидання та збереження експерименту",
                "Поточні дані експерименту буде збережено у файл, а графік та пам'ять очищено для нового запису. Продовжити?",
                "Зберегти та скинути",
                "Скасувати",
                () -> {
                    sessionService.resetSession();
                    notifier.showInfo("Дані експерименту збережено, графік очищено для нового запису.");
                },
                null
        );
    }
}
