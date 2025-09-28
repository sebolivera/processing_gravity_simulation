package events.core;

import java.util.function.Consumer;

/**
 * Functional interface for event handlers (allows lambdas to be used as event handlers). <i>You
 * can't handle my events.</i>
 */
@FunctionalInterface
public interface EventHandler<T extends Event> extends Consumer<T> {}
