package events.gui;

import events.core.EventManager;
import processing.core.PApplet;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static events.gui.GUIStateChangedEvent.UIElement;

public class GUIManager {
    private final EventManager eventManager;
    private final PApplet parent;

    private final Map<UIElement, Boolean> uiStates = new EnumMap<>(UIElement.class);
    private final Map<String, Boolean> hoverStates = new HashMap<>();

    private final int bottomInitX = 50;
    private final int bottomInitY;
    private final int itemHeight = 20;
    private final int itemSpacing = 50;


    public GUIManager(EventManager eventManager, PApplet parent) {
        this.eventManager = eventManager;
        this.parent = parent;
        this.bottomInitY = parent.height - 50;

        initializeDefaultState();
        setupEventHandlers();
    }

    private void initializeDefaultState() {
        uiStates.put(UIElement.VELOCITY_ARROWS, false);
        uiStates.put(UIElement.SPHERE_NAMES, false);
        uiStates.put(UIElement.SPHERE_WEIGHTS, false);
        uiStates.put(UIElement.SPHERE_TRAILS, false);
        uiStates.put(UIElement.GRAVITY_ENABLED, true);
        uiStates.put(UIElement.BOUNDS_ENABLED, true);
        uiStates.put(UIElement.FREE_CAM, false);
        uiStates.put(UIElement.INTERFACE_VISIBLE, true);
        uiStates.put(UIElement.SIMULATION_PAUSED, false);
    }

    private void setupEventHandlers() {
        eventManager.subscribe(GUIStateChangedEvent.class, this::handleStateChange);
        eventManager.subscribe(GUIHoverEvent.class, this::handleHover);
    }


    /**
     * Handles hover events triggered in the GUI.
     * <i>My handleHover is full of eels.</i>
     *
     * @param event the GUI hover event.
     */
    private void handleHover(GUIHoverEvent event) {
        hoverStates.put(event.getElementId(), event.isHovered());
    }

    private void handleStateChange(GUIStateChangedEvent event) {
        uiStates.put(event.getElement(), event.getNewState());
    }


    public void updateHoverStates() {
        updateHoverState("velocity_arrows", bottomInitX, bottomInitY, 320, itemHeight);
        updateHoverState("sphere_names", bottomInitX, bottomInitY - itemSpacing, 295, itemHeight);
        updateHoverState("sphere_weights", bottomInitX, bottomInitY - itemSpacing * 2, 270, itemHeight);
        updateHoverState("sphere_trails", bottomInitX, bottomInitY - itemSpacing * 3, 220, itemHeight);
        updateHoverState("gravity_enabled", bottomInitX, bottomInitY - itemSpacing * 4, 280, itemHeight);
        updateHoverState("bounds_visible", bottomInitX, bottomInitY - itemSpacing * 5, 240, itemHeight);
    }

    private void updateHoverState(String elementId, int x, int y, int width, int height) {
        boolean isHovered = parent.mouseX >= x && parent.mouseX <= x + width &&
                parent.mouseY >= y && parent.mouseY <= y + height;

        boolean wasHovered = hoverStates.getOrDefault(elementId, false);
        if (isHovered != wasHovered) {
            hoverStates.put(elementId, isHovered);
            eventManager.publish(new GUIHoverEvent(elementId, isHovered, parent.mouseX, parent.mouseY));
        }
    }

    public void handleMouseClick() {
        if (hoverStates.getOrDefault("velocity_arrows", false)) {
            toggleSetting(UIElement.VELOCITY_ARROWS);
        }
        if (hoverStates.getOrDefault("sphere_names", false)) {
            toggleSetting(UIElement.SPHERE_NAMES);
        }
        if (hoverStates.getOrDefault("sphere_weights", false)) {
            toggleSetting(UIElement.SPHERE_WEIGHTS);
        }
        if (hoverStates.getOrDefault("sphere_trails", false)) {
            toggleSetting(UIElement.SPHERE_TRAILS);
        }
        if (hoverStates.getOrDefault("gravity_enabled", false)) {
            toggleSetting(UIElement.GRAVITY_ENABLED);
        }
        if (hoverStates.getOrDefault("bounds_enabled", false)) {
            toggleSetting(UIElement.BOUNDS_ENABLED);
        }
        if (hoverStates.getOrDefault("free_cam", false)) {
            toggleSetting(UIElement.FREE_CAM);
        }
        if (hoverStates.getOrDefault("interface_visible", false)) {
            toggleSetting(UIElement.INTERFACE_VISIBLE);
        }
        if (hoverStates.getOrDefault("simulation_paused", false)) {
            toggleSetting(UIElement.SIMULATION_PAUSED);
        }
    }

    private void toggleSetting(UIElement element) {
        boolean newValue = !uiStates.get(element);
        uiStates.put(element, newValue);
        eventManager.publish(new GUIStateChangedEvent(element, newValue));
    }


    public void render() {
        drawTickbox("velocity_arrows", "Show velocity arrows",
                uiStates.get(UIElement.VELOCITY_ARROWS),
                bottomInitX, bottomInitY);

        drawTickbox("sphere_names", "Show sphere names",
                uiStates.get(UIElement.SPHERE_NAMES),
                bottomInitX, bottomInitY - itemSpacing);

        drawTickbox("sphere_weights", "Show sphere weights",
                uiStates.get(UIElement.SPHERE_WEIGHTS),
                bottomInitX, bottomInitY - itemSpacing * 2);

        drawTickbox("sphere_trails", "Show sphere trails",
                uiStates.get(UIElement.SPHERE_TRAILS),
                bottomInitX, bottomInitY - itemSpacing * 3);

        drawTickbox("gravity_enabled", "Enable gravity",
                uiStates.get(UIElement.GRAVITY_ENABLED),
                bottomInitX, bottomInitY - itemSpacing * 4);

        drawTickbox("bounds_enabled", "Enable boundaries",
                uiStates.get(UIElement.BOUNDS_ENABLED),
                bottomInitX, bottomInitY - itemSpacing * 5);

    }


    /**
     * Draws a tickbox along with its label.
     * Normal color is white, active is yellow, disabled is gray.
     * <i>It's my tick in a box!</i>
     * @param elementId ID of the element.
     * @param elementLabel Label of the element.
     * @param active Whether the element is active.
     * @param xPosition X coordinate of the element.
     * @param yPosition Y coordinate of the element.
     */
    private void drawTickbox(String elementId, String elementLabel, boolean active, int xPosition, int yPosition) {
        boolean hovered = hoverStates.getOrDefault(elementId, false);

        if (hovered) {
            parent.fill(51);
        } else {
            parent.fill(0);
        }
        parent.rect(xPosition, yPosition, 20, 20);

        if (active) {
            parent.fill(255, 255, 0);
            parent.pushMatrix();
            parent.translate(0, 0, 1);
            parent.text("X", xPosition, yPosition + 20);
            parent.popMatrix();
        }

        if (hovered) {
            parent.fill(255);
        } else {
            parent.fill(200);
        }
        parent.text(elementLabel, xPosition + 30, yPosition + 20);
    }

    public boolean getDisplaySetting(UIElement element) {
        return uiStates.getOrDefault(element, false);
    }

    public boolean isFreeCamEnabled() {
        return uiStates.get(UIElement.FREE_CAM);
    }
}
