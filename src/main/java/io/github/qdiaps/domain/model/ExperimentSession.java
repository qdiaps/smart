package io.github.qdiaps.domain.model;

import io.github.qdiaps.domain.service.TimeProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExperimentSession {
    private final TimeProvider time;

    private long lastResumeTimestamp = -1;
    private double accumulatedSeconds = 0.0;
    private boolean isPaused = false;

    public synchronized double getElapsedSeconds() {
        if (isPaused) {
            return accumulatedSeconds;
        }

        if (lastResumeTimestamp == -1) {
            lastResumeTimestamp = time.getCurrentTimeMillis();
            return 0.0;
        }

        long now = time.getCurrentTimeMillis();
        return accumulatedSeconds + (now - lastResumeTimestamp) / 1000.0;
    }

    public synchronized void pause() {
        if (!isPaused) {
            if (lastResumeTimestamp != -1)
                accumulatedSeconds += (time.getCurrentTimeMillis() - lastResumeTimestamp) / 1000.0;
            isPaused = true;
        }
    }

    public synchronized void resume() {
        if (isPaused) {
            if (lastResumeTimestamp != -1)
                lastResumeTimestamp = time.getCurrentTimeMillis();
            isPaused = false;
        }
    }

    public synchronized void reset() {
        lastResumeTimestamp = -1;
        accumulatedSeconds = 0.0;
        isPaused = false;
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }
}
