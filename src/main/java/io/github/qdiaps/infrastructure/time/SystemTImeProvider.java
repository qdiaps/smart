package io.github.qdiaps.infrastructure.time;

import io.github.qdiaps.domain.service.TimeProvider;

public class SystemTImeProvider implements TimeProvider {
    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
