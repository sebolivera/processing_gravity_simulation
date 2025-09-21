package input;

import events.core.EventManager;
import events.graphics.CameraCommandEvent;
import events.graphics.gui.GUIStateChangedEvent;
import events.input.InputStateChangedEvent;
import events.input.MousePositionChangedEvent;
import events.input.MouseStateChangedEvent;
import graphics.gui.GUIHandler;
import events.simulation.SimulationRestartEvent;
import graphics.CameraHandler;
import model.SimulationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private static final Logger logger = LoggerFactory.getLogger(InputHandler.class);
    private final EventManager eventManager;
    private final GUIHandler guiHandler;

    private final Set<String> keysDown = new HashSet<>();
    private final Set<String> moveKeys = Set.of("z", "q", "s", "d", "w", "a");
    private boolean isAzerty = false;
    private float lastMouseX;
    private float lastMouseY;
    private final PApplet app;


    public boolean firstMousePress = false;

    public InputHandler(PApplet app, EventManager eventManager, GUIHandler guiHandler) {
        this.eventManager = eventManager;
        this.guiHandler = guiHandler;

        this.lastMouseX = app.mouseX;
        this.lastMouseY = app.mouseY;
        this.app = app;
    }

    /**
     * Update the mouse position and publish events if changed.
     * <i>Monitoring mice isn't something I expected to be doing in my future.</i>
     */
    public void updateMousePosition() {
        if (app.mouseX != lastMouseX || app.mouseY != lastMouseY) {
            eventManager.publish(new MousePositionChangedEvent(
                    app.mouseX, app.mouseY, lastMouseX, lastMouseY));
            lastMouseX = app.mouseX;
            lastMouseY = app.mouseY;
        }
    }

    /**
     * Handle keyboard press events.
     * <i>Stop pressing my buttons.</i>
     */
    public void handleKeyPressed(int key, int keyCode) {
        logger.debug("Key '{}' with keycode {} pressed", key, keyCode);
        String keyStr = key == PApplet.CODED ?
                (keyCode == PApplet.SHIFT ? "SHIFT" : String.valueOf(keyCode)) :
                String.valueOf((char)key).toLowerCase();

        if (moveKeys.contains(keyStr)) {
            keysDown.add(keyStr);
        }

        if (keyCode == PApplet.SHIFT) {
            eventManager.publish(new InputStateChangedEvent(
                    InputStateChangedEvent.InputElement.SHIFT_KEY_DOWN,
                    true));
        }

        switch (keyStr) {
            case "l" -> {
                isAzerty = !isAzerty;
                logger.info("Keyboard layout switched to: {}", (isAzerty ? "AZERTY" : "QWERTY"));
            }
            case "f" -> eventManager.publish(new GUIStateChangedEvent(
                    GUIStateChangedEvent.UIElement.FREE_CAM,
                    !guiHandler.isFreeCamEnabled()
            ));
            case "c" -> eventManager.publish(new CameraCommandEvent(CameraCommandEvent.Operation.RESET, 0));
        }
    }

    /**
     * Handle keyboard release events
     */
    public void handleKeyReleased(int key, int keyCode) {
        String keyStr = key == PApplet.CODED ?
                (keyCode == PApplet.SHIFT ? "SHIFT" : String.valueOf(keyCode)) :
                String.valueOf((char)key).toLowerCase();

        if (moveKeys.contains(keyStr)) {
            keysDown.remove(keyStr);
        }

        if (keyCode == PApplet.SHIFT) {
            eventManager.publish(new InputStateChangedEvent(
                    InputStateChangedEvent.InputElement.SHIFT_KEY_DOWN,
                    false));
        }

        switch (keyCode) {
            case 82 -> { // 'R' key
                eventManager.publish(new SimulationRestartEvent(SimulationHandler.DEFAULT_SPHERE_COUNT));
                logger.info("Restart requested via InputHandler");
            }
            case 80 -> {
                eventManager.publish(new GUIStateChangedEvent( // 'P' key'
                        GUIStateChangedEvent.UIElement.SIMULATION_PAUSED,
                        !guiHandler.getDisplaySetting(GUIStateChangedEvent.UIElement.SIMULATION_PAUSED)
                ));
                logger.info("Pause requested via InputHandler");
            }
            case 72 -> {
                eventManager.publish(new GUIStateChangedEvent( // 'H' key
                        GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE,
                        !guiHandler.getDisplaySetting(GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)
                ));
                logger.info("Interface hiding requested via InputHandler");
            }
        }
    }

    /**
     * Handle mouse press events.
     * <i>Hopefully not present in the hydraulic press channel.</i>
     */
    public void handleMousePressed() {
        eventManager.publish(new MouseStateChangedEvent(true, app.mouseButton));

        if (!guiHandler.isFreeCamEnabled()) {
            guiHandler.handleMouseClick();

            if (!firstMousePress) {
                firstMousePress = true;
            }
        } else {
            guiHandler.handleMouseClick();
        }
    }


    /**
     * Handle mouse release events.
     */
    public void handleMouseReleased() {
        eventManager.publish(new MouseStateChangedEvent(false, 0));
    }


    /**
     * Handle mouse wheel events.
     * <i>The Wheel of Time is a terribly overrated book. There, I said it.</i>
     */
    public void handleMouseWheel(MouseEvent evt) {
        float dollyAmount = evt.getCount() * CameraHandler.CAM_DOLLY_STEP;
        eventManager.publish(new CameraCommandEvent(CameraCommandEvent.Operation.DOLLY, -dollyAmount));
    }

    /**
     * Get the set of keys currently pressed.
     * <i>Get down!</i>
     * @return The set of keys currently pressed.
     */
    public Set<String> getKeysDown() {
        return keysDown;
    }
}