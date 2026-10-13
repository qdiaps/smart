package io.github.qdiaps.ui.controller;

import atlantafx.base.theme.Styles;
import io.github.qdiaps.domain.command.DataPointAddedEvent;
import io.github.qdiaps.domain.command.TimeLimitReachedEvent;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.PidParameters;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import io.github.qdiaps.service.ExperimentControlService;
import io.github.qdiaps.service.PidService;
import io.github.qdiaps.service.SessionEvent;
import io.github.qdiaps.service.SessionService;
import io.github.qdiaps.ui.Notifier;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RightPanelController {
    private final SessionService sessionService;
    private final ExperimentControlService experimentControlService;
    private final PidService pidService;
    private final EventBus eventBus;
    private final Notifier notifier;

    @FXML
    private TextField timeScaleField;
    @FXML
    private Button applyTimeScaleButton;
    @FXML
    private TextField tempTzField;
    @FXML
    private TextField proportionalField;
    @FXML
    private TextField integralField;
    @FXML
    private TextField differentialField;
    @FXML
    private Button applyPidButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resetButton;

    private boolean isControllerConnected = false;

    @FXML
    private void initialize() {
        updateFieldWithCurrentMaxTime();
        updatePauseButtonUI(experimentControlService.isPaused());

        applyTimeScaleButton.setOnAction(e -> onApplyTimeScale());
        timeScaleField.setOnAction(e -> onApplyTimeScale());

        applyPidButton.setDisable(!isControllerConnected);
        applyPidButton.setOnAction(e -> onApplyPid());
        tempTzField.setOnAction(e -> onApplyPid());
        proportionalField.setOnAction(e -> onApplyPid());
        integralField.setOnAction(e -> onApplyPid());
        differentialField.setOnAction(e -> onApplyPid());

        pauseButton.setOnAction(e -> onTogglePause());

        resetButton.setDisable(sessionService.getPointCount() == 0);
        resetButton.setOnAction(e -> onResetData());

        eventBus.subscribe(SerialEvent.PingSuccess.class, e -> Platform.runLater(() -> {
            isControllerConnected = true;
            applyPidButton.setDisable(false);
        }));

        eventBus.subscribe(SerialEvent.Disconnected.class, e -> Platform.runLater(() -> {
            isControllerConnected = false;
            applyPidButton.setDisable(true);
        }));

        eventBus.subscribe(SerialEvent.ConnectionLost.class, e -> Platform.runLater(() -> {
            isControllerConnected = false;
            applyPidButton.setDisable(true);
        }));

        eventBus.subscribe(SerialEvent.ConnectionFailed.class, e -> Platform.runLater(() -> {
            isControllerConnected = false;
            applyPidButton.setDisable(true);
        }));

        eventBus.subscribe(SessionEvent.TimeLimitChanged.class, e ->
                Platform.runLater(this::updateFieldWithCurrentMaxTime)
        );

        eventBus.subscribe(TimeLimitReachedEvent.class, e ->
                Platform.runLater(() -> notifier.showWarn(
                        String.format("Досягнуто ліміту шкали часу (%.0f с). Нові точки не записуються.", e.timeLimit())
                ))
        );

        eventBus.subscribe(SessionEvent.Paused.class, e ->
                Platform.runLater(() -> updatePauseButtonUI(true))
        );

        eventBus.subscribe(SessionEvent.Resumed.class, e ->
                Platform.runLater(() -> updatePauseButtonUI(false))
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
                Platform.runLater(() -> {
                    resetButton.setDisable(true);
                    updatePauseButtonUI(false);
                })
        );
    }

    private void onApplyPid() {
        if (!isControllerConnected) {
            return;
        }

        List<String> invalidFields = new ArrayList<>();

        Float tempTz = parseAndValidate(tempTzField.getText(), Config.MIN_TEMP_TZ, Config.MAX_TEMP_TZ);
        if (tempTz == null) {
            invalidFields.add("Цільова температура");
        }

        Float kp = parseAndValidate(proportionalField.getText(), Config.MIN_PID_COEFF, Config.MAX_PID_COEFF);
        if (kp == null) {
            invalidFields.add("Пропорційний (Р)");
        }

        Float ki = parseAndValidate(integralField.getText(), Config.MIN_PID_COEFF, Config.MAX_PID_COEFF);
        if (ki == null) {
            invalidFields.add("Інтегральний (І)");
        }

        Float kd = parseAndValidate(differentialField.getText(), Config.MIN_PID_COEFF, Config.MAX_PID_COEFF);
        if (kd == null) {
            invalidFields.add("Диференціальний (D)");
        }

        if (!invalidFields.isEmpty()) {
            notifier.showWarn(String.format(
                    "Виявлено некоректні або порожні значення в полях: %s. Будь ласка, введіть дійсні числа.",
                    String.join(", ", invalidFields)
            ));
            return;
        }

        applyPidButton.setDisable(true);
        var pidParameters = new PidParameters(tempTz, kp, ki, kd);

        pidService.sendParameters(pidParameters)
                .thenRun(() -> Platform.runLater(() -> {
                    applyPidButton.setDisable(!isControllerConnected);
                    notifier.showInfo("Нові коефіцієнти ПІД-регулятора успішно передані на Arduino");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        applyPidButton.setDisable(!isControllerConnected);
                        notifier.showError("Не вдалося відправити дані. Arduino не відповідає на запит.");
                    });
                    return null;
                });
    }

    private Float parseAndValidate(String text, float min, float max) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            float val = Float.parseFloat(text.trim().replace(',', '.'));
            if (val < min || val > max || Float.isNaN(val) || Float.isInfinite(val)) {
                return null;
            }
            return val;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void onTogglePause() {
        if (experimentControlService.isPaused()) {
            experimentControlService.resumeExperiment();
            notifier.showInfo("Експеримент відновлено.");
        } else {
            experimentControlService.pauseExperiment();
            notifier.showInfo("Експеримент призупинено.");
        }
    }

    private void updatePauseButtonUI(boolean isPaused) {
        if (isPaused) {
            pauseButton.setText("Продовжити");
            pauseButton.getStyleClass().removeAll(Styles.SUCCESS);
            pauseButton.getStyleClass().add(Styles.SUCCESS);
        } else {
            pauseButton.setText("Пауза");
            pauseButton.getStyleClass().removeAll(Styles.SUCCESS);
        }
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
