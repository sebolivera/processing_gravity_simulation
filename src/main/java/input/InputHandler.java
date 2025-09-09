package input;

import events.core.EventManager;
import events.gui.GUIStateChangedEvent;
import events.gui.GUIManager;
import events.physics.CameraChangedEvent;
import events.simulation.SimulationRestartEvent;
import graphics.CameraHandler;
import model.SimulationManager;
import processing.core.PApplet;
import processing.core.PSurface;
import processing.event.MouseEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final PApplet app;
    private final EventManager eventManager;
    private final GUIManager guiManager;
    private final CameraChangedEvent cameraEvent;

    private final Set<String> keysDown = new HashSet<>();
    private final Set<String> moveKeys = Set.of("z", "q", "s", "d", "w", "a");
    private boolean isShiftDown = false;
    private boolean isAzerty = false;

    private Robot robot;
    private int robotMoveBuffer = 0;
    private float lastCursorX;
    private float lastCursorY;
    public boolean firstMousePress = false;

    public InputHandler(PApplet app, EventManager eventManager, GUIManager guiManager, CameraHandler cameraHandler) {
        this.app = app;
        this.eventManager = eventManager;
        this.guiManager = guiManager;
        this.cameraEvent = cameraHandler.getCameraEvent();

        this.lastCursorX = app.mouseX;
        this.lastCursorY = app.mouseY;

        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Could not create Robot for mouse binding: " + e.getMessage());
        }
    }

    /**
     * Handle keyboard press events
     */
    public void handleKeyPressed(int key, int keyCode) {
        String keyStr = key == PApplet.CODED ?
                (keyCode == PApplet.SHIFT ? "SHIFT" : String.valueOf(keyCode)) :
                String.valueOf((char)key).toLowerCase();

        if (moveKeys.contains(keyStr)) {
            keysDown.add(keyStr);
        }

        if (keyCode == PApplet.SHIFT) {
            isShiftDown = true;
        }

        switch (keyStr) {
            case "l" -> {
                isAzerty = !isAzerty;
                System.out.println("Keyboard layout switched to: " + (isAzerty ? "AZERTY" : "QWERTY"));
            }
            case "f" -> eventManager.publish(new GUIStateChangedEvent(
                    GUIStateChangedEvent.UIElement.FREE_CAM,
                    !guiManager.isFreeCamEnabled()
            ));
            case "c" -> {
                cameraEvent.ResetCameraEvent(app.width, app.height);
                eventManager.publish(cameraEvent);
            }
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
            isShiftDown = false;
        }

        switch (keyCode) {
            case 82 -> { // 'R' key
                eventManager.publish(new SimulationRestartEvent(SimulationManager.DEFAULT_SPHERE_COUNT));
                System.out.println("Restart requested via InputHandler");
            }
            case 80 -> eventManager.publish(new GUIStateChangedEvent( // 'P' key'
                    GUIStateChangedEvent.UIElement.SIMULATION_PAUSED,
                    !guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.SIMULATION_PAUSED)
            ));
            case 72 -> eventManager.publish(new GUIStateChangedEvent( // 'H' key
                    GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE,
                    !guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)
            ));
        }
    }

    /**
     * Handle mouse press events.
     * <i>Hopefully not present in the hydraulic press channel.</i>
     */
    public void handleMousePressed(int mouseButton) {
        if (!guiManager.isFreeCamEnabled()) {
            guiManager.handleMouseClick();

            if (!firstMousePress) {
                firstMousePress = true;
            }
        } else {
            guiManager.handleMouseClick();
        }
    }

    /**
     * Handle mouse wheel events.
     * <i>The Wheel of Time is a terribly overrated book. There, I said it.</i>
     */
    public void handleMouseWheel(MouseEvent evt) {
        float dollyAmount = evt.getCount() * CameraHandler.CAM_DOLLY_STEP;
        cameraEvent.CameraDollyEvent(-dollyAmount);
        eventManager.publish(cameraEvent);
    }

    /**
     * Handle camera/object movement based on the current input state.
     * <i>You can't handle my moves 💋.</i>
     */
    public void handleMovement() {
        if (robotMoveBuffer > 0) {
            robotMoveBuffer--;
            return;
        }

        float yaw = PApplet.radians(app.mouseX - app.pmouseX) * 0.125f;
        float pitch = PApplet.radians(app.mouseY - app.pmouseY) * 0.125f;

        if (guiManager.isFreeCamEnabled()) {
            boolean cameraChanged = false;
            if (app.mousePressed && app.mouseButton == PApplet.RIGHT) {
                float truckAmount = -(app.mouseX - app.pmouseX);
                float boomAmount = (app.mouseY - app.pmouseY);

                if (truckAmount != 0) {
                    cameraEvent.CameraTruckEvent(truckAmount);
                    cameraChanged = true;
                }
                if (boomAmount != 0) {
                    cameraEvent.CameraBoomEvent(boomAmount);
                    cameraChanged = true;
                }
            } else if (yaw != 0 || pitch != 0) {
                cameraEvent.CameraPanEvent(yaw);
                cameraEvent.CameraTiltEvent(pitch);
                cameraEvent.CameraRollEvent(0);
                cameraChanged = true;
            }

            float speedMult = isShiftDown ? 4 : 1;

            for (String currentKey : keysDown) {
                switch (currentKey) {
                    case "z", "w" -> {
                        cameraEvent.CameraDollyEvent(-CameraHandler.CAM_DOLLY_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "s" -> {
                        cameraEvent.CameraDollyEvent(CameraHandler.CAM_DOLLY_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "q", "a" -> {
                        cameraEvent.CameraTruckEvent(-CameraHandler.CAM_PAN_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "d" -> {
                        cameraEvent.CameraTruckEvent(CameraHandler.CAM_PAN_STEP * speedMult);
                        cameraChanged = true;
                    }
                }
            }

            if (cameraChanged) {
                eventManager.publish(cameraEvent);
            }
        }
    }

    /**
     * Bind the mouse position to the area inside the window.
     * <i>Some kind of mousetrap, if you will.</i>
     */
    public void bindMousePositionInWindow(boolean isPaused) {
        if (robot == null || app.width <= 0 || app.height <= 0) return;
        if (isPaused) return;

        try {
            PSurface surface = app.getSurface();
            com.jogamp.newt.opengl.GLWindow glWindow = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
            if (!glWindow.hasFocus()) {
                return;
            }

            int windowScreenX = glWindow.getX();
            int windowScreenY = glWindow.getY();

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) return;

            int mouseScreenX = (int) pointerInfo.getLocation().getX();
            int mouseScreenY = (int) pointerInfo.getLocation().getY();

            int windowRight = windowScreenX + app.width;
            int windowBottom = windowScreenY + app.height;

            if (!guiManager.isFreeCamEnabled()) {
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
            System.err.println("Mouse centering error: " + e.getMessage());
        }
    }

    /**
     * Draw the mouse cursor
     */
    public void drawMouse() {
        app.strokeWeight(2);
        if (guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.FREE_CAM)) {
            app.stroke(128);
        } else {
            app.stroke(0, 0, 255);
        }
        if (!guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.FREE_CAM)) {
            lastCursorX = app.mouseX;
            lastCursorY = app.mouseY;
        }
        app.line(lastCursorX - 25, lastCursorY, lastCursorX + 25, lastCursorY);
        app.line(lastCursorX, lastCursorY - 25, lastCursorX, lastCursorY + 25);
        app.strokeWeight(1);
    }

    public boolean isFirstMousePress() {
        return firstMousePress;
    }

    public boolean isAzerty() {
        return isAzerty;
    }
}