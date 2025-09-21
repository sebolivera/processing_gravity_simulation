package events.input;

import events.core.Event;

/**
 * Event fired when the mouse position changes. <i>This mouse definitely isn't dead, I saw it
 * moving.</i>
 */
public record MousePositionChangedEvent(float x, float y, float prevX, float prevY)
        implements Event {}
