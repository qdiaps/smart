package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.Event;
import io.github.qdiaps.domain.model.PidParameters;

public interface PidEvent extends Event {
    record ParametersUpdated(PidParameters parameters) implements PidEvent {}
}
