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
    private int frames;

    private SimulationHandler simulationHandler;
    private GUIHandler guiHandler;
    private InputHandler inputHandler;
    private CameraHandler cameraHandler;
    private Renderer renderer;

    @Override
    public void settings() {
        final int width = 1000;
        final int height = 1000;
        final String engine = P3D;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                    "Initializing application settings with size {}x{} with {} engine.",
                    width,
                    height,
                    engine);
        }
        size(width, height, engine);
    }

    @Override
    public void setup() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Setting up app event handlers");
        }
        try {
            final EventManager eventManager = new EventManager();
            simulationHandler = new SimulationHandler(this, eventManager);

            cameraHandler = new CameraHandler(this, eventManager);
            cameraHandler.initializeCamera();
            guiHandler = new GUIHandler(eventManager, this);
            inputHandler = new InputHandler(this, eventManager, guiHandler);
            renderer = new Renderer(this, eventManager, inputHandler, guiHandler);
            simulationHandler.initialize();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Setting up GUI");
            }
            noCursor();
            initGUI();
        } catch (Exception exc) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to setup application", exc);
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application setup completed successfully");
        }
    }

    private void initGUI() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initializing GUI components");
        }
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
            if (LOGGER.isDebugEnabled() && frames % 3600 == 0) {
                LOGGER.debug("Application running - Frame: {}, FPS: {}", frames, frameRate);
            }
        } catch (Exception exc) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error in draw loop at frame {}", frames, exc);
            }
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
    public void mouseWheel(final MouseEvent event) {
        inputHandler.handleMouseWheel(event);
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
    public int getFrames() {
        return frames;
    }

    public static void main(final String[] args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting Gravity Collision Application");
        }
        PApplet.main(GravityCollisionApp.class);
    }
}
