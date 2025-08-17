package events.gui;

import events.core.Event;

public class DisplaySettingsChangedEvent implements Event {
    public enum DisplaySettings {
        VELOCITY_ARROWS,
        SPHERE_NAMES,
        SPHERE_WEIGHTS,
        SPHERE_TRAILS,
        BOUNDS_VISIBLE
    }

    private final DisplaySettings setting;
    private final boolean enabled;

    public DisplaySettingsChangedEvent(DisplaySettings setting, boolean enabled) {
        this.setting = setting;
        this.enabled = enabled;
    }

    public DisplaySettings getSetting() { return setting; }
    public boolean isEnabled() { return enabled; }
}
