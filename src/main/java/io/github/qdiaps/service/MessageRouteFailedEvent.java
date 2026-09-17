package io.github.qdiaps.service;

import io.github.qdiaps.domain.event.Event;

public record MessageRouteFailedEvent(String reason) implements Event {}
