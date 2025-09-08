package events.physics;

import damkjer.ocd.Camera;
import events.core.Event;
import processing.core.PApplet;

/**
 * Event indicating changes in the camera's state.
 */
public record CameraChangedEvent(Camera camera) implements Event {
    public CameraChangedEvent(PApplet pApplet) {
        this(new Camera(
                pApplet,
                pApplet.width / 2.0f,
                pApplet.height / 2.0f,
                1000.0f,
                pApplet.width / 2.0f,
                pApplet.height / 2.0f,
                0.0f
        ));
    }

    public void ResetCameraEvent(float width, float height) {
        camera.jump(width / 2, height / 2, height + 1000);
        camera.aim(width / 2, height / 2, 0);
    }

    public void CameraTruckEvent(float truckValue) {
        camera.truck(truckValue);
    }

    public void CameraBoomEvent(float boomValue) {
        camera.boom(boomValue);
    }

    public void CameraTiltEvent(float tiltValue) {
        camera.tilt(tiltValue);
    }

    public void CameraPanEvent(float panValue) {
        camera.pan(panValue);
    }

    public void CameraRollEvent(float rollValue) {
        camera.roll(rollValue);
    }

    public void CameraDollyEvent(float dollyValue) {
        camera.dolly(dollyValue);
    }

    public void FeedEvent() {
        camera.feed();
    }
}
