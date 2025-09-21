package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import events.core.EventManager;
import graphics.gui.GUIHandler;
import graphics.CameraHandler;
import graphics.Renderer;
import input.InputHandler;
import model.SimulationHandler;
import processing.core.*;

import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {
    private static final Logger logger = LoggerFactory.getLogger(GravityCollisionApp.class);
    public static int FRAMES = 0;

    private SimulationHandler simulationHandler;
    private GUIHandler guiHandler;
    private InputHandler inputHandler;
    public CameraHandler cameraHandler;
    private Renderer renderer;


    @Override
    public void settings() {
        int WIDTH = 1000;
        int HEIGHT = 1000;
        String ENGINE = P3D;
        logger.info("Initializing application settings with size {}x{} with {} engine.", WIDTH, HEIGHT, ENGINE);
        size(WIDTH, HEIGHT, ENGINE);
    }

    @Override
    public void setup() {
        logger.info("Setting up app event handlers");
        try {
            EventManager eventManager = new EventManager();
            simulationHandler = new SimulationHandler(this, eventManager);

            cameraHandler = new CameraHandler(this, eventManager);
            cameraHandler.initializeCamera();
            guiHandler = new GUIHandler(eventManager, this);
            inputHandler = new InputHandler(this, eventManager, guiHandler);
            renderer = new Renderer(this, eventManager, simulationHandler, inputHandler, guiHandler);
            simulationHandler.initialize();

            logger.info("Setting up GUI");
            noCursor();
            initGUI();
        } catch (Exception e) {
            logger.error("Failed to setup application", e);
        }
        logger.info("Application setup completed successfully");
    }

    private void initGUI() {
        logger.debug("Initializing GUI components");
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
        FRAMES++;
        if (FRAMES % 3600 == 0) {
            logger.debug("Application running - Frame: {}, FPS: {}", FRAMES, frameRate);
        }
        } catch (Exception e) {
            logger.error("Error in draw loop at frame {}", FRAMES, e);
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
    public void mouseWheel(MouseEvent evt) {
        inputHandler.handleMouseWheel(evt);
    }

    @Override
    public void keyPressed() {
        inputHandler.handleKeyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        inputHandler.handleKeyReleased(key, keyCode);
    }


    public static void main(String[] args) {
        logger.info("Starting Gravity Collision Application");
        PApplet.main(GravityCollisionApp.class);
    }
}
