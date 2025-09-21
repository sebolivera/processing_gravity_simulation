package graphics;

import damkjer.ocd.Camera;
import events.core.EventManager;
import events.graphics.CameraChangedEvent;
import events.graphics.CameraCommandEvent;
import processing.core.PApplet;

public class CameraHandler {
    public static float CAM_PAN_STEP = 20;
    public static float CAM_DOLLY_STEP = 20;

    private final PApplet app;
    private final EventManager eventManager;
    private final Camera camera;

    public CameraHandler(PApplet app, EventManager eventManager) {
        this.app = app;
        this.eventManager = eventManager;
        this.camera = new Camera(
                app,
                app.width / 2.0f,
                app.height / 2.0f,
                1000.0f,
                app.width / 2.0f,
                app.height / 2.0f,
                0.0f
        );
        setupCameraEventHandler();
    }

    /**
     * Handle camera commands.
     * @param cameraCommandEvent The event.
     */
    private void onCommand(CameraCommandEvent cameraCommandEvent) {
        switch (cameraCommandEvent.op()) {
            case DOLLY -> camera.dolly(cameraCommandEvent.value());
            case TRUCK -> camera.truck(cameraCommandEvent.value());
            case PAN -> camera.pan(cameraCommandEvent.value());
            case TILT -> camera.tilt(cameraCommandEvent.value());
            case BOOM -> camera.boom(cameraCommandEvent.value());
            case ROLL -> camera.roll(cameraCommandEvent.value());
            case RESET -> resetCamera();
        }
        eventManager.publish(new CameraChangedEvent(camera));
    }

    /**
     * Initialize and reset camera to default position.
     * <i>AAAAND... ACTION!</i>
     */
    public void initializeCamera() {
        resetCamera();
    }

    /**
     * Reset camera to default position.
     * <i>Take 2.</i>
     */
    public void resetCamera() {
        camera.jump(app.width / 2f, app.height / 2f, app.height + 1000f);
        camera.aim(app.width / 2f, app.height / 2f, 0);
        eventManager.publish(new CameraChangedEvent(camera));
    }

    /**
     * Update the camera transformation.
     * <i>Rolling...</i>
     */
    public void update() {
        camera.feed();
    }

    /**
     * Set up the camera event handler.
     */
    private void setupCameraEventHandler() {
        eventManager.subscribe(CameraCommandEvent.class, this::onCommand);
    }
}
