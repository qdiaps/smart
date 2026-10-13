package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.ExperimentSession;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentControlService {
    private final ExperimentSession session;
    private final EventBus eventBus;

    private boolean wasAutoPaused = false;

    public ExperimentControlService(ExperimentSession session, EventBus eventBus) {
        this.session = session;
        this.eventBus = eventBus;

        this.eventBus.subscribe(SerialEvent.ConnectionLost.class, e -> autoPause());
        this.eventBus.subscribe(SerialEvent.Disconnected.class, e -> autoPause());
        this.eventBus.subscribe(SerialEvent.PingSuccess.class, e -> autoResume());
        this.eventBus.subscribe(SessionEvent.PointsCleared.class, e -> resetAutoPause());
    }

    public synchronized void pauseExperiment() {
        wasAutoPaused = false;
        if (!session.isPaused()) {
            session.pause();
            log.info("Experiment paused by user");
            eventBus.post(new SessionEvent.Paused());
        }
    }

    public synchronized void resumeExperiment() {
        wasAutoPaused = false;
        if (session.isPaused()) {
            session.resume();
            log.info("Experiment resumed by user");
            eventBus.post(new SessionEvent.Resumed());
        }
    }

    public synchronized void stopExperiment() {
        wasAutoPaused = false;
        session.reset();
        log.info("Experiment stopped");
        eventBus.post(new SessionEvent.Stopped());
    }

    public synchronized boolean isPaused() {
        return session.isPaused();
    }

    private synchronized void autoPause() {
        if (session.hasStarted() && !session.isPaused()) {
            wasAutoPaused = true;
            session.pause();
            log.warn("Experiment auto-paused due to connection disruption");
            eventBus.post(new SessionEvent.Paused());
        }
    }

    private synchronized void autoResume() {
        if (wasAutoPaused && session.isPaused()) {
            wasAutoPaused = false;
            session.resume();
            log.info("Experiment auto-resumed after connection restored");
            eventBus.post(new SessionEvent.Resumed());
        }
    }

    private synchronized void resetAutoPause() {
        wasAutoPaused = false;
    }
}
