package events.physics;

import events.core.Event;

/**
 * Event fired when global speed changes.
 * <i>WE HAV REAHCED MXAIMUN VLELOCIPY</i>
 */
public class SpeedChangedEvent implements Event {
    private final int newSpeed;

    public SpeedChangedEvent(int newSpeed) {
        this.newSpeed = newSpeed;
    }

    public int getNewSpeed() {
        return newSpeed;
    }
}
