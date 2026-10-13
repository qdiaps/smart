package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.Event;

public record MessageDeliveryFailedEvent(String reason) implements Event {}
