package events.physics;

import events.core.Event;

/**
 * Event fired when gravity value changes.
 * <i>I don't think you're grasping the gravity of the situation.</i>
 */
public final class GravityChangedEvent implements Event {
    private final float newGravity;

    public GravityChangedEvent(float newGravity) {
        this.newGravity = newGravity;
    }

    public float getNewGravity() {
        return newGravity;
    }
}
