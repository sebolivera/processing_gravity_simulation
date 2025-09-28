package graphics;

import app.GravityCollisionApp;
import damkjer.ocd.Camera;
import events.core.EventManager;
import events.graphics.CameraChangedEvent;
import events.graphics.CameraCommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntSupplier;

public class CameraHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraHandler.class);
    private static final float CAM_PAN_STEP = 20;
    private static final float CAM_DOLLY_STEP = 20;
    private final EventManager eventManager;
    private final Camera camera;
    private final IntSupplier width;
    private final IntSupplier height;

    public CameraHandler(final GravityCollisionApp appParam, final EventManager eventManagerParam) {
        this.eventManager = eventManagerParam;
        this.width = () -> appParam.width;
        this.height = () -> appParam.height;

        this.camera = new Camera(
                appParam,
                width.getAsInt() / 2.0f,
                height.getAsInt() / 2.0f,
                1000.0f,
                width.getAsInt() / 2.0f,
                height.getAsInt() / 2.0f,
                0.0f);
        setupCameraEventHandler();
    }

    /**
     * Handle camera commands.
     *
     * @param cameraCommandEvent The event.
     */
    private void onCommand(final CameraCommandEvent cameraCommandEvent) {
        switch (cameraCommandEvent.op()) {
            case DOLLY -> camera.dolly(cameraCommandEvent.value());
            case TRUCK -> camera.truck(cameraCommandEvent.value());
            case PAN -> camera.pan(cameraCommandEvent.value());
            case TILT -> camera.tilt(cameraCommandEvent.value());
            case BOOM -> camera.boom(cameraCommandEvent.value());
            case ROLL -> camera.roll(cameraCommandEvent.value());
            case RESET -> resetCamera();
            default -> {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unhandled camera command: " + cameraCommandEvent.op());
                }
            }
        }
        eventManager.publish(new CameraChangedEvent(camera));
    }

    /** Initialize and reset camera to default position. <i>AAAAND... ACTION!</i> */
    public void initializeCamera() {
        resetCamera();
    }

    /** Reset camera to default position. <i>Take 2.</i> */
    public final void resetCamera() {
        camera.jump(width.getAsInt() / 2f, height.getAsInt() / 2f, height.getAsInt() + 1000f);
        camera.aim(width.getAsInt() / 2f, height.getAsInt() / 2f, 0);
        eventManager.publish(new CameraChangedEvent(camera));
    }

    /** Update the camera transformation. <i>Rolling...</i> */
    public void update() {
        camera.feed();
    }

    /** Set up the camera event handler. */
    private void setupCameraEventHandler() {
        eventManager.subscribe(CameraCommandEvent.class, this::onCommand);
    }

    /**
     * Get the current camera pan step.
     *
     * @return The pan step.
     */
    public static float getCamPanStep() {
        return CAM_PAN_STEP;
    }

    /**
     * Get the current camera dolly step.
     *
     * @return The dolly step.
     */
    public static float getCamDollyStep() {
        return CAM_DOLLY_STEP;
    }
}
