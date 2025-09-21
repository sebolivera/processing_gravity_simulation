package graphics;

import static events.graphics.CameraCommandEvent.Operation.BOOM;
import static events.graphics.CameraCommandEvent.Operation.DOLLY;
import static events.graphics.CameraCommandEvent.Operation.PAN;
import static events.graphics.CameraCommandEvent.Operation.TILT;
import static events.graphics.CameraCommandEvent.Operation.TRUCK;

import events.core.EventManager;
import events.graphics.CameraCommandEvent;
import events.graphics.gui.GUIStateChangedEvent;
import events.input.InputStateChangedEvent;
import events.input.MousePositionChangedEvent;
import events.input.MouseStateChangedEvent;
import graphics.gui.GUIHandler;
import input.InputHandler;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.util.Objects;
import model.SimulationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PSurface;

/**
 * Handles all rendering operations for the application. Centralizes drawing methods, font
 * management, and display-related functionality.
 */
public class Renderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);
    private final PApplet app;
    private final EventManager eventManager;
    private final GUIHandler guiHandler;
    private final InputHandler inputHandler;

    private boolean shiftHeld = false;
    private boolean mousePressed = false;
    private int mouseButton = 0;
    private float mouseX = 0;
    private float mouseY = 0;
    private float prevMouseX;
    private float prevMouseY;
    private float prevCrosshairX;
    private float prevCrosshairY;
    private boolean justSnapped = false;

    private final Robot robot;
    private int robotMoveBuffer = 0;

    public Renderer(
            final PApplet appParam,
            final EventManager eventManagerParam,
            final InputHandler inputHandlerParam,
            final GUIHandler guiHandlerParam)
            throws AWTException {
        this.app = appParam;
        this.eventManager = eventManagerParam;
        this.guiHandler = guiHandlerParam;
        this.inputHandler = inputHandlerParam;

        eventManagerParam.subscribe(InputStateChangedEvent.class, this::onInputEvent);
        eventManagerParam.subscribe(MousePositionChangedEvent.class, this::onMousePositionEvent);
        eventManagerParam.subscribe(MouseStateChangedEvent.class, this::onMouseStateEvent);
        eventManagerParam.subscribe(GUIStateChangedEvent.class, this::onGUIStateChangedEvent);
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            LOGGER.error("Could not create Robot for mouse binding: {}", e.getMessage());
            throw e;
        }
        prevCrosshairX = mouseX;
        prevCrosshairY = mouseY;
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }

    /**
     * Handle GUI state changes.
     *
     * @param e The event.
     */
    private void onGUIStateChangedEvent(final GUIStateChangedEvent e) {
        if (Objects.requireNonNull(e.element()) == GUIStateChangedEvent.UIElement.FREE_CAM) {
            if (!e.newState()) {
                snapMouseToCrosshair();
            }
        }
    }

    /** Snap the mouse to the crosshair. */
    private void snapMouseToCrosshair() {
        if (robot == null) {
            return;
        }

        try {
            PSurface surface = app.getSurface();
            com.jogamp.newt.opengl.GLWindow glWindow =
                    (com.jogamp.newt.opengl.GLWindow) surface.getNative();

            int windowScreenX = glWindow.getX();
            int windowScreenY = glWindow.getY();

            int targetScreenX = windowScreenX + (int) prevCrosshairX;
            int targetScreenY = windowScreenY + (int) prevCrosshairY;

            justSnapped = true;
            robotMoveBuffer = 5;
            robot.mouseMove(targetScreenX, targetScreenY);

            mouseX = prevCrosshairX;
            mouseY = prevCrosshairY;
            prevMouseX = prevCrosshairX;
            prevMouseY = prevCrosshairY;
        } catch (Exception e) {
            LOGGER.error("Error snapping mouse to crosshair: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Handle input state changes.
     *
     * @param e The event.
     */
    private void onInputEvent(final InputStateChangedEvent e) {
        if (Objects.requireNonNull(e.element())
                == InputStateChangedEvent.InputElement.SHIFT_KEY_DOWN) {
            shiftHeld = e.newState();
        }
    }

    /**
     * Handle mouse position changes.
     *
     * @param e The event.
     */
    private void onMousePositionEvent(final MousePositionChangedEvent e) {
        if (justSnapped) {
            justSnapped = false;
            return;
        }

        prevMouseX = mouseX;
        prevMouseY = mouseY;
        mouseX = e.x();
        mouseY = e.y();
    }

    /**
     * Handle mouse state changes.
     *
     * @param e The event.
     */
    private void onMouseStateEvent(final MouseStateChangedEvent e) {
        mousePressed = e.pressed();
        mouseButton = e.button();
    }

    /**
     * Draws the boundaries of the simulation, if enabled. <i>This is where I would draw the line.
     * IF I HAD ONE.</i>
     */
    public void drawBounds() {
        if (SimulationHandler.areBoundsEnabled()) {
            app.noFill();
            app.stroke(255);
            app.line(0, 0, 0, app.width, 0, 0);
            app.line(0, 0, 0, 0, app.height, 0);
            app.line(0, 0, 0, 0, 0, app.height);

            app.line(app.width, 0, 0, app.width, app.height, 0);
            app.line(app.width, 0, 0, app.width, 0, app.height);

            app.line(app.width, 0, app.height, 0, 0, app.height);
            app.line(app.width, 0, app.height, app.width, app.height, app.height);

            app.line(0, 0, app.height, app.width, 0, app.height);
            app.line(0, 0, app.height, 0, app.height, app.height);

            app.line(0, app.height, app.height, app.width, app.height, app.height);
            app.line(0, app.height, app.height, 0, app.height, 0);

            app.line(app.width, app.height, 0, app.width, app.height, app.height);
            app.line(app.width, app.height, 0, 0, app.height, 0);
        }
    }

    /** Draw the crosshair. <i>BOOM! Headshot!</i> */
    public void drawCrosshair() {
        if (!guiHandler.getDisplaySetting(GUIStateChangedEvent.UIElement.FREE_CAM)) {
            prevCrosshairX = mouseX;
            prevCrosshairY = mouseY;
        }
        app.strokeWeight(2);
        if (guiHandler.getDisplaySetting(GUIStateChangedEvent.UIElement.FREE_CAM)) {
            app.stroke(128);
        } else {
            app.stroke(0, 0, 255);
        }

        app.line(prevCrosshairX - 25, prevCrosshairY, prevCrosshairX + 25, prevCrosshairY);
        app.line(prevCrosshairX, prevCrosshairY - 25, prevCrosshairX, prevCrosshairY + 25);
        app.strokeWeight(1);
    }

    /**
     * Handle camera/object movement based on the current input state. <i>You can't handle my moves
     * 💋.</i>
     */
    public void handleMovement() {
        if (robotMoveBuffer > 0) {
            robotMoveBuffer--;
            return;
        }

        if (guiHandler.isFreeCamEnabled()) {
            float speedMult = shiftHeld ? 4 : 1;
            if (mousePressed && mouseButton == PApplet.RIGHT) {
                float truck = -(mouseX - prevMouseX);
                float boom = (mouseY - prevMouseY);
                if (truck != 0) {
                    eventManager.publish(new CameraCommandEvent(TRUCK, truck));
                }
                if (boom != 0) {
                    eventManager.publish(new CameraCommandEvent(BOOM, boom));
                }
            } else {
                float yaw = PApplet.radians(mouseX - prevMouseX) * 0.125f;
                float pitch = PApplet.radians(mouseY - prevMouseY) * 0.125f;
                if (yaw != 0) {
                    eventManager.publish(new CameraCommandEvent(PAN, yaw));
                }
                if (pitch != 0) {
                    eventManager.publish(new CameraCommandEvent(TILT, pitch));
                }
            }
            prevMouseX = mouseX;
            prevMouseY = mouseY;

            for (String k : inputHandler.getKeysDown()) {
                switch (k) {
                    case "z", "w" ->
                            eventManager.publish(
                                    new CameraCommandEvent(
                                            DOLLY, -CameraHandler.getCamDollyStep() * speedMult));
                    case "s" ->
                            eventManager.publish(
                                    new CameraCommandEvent(
                                            DOLLY, CameraHandler.getCamDollyStep() * speedMult));
                    case "q", "a" ->
                            eventManager.publish(
                                    new CameraCommandEvent(
                                            TRUCK, -CameraHandler.getCamPanStep() * speedMult));
                    case "d" ->
                            eventManager.publish(
                                    new CameraCommandEvent(
                                            TRUCK, CameraHandler.getCamPanStep() * speedMult));
                    default -> LOGGER.debug("Unhandled key: {}", k);
                }
            }
        }
    }

    /** Bind the mouse position to the area inside the window. <i>A mousetrap, if you will.</i> */
    public void bindMousePositionInWindow(final boolean isPaused) {
        if (robot == null || app.width <= 0 || app.height <= 0) {
            return;
        }
        if (isPaused) {
            return;
        }

        try {
            PSurface surface = app.getSurface();
            com.jogamp.newt.opengl.GLWindow glWindow =
                    (com.jogamp.newt.opengl.GLWindow) surface.getNative();
            if (!glWindow.hasFocus()) {
                return;
            }

            int windowScreenX = glWindow.getX();
            int windowScreenY = glWindow.getY();

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                return;
            }

            int mouseScreenX = (int) pointerInfo.getLocation().getX();
            int mouseScreenY = (int) pointerInfo.getLocation().getY();

            int windowRight = windowScreenX + app.width;
            int windowBottom = windowScreenY + app.height;

            if (!guiHandler.isFreeCamEnabled()) {
                if (mouseScreenX < windowScreenX + 2) {
                    robot.mouseMove(windowScreenX + 1, mouseScreenY);
                }
                if (mouseScreenX > windowRight - 3) {
                    robot.mouseMove(windowRight - 3, mouseScreenY);
                }
                if (mouseScreenY < windowScreenY + 2) {
                    robot.mouseMove(mouseScreenX, windowScreenY + 2);
                }
                if (mouseScreenY > windowBottom - 3) {
                    robot.mouseMove(mouseScreenX, windowBottom - 3);
                }
            } else {
                if (mouseScreenX < windowScreenX + 1) {
                    robot.mouseMove(windowRight - 2, mouseScreenY);
                    robotMoveBuffer = 3;
                }
                if (mouseScreenX > windowRight - 2) {
                    robot.mouseMove(windowScreenX + 1, mouseScreenY);
                    robotMoveBuffer = 3;
                }
                if (mouseScreenY < windowScreenY + 1) {
                    robot.mouseMove(mouseScreenX, windowBottom - 2);
                    robotMoveBuffer = 3;
                }
                if (mouseScreenY > windowBottom - 2) {
                    robot.mouseMove(mouseScreenX, windowScreenY + 1);
                    robotMoveBuffer = 3;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Mouse centering error: {}", e.getMessage());
            throw e;
        }
    }
}
