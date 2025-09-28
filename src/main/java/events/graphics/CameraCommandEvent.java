package events.graphics;

import events.core.Event;

public record CameraCommandEvent(Operation op, float value) implements Event {
    public enum Operation {
        DOLLY,
        TRUCK,
        PAN,
        TILT,
        BOOM,
        ROLL,
        RESET
    }
}
