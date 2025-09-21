package app;

import events.core.EventManager;
import graphics.gui.GUIHandler;
import graphics.CameraHandler;
import graphics.Renderer;
import input.InputHandler;
import model.SimulationHandler;
import processing.core.*;

import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {
    public static int FRAMES = 0;

    private SimulationHandler simulationHandler;
    private GUIHandler guiHandler;
    private InputHandler inputHandler;
    public CameraHandler cameraHandler;
    private Renderer renderer;



    @Override
    public void settings() {
        size(1000, 1000, P3D);
    }

    @Override
    public void setup() {
        EventManager eventManager = new EventManager();
        simulationHandler = new SimulationHandler(this, eventManager);

        simulationHandler.initialize();
        noCursor();
        cameraHandler = new CameraHandler(this, eventManager);
        cameraHandler.initializeCamera();
        guiHandler = new GUIHandler(eventManager, this);
        inputHandler = new InputHandler(this, eventManager, guiHandler);
        renderer = new Renderer(this, eventManager, simulationHandler, inputHandler, guiHandler);
        initGUI();
    }

    private void initGUI() {
        guiHandler.setupSliders(width, height);
    }

    @Override
    public void draw() {
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
        PApplet.main(GravityCollisionApp.class);
    }
}
