package events.gui;

import events.core.Event;

/**
 * State changes events in the GUI.
 * <i>Lucky there's a famili GUI.</i>
 */
public record GUIStateChangedEvent(UIElement element, boolean newState) implements Event {
    public enum UIElement {
        VELOCITY_ARROWS,
        SPHERE_NAMES,
        SPHERE_WEIGHTS,
        SPHERE_TRAILS,
        GRAVITY_ENABLED,
        BOUNDS_ENABLED,
        FREE_CAM,
        INTERFACE_VISIBLE,
        SIMULATION_PAUSED
    }

}
