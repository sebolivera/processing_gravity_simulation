package app;

import events.core.EventManager;
import events.gui.DisplaySettingsChangedEvent;
import events.gui.GUIManager;
import events.gui.GUIStateChangedEvent;
import graphics.CameraHandler;
import input.InputHandler;
import events.physics.GravityChangedEvent;
import events.physics.SpeedChangedEvent;
import gui.HScrollBar;
import misc.MathUtils;
import model.SimulationManager;
import processing.core.*;

import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {
    public static int FRAMES = 0;
    public static int DEFAULT_FONT_SIZE = 28;

    private PFont fontLight;
    private PFont fontBold;
    private long unpausedTimer = 0;

    private EventManager eventManager;
    private SimulationManager simulationManager;
    private HScrollBar gravityScroll;
    private HScrollBar speedScroll;
    private GUIManager guiManager;
    private InputHandler inputHandler;
    public CameraHandler cameraHandler;


    @Override
    public void settings() {
        size(1000, 1000, P3D);
    }

    @Override
    public void setup() {
        eventManager = new EventManager();
        simulationManager = new SimulationManager(this, eventManager);
        setupEventHandlers();
        fontLight = createFont("Roboto-Light.ttf", DEFAULT_FONT_SIZE);
        fontBold = createFont("Roboto-Black.ttf", DEFAULT_FONT_SIZE);

        simulationManager.initialize();
        noCursor();
        guiManager = new GUIManager(eventManager, this);
        cameraHandler = new CameraHandler(this, eventManager);
        inputHandler = new InputHandler(this, eventManager, guiManager, cameraHandler);
        initGUI();
    }

    private void initGUI() {
        int bottomInitX = 50;
        int bottomInitY = height - 50;

        MathUtils.FloatFunction editGLambda = this::setGravityConstant;
        MathUtils.FloatFunction editPhysicsFPS = this::setGlobalSpeed;

        gravityScroll = new HScrollBar(
                bottomInitX,
                bottomInitY - 330,
                width / 3,
                16,
                0,
                20,
                "Global gravity scale",
                0.5f,
                editGLambda,
                true,
                "0",
                "2",
                this,
                eventManager,
                guiManager,
                "gravity_scroll"
        );

        speedScroll = new HScrollBar(
                bottomInitX,
                bottomInitY - 420,
                width / 3,
                16,
                1f,
                3000.0f,
                "Simulation speed multiplier",
                0.01f,
                editPhysicsFPS,
                true,
                "1/60x",
                "50x",
                this,
                eventManager,
                guiManager,
                "speed_scroll",
                true,
                30.0f
        );
    }

    /**
     * Set the gravity constant.
     * <i>You now grasp the gravity of the matter.</i>
     */
    private void setGravityConstant(float newGravity) {
        eventManager.publish(new GravityChangedEvent(newGravity));
    }

    /**
     * Set the global speed.
     * <i>What James Wilson's probably on.</i>
     */
    private void setGlobalSpeed(float newSpeed) {
        eventManager.publish(new SpeedChangedEvent((int) newSpeed));
    }

    /**
     * Updates the UI.
     * <i>Draw me like one of your French GUIs.</i>
     */
    private void drawGUI() {
        drawHints();
        inputHandler.drawMouse();
        if (gravityScroll != null) {
            gravityScroll.update();
            gravityScroll.display();

        }
        if (speedScroll != null) {
            speedScroll.update();
            speedScroll.display();
        }

        if (guiManager != null) {
            guiManager.updateHoverStates();
            guiManager.render();
        }
    }

    /**
     * Draw hints and UI overlays.
     * <i>This really tipped me off.</i>
     */
    void drawHints() {
        if (!guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)) {
            return;
        }

        textFont(fontLight);
        fill(255, 255, 0);

        if (guiManager.isFreeCamEnabled()) {
            text("Use wasd/zqsd to move around.", width - 575, height - 330);
            text("Use right-click to move the camera laterally.", width - 575, height - 280);
        }

        text("Press 'f' to toggle freecam", width - 575, height - 230);
        text("Press 'c' to reset camera position.", width - 575, height - 180);
        text("Press 'h' to hide the interface.", width - 575, height - 130);
        text("Press 'r' to restart the simulation.", width - 575, height - 30);

        if (guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.SIMULATION_PAUSED)) {
            textFont(fontLight);
            text("Press 'p' to unpause the simulation.", width - 575, height - 80);
            textFont(fontBold);
            noStroke();
            fill(255, 0, 0);
            rect(35, 68, 10, 30);
            rect(50, 68, 10, 30);
            text("PAUSED", 75, 100);
        } else {
            textFont(fontLight);
            text("Press 'p' to pause the simulation.", width - 575, height - 80);
            textFont(fontBold);
            noStroke();
            if (millis() - unpausedTimer < 2000) {
                fill(0, lerp(255, 0, (float) (millis() - unpausedTimer) / 2000), 0);
                triangle(35, 70, 35, 96, 65, 83);
                text("RUNNING", 75, 100);
            }
        }
    }


    /**
     * Handle hovering effects.
     * <i>It's hover Hanakin, I ave the igh ground!</i>
     */
    private void hover() {
        if (guiManager != null) {
            guiManager.updateHoverStates();
        }
    }


    /**
     * Draws the boundaries of the simulation, if enabled.
     * <i>This is where I would draw the line. IF I HAD ONE.</i>
     */
    void drawBounds() {
        if (simulationManager.areBoundsEnabled()) {
            noFill();
            stroke(255);
            line(0, 0, 0, width, 0, 0);
            line(0, 0, 0, 0, height, 0);
            line(0, 0, 0, 0, 0, height);

            line(width, 0, 0, width, height, 0);
            line(width, 0, 0, width, 0, height);

            line(width, 0, height, 0, 0, height);
            line(width, 0, height, width, height, height);

            line(0, 0, height, width, 0, height);
            line(0, 0, height, 0, height, height);


            line(0, height, height, width, height, height);
            line(0, height, height, 0, height, 0);

            line(width, height, 0, width, height, height);
            line(width, height, 0, 0, height, 0);
        }
    }

    @Override
    public void draw() {
        background(0);
        inputHandler.bindMousePositionInWindow(simulationManager.isPaused());
        cameraHandler.update();
        lights();
        hover();
        drawBounds();

        simulationManager.update();
        simulationManager.render();

        drawGUI();
        inputHandler.handleMovement();
        FRAMES++;
    }

    @Override
    public void mousePressed() {
        inputHandler.handleMousePressed(mouseButton);
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

    /**
     * Set up event handlers for various events.
     * <i>The party planning committee.</i>
     */
    private void setupEventHandlers() {
        eventManager.subscribe(DisplaySettingsChangedEvent.class, event -> {
            switch (event.getSetting()) {
                case VELOCITY_ARROWS -> SimulationManager.drawArrows = event.isEnabled();
                case SPHERE_NAMES -> SimulationManager.drawNames = event.isEnabled();
                case SPHERE_WEIGHTS -> SimulationManager.drawWeights = event.isEnabled();
                case SPHERE_TRAILS -> SimulationManager.drawTrails = event.isEnabled();
                case BOUNDS_VISIBLE -> SimulationManager.boundsEnabled = event.isEnabled();
            }
            System.out.println("Display setting " + event.getSetting() + " changed to: " + event.isEnabled());
        });

        eventManager.subscribe(GUIStateChangedEvent.class, event -> {
            if (event.element() == GUIStateChangedEvent.UIElement.SIMULATION_PAUSED) {
                if (!event.newState()) {
                    unpausedTimer = millis();
                }
            }
            switch (event.element()) {
                case FREE_CAM -> System.out.println("Free cam " + (event.newState() ? "enabled" : "disabled"));
                case INTERFACE_VISIBLE -> System.out.println("Interface " + (event.newState() ? "visible" : "hidden"));
            }
        });
    }

    public static void main(String[] args) {
        PApplet.main(GravityCollisionApp.class);
    }
}
