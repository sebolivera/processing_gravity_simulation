package events.graphics;

import damkjer.ocd.Camera;
import events.core.Event;

public record CameraChangedEvent(Camera camera) implements Event {
}