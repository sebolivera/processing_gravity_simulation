package events.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central event manager for the app.
 */
public final class EventManager {
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> handlers = new ConcurrentHashMap<>();

    /**
     * Subscribe to an event.
     * <i>Don't forget to leave a like!</i>
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * Publish an event to all subscribers.
     * <i>Sending spam to all my subscribers 🥰.</i>
     */
    public <T extends Event> void publish(T event) {
        List<EventHandler<? extends Event>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (EventHandler<? extends Event> handler : eventHandlers) {
                try {
                    ((EventHandler<T>) handler).accept(event);
                } catch (Exception e) {
                    System.err.println("Error handling event: " + e.getMessage());
                    e.printStackTrace(); // TODO: implement actual logging
                }
            }
        }
    }
}
