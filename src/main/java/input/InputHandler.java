package input;

import events.core.EventManager;
import events.graphics.CameraCommandEvent;
import events.graphics.gui.GUIStateChangedEvent;
import events.input.InputStateChangedEvent;
import events.input.MousePositionChangedEvent;
import events.input.MouseStateChangedEvent;
import events.simulation.SimulationRestartEvent;
import graphics.CameraHandler;
import graphics.gui.GUIHandler;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import model.SimulationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class InputHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputHandler.class);
    private final EventManager eventManager;
    private final GUIHandler guiHandler;

    private final Set<String> keysDown = new HashSet<>();
    private final Set<String> moveKeys = Set.of("z", "q", "s", "d", "w", "a");
    private boolean isAzerty;
    private float lastMouseX;
    private float lastMouseY;
    private final PApplet app;
    private boolean firstMousePress;

    public InputHandler(
            final PApplet appParam,
            final EventManager eventManagerParam,
            final GUIHandler guiHandlerParam) {
        this.eventManager = eventManagerParam;
        this.guiHandler = guiHandlerParam;
        this.lastMouseX = appParam.mouseX;
        this.lastMouseY = appParam.mouseY;
        this.app = appParam;
        this.isAzerty = true;
        this.firstMousePress = false;
    }

    /**
     * Update the mouse position and publish events if changed. <i>Monitoring mice isn't something I
     * expected to be doing in my future.</i>
     */
    public void updateMousePosition() {
        if (app.mouseX != lastMouseX || app.mouseY != lastMouseY) {
            eventManager.publish(
                    new MousePositionChangedEvent(app.mouseX, app.mouseY, lastMouseX, lastMouseY));
            lastMouseX = app.mouseX;
            lastMouseY = app.mouseY;
        }
    }

    /** Handle keyboard press events. <i>Stop pressing my buttons.</i> */
    public void handleKeyPressed(final int key, final int keyCode) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Key '{}' with keycode {} pressed", key, keyCode);
        }
        final String keyStr =
                key == PApplet.CODED
                        ? (keyCode == PApplet.SHIFT ? "SHIFT" : String.valueOf(keyCode))
                        : String.valueOf((char) key).toLowerCase(Locale.ROOT);

        if (moveKeys.contains(keyStr)) {
            keysDown.add(keyStr);
        }

        if (keyCode == PApplet.SHIFT) {
            eventManager.publish(
                    new InputStateChangedEvent(
                            InputStateChangedEvent.InputElement.SHIFT_KEY_DOWN, true));
        }

        switch (keyStr) {
            case "l" -> {
                isAzerty = !isAzerty;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Keyboard layout switched to: {}", isAzerty ? "AZERTY" : "QWERTY");
                }
            }
            case "f" ->
                    eventManager.publish(
                            new GUIStateChangedEvent(
                                    GUIStateChangedEvent.UIElement.FREE_CAM,
                                    !guiHandler.isFreeCamEnabled()));
            case "c" ->
                    eventManager.publish(
                            new CameraCommandEvent(CameraCommandEvent.Operation.RESET, 0));
            default -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Unknown key: {}", keyStr);
                }
            }
        }
    }

    /** Handle keyboard release events */
    public void handleKeyReleased(final int key, final int keyCode) {
        final String keyStr =
                key == PApplet.CODED
                        ? (keyCode == PApplet.SHIFT ? "SHIFT" : String.valueOf(keyCode))
                        : String.valueOf((char) key).toLowerCase(Locale.ROOT);

        if (moveKeys.contains(keyStr)) {
            keysDown.remove(keyStr);
        }

        if (keyCode == PApplet.SHIFT) {
            eventManager.publish(
                    new InputStateChangedEvent(
                            InputStateChangedEvent.InputElement.SHIFT_KEY_DOWN, false));
        }

        switch (keyCode) {
            case 82 -> { // 'R' key
                eventManager.publish(
                        new SimulationRestartEvent(SimulationHandler.DEFAULT_SPHERE_COUNT));

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Restart requested via InputHandler");
                }
            }
            case 80 -> { // 'P' key'
                eventManager.publish(
                        new GUIStateChangedEvent(
                                GUIStateChangedEvent.UIElement.SIMULATION_PAUSED,
                                !guiHandler.getDisplaySetting(
                                        GUIStateChangedEvent.UIElement.SIMULATION_PAUSED)));

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Pause requested via InputHandler");
                }
            }
            case 72 -> { // 'H' key
                eventManager.publish(
                        new GUIStateChangedEvent(
                                GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE,
                                !guiHandler.getDisplaySetting(
                                        GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)));

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Interface hiding requested via InputHandler");
                }
            }
            default -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Unhandled keycode: {}", key);
                }
            }
        }
    }

    /** Handle mouse press events. <i>Hopefully not present in the hydraulic press channel.</i> */
    public void handleMousePressed() {
        eventManager.publish(new MouseStateChangedEvent(true, app.mouseButton));
        guiHandler.handleMouseClick();
        if (!guiHandler.isFreeCamEnabled() && !firstMousePress) {
            firstMousePress = true;
        }
    }

    /** Handle mouse release events. <i>Release the mice!</i> */
    public void handleMouseReleased() {
        eventManager.publish(new MouseStateChangedEvent(false, 0));
    }

    /**
     * Handle mouse wheel events. <i>The Wheel of Time is a terribly overrated book. There, I said
     * it.</i>
     */
    public void handleMouseWheel(final MouseEvent evt) {
        final float dollyAmount = evt.getCount() * CameraHandler.getCamDollyStep();
        eventManager.publish(
                new CameraCommandEvent(CameraCommandEvent.Operation.DOLLY, -dollyAmount));
    }

    /**
     * Get the set of keys currently pressed. <i>Get down!</i>
     *
     * @return The set of keys currently pressed.
     */
    public Set<String> getKeysDown() {
        return keysDown;
    }
}
