package events.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central event manager for the app.
 */
public final class EventManager {
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
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
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventHandler<? extends Event>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            logger.trace("Publishing event {} to {} handlers", event.getClass().getSimpleName(), eventHandlers.size());
            for (EventHandler<? extends Event> handler : eventHandlers) {
                try {
                    ((EventHandler<T>) handler).accept(event);
                } catch (Exception e) {
                    logger.error("Error handling event {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        } else {
            logger.trace("No handlers registered for event type: {}", event.getClass().getSimpleName());
        }
    }
}
