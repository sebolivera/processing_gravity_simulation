package events.input;

import events.core.Event;

/**
 * Event fired when the mouse button state changes.
 * <i>Why did the mouse cross from NY to NJ?</i>
 */
public record MouseStateChangedEvent(
        boolean pressed,
        int button
) implements Event {
}
