package events.graphics.gui;

import events.core.Event;

/** Hover event */
public record GUIHoverEvent(String elementId, boolean isHovered, int x, int y) implements Event {}
