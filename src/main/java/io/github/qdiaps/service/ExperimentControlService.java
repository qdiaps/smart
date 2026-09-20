package io.github.qdiaps.service;

import io.github.qdiaps.domain.model.ExperimentSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExperimentControlService {
    private final ExperimentSession session;

    public void pauseExperiment() {
        session.pause();
    }

    public void resumeExperiment() {
        session.resume();
    }

    public void stopExperiment() {
        session.reset();
    }
}
