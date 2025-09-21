package events.graphics.gui;

import events.core.Event;

/**
 * Hover event
 */
public class GUIHoverEvent implements Event {
    private final String elementId;
    private final boolean isHovered;
    private final int x, y;

    public GUIHoverEvent(String elementId, boolean isHovered, int x, int y) {
        this.elementId = elementId;
        this.isHovered = isHovered;
        this.x = x;
        this.y = y;
    }

    public String getElementId() {
        return elementId;
    }

    public boolean isHovered() {
        return isHovered;
    }
}
