package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.FailedEvent;

public record MessageDeliveryFailedEvent(String reason) implements FailedEvent {}
