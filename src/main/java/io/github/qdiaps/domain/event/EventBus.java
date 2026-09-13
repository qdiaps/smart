package io.github.qdiaps.domain.event;

import java.util.function.Consumer;

public interface EventBus {
    <T extends Event> void subscribe(Class<T> eventType, Consumer<T> listener);

    void subscribeToAll(Consumer<Event> listener);

    void post(Event event);
}
