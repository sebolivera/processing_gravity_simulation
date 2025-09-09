package graphics;

import events.core.EventManager;
import events.physics.CameraChangedEvent;
import processing.core.PApplet;

public class CameraHandler {
    public static float CAM_PAN_STEP = 20;
    public static float CAM_DOLLY_STEP = 20;

    private final PApplet app;
    private final EventManager eventManager;
    private final CameraChangedEvent cameraEvent;

    public CameraHandler(PApplet app, EventManager eventManager) {
        this.app = app;
        this.eventManager = eventManager;
        this.cameraEvent = new CameraChangedEvent(app);
        setupCameraEventHandler();
    }

    /**
     * Initialize and reset camera to default position.
     * <i>AAAAND... ACTION!</i>
     */
    public void initializeCamera() {
        cameraEvent.ResetCameraEvent(app.width, app.height);
    }

    /**
     * Reset camera to default position.
     * <i>Take 2.</i>
     */
    public void resetCamera() {
        cameraEvent.ResetCameraEvent(app.width, app.height);
    }

    /**
     * Update the camera transformation.
     * <i>Rolling...</i>
     */
    public void update() {
        cameraEvent.FeedEvent();
    }

    /**
     * Set up the camera event handler.
     * <i>Cameraman... handler. Camera man-handler?</i>
     */
    private void setupCameraEventHandler() {
        eventManager.subscribe(CameraChangedEvent.class, event ->
                System.out.println("Camera updated via event system"));
    }

    /**
     * Get the current camera event for external use
     */
    public CameraChangedEvent getCameraEvent() {
        return cameraEvent;
    }
}