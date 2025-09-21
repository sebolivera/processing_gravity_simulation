package events.physics;

import events.core.Event;

/**
 * Event fired when gravity value changes. <i>I don't think you're grasping the gravity of the
 * situation.</i>
 */
public record GravityChangedEvent(float newGravity) implements Event {}
