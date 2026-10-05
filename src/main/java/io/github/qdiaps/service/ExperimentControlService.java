package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.ExperimentSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExperimentControlService {
    private final ExperimentSession session;
    private final EventBus eventBus;

    public void pauseExperiment() {
        if (!session.isPaused()) {
            session.pause();
            eventBus.post(new SessionEvent.Paused());
        }
    }

    public void resumeExperiment() {
        if (session.isPaused()) {
            session.resume();
            eventBus.post(new SessionEvent.Resumed());
        }
    }

    public void stopExperiment() {
        session.reset();
        eventBus.post(new SessionEvent.Stopped());
    }

    public boolean isPaused() {
        return session.isPaused();
    }
}
