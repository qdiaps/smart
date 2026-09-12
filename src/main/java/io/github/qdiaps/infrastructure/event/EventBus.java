package io.github.qdiaps.infrastructure.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {
    private final Map<Class<? extends Event>, List<Consumer<Event>>> listeners = new ConcurrentHashMap<>();
    private final List<Consumer<Event>> globalListeners = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(event -> listener.accept((T) event));
    }

    public void subscribeToAll(Consumer<Event> listener) {
        globalListeners.add(listener);
    }

    public void post(Event event) {
        var eventListeners = listeners.get(event.getClass());

        if (eventListeners != null) {
            for (var listener : eventListeners)
                listener.accept(event);
        }

        for (var globalListener : globalListeners)
            globalListener.accept(event);
    }
}
