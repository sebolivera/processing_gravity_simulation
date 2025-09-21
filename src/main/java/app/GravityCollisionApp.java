package app;

import events.core.EventManager;
import graphics.CameraHandler;
import graphics.Renderer;
import graphics.gui.GUIHandler;
import input.InputHandler;
import model.SimulationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GravityCollisionApp.class);
    private static int frames = 0;

    private SimulationHandler simulationHandler;
    private GUIHandler guiHandler;
    private InputHandler inputHandler;
    private CameraHandler cameraHandler;
    private Renderer renderer;

    @Override
    public void settings() {
        int width = 1000;
        int height = 1000;
        String engine = P3D;
        LOGGER.info(
                "Initializing application settings with size {}x{} with {} engine.",
                width,
                height,
                engine);
        size(width, height, engine);
    }

    @Override
    public void setup() {
        LOGGER.info("Setting up app event handlers");
        try {
            EventManager eventManager = new EventManager();
            simulationHandler = new SimulationHandler(this, eventManager);

            cameraHandler = new CameraHandler(this, eventManager);
            cameraHandler.initializeCamera();
            guiHandler = new GUIHandler(eventManager, this);
            inputHandler = new InputHandler(this, eventManager, guiHandler);
            renderer = new Renderer(this, eventManager, inputHandler, guiHandler);
            simulationHandler.initialize();

            LOGGER.info("Setting up GUI");
            noCursor();
            initGUI();
        } catch (Exception e) {
            LOGGER.error("Failed to setup application", e);
        }
        LOGGER.info("Application setup completed successfully");
    }

    private void initGUI() {
        LOGGER.debug("Initializing GUI components");
        guiHandler.setupSliders(width, height);
    }

    @Override
    public void draw() {
        try {
            background(0);
            inputHandler.updateMousePosition();
            renderer.bindMousePositionInWindow(simulationHandler.isPaused());
            cameraHandler.update();
            lights();
            guiHandler.hover();
            guiHandler.render();
            renderer.drawBounds();
            renderer.drawCrosshair();

            simulationHandler.update();
            simulationHandler.renderSpheres();

            guiHandler.drawGUI();
            renderer.handleMovement();
            frames++;
            if (frames % 3600 == 0) {
                LOGGER.debug("Application running - Frame: {}, FPS: {}", frames, frameRate);
            }
        } catch (Exception e) {
            LOGGER.error("Error in draw loop at frame {}", frames, e);
        }
    }

    @Override
    public void mousePressed() {
        inputHandler.handleMousePressed();
    }

    @Override
    public void mouseReleased() {
        inputHandler.handleMouseReleased();
    }

    @Override
    public void mouseWheel(final MouseEvent e) {
        inputHandler.handleMouseWheel(e);
    }

    @Override
    public void keyPressed() {
        inputHandler.handleKeyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        inputHandler.handleKeyReleased(key, keyCode);
    }

    /**
     * Get the current frame count.
     *
     * @return The frame count.
     */
    public static int getFrames() {
        return frames;
    }

    public static void main(final String[] args) {
        LOGGER.info("Starting Gravity Collision Application");
        PApplet.main(GravityCollisionApp.class);
    }
}
