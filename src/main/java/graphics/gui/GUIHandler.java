package graphics.gui;

import events.core.EventManager;
import events.graphics.gui.GUIHoverEvent;
import events.graphics.gui.GUIStateChangedEvent;
import events.input.MousePositionChangedEvent;
import events.physics.GravityChangedEvent;
import events.physics.SimulationPausedEvent;
import events.physics.SpeedChangedEvent;
import misc.MathUtils;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static events.graphics.gui.GUIStateChangedEvent.UIElement;

public class GUIHandler {
    public static final int DEFAULT_FONT_SIZE = 28;

    private final EventManager eventManager;
    private final PApplet app;

    private final Map<UIElement, Boolean> uiStates = new EnumMap<>(UIElement.class);
    private final Map<String, Boolean> hoverStates = new HashMap<>();

    private final int bottomInitX = 50;
    private final int bottomInitY;
    private final int itemHeight = 20;
    private final int itemSpacing = 50;

    private HScrollBar gravityScroll;
    private HScrollBar speedScroll;
    private long unpausedTimer = 0;
    private float cursorX, cursorY;

    private PFont fontLight;
    private PFont fontBold;


    public GUIHandler(EventManager eventManager, PApplet app) {
        this.eventManager = eventManager;
        this.app = app;
        this.bottomInitY = app.height - 50;

        eventManager.subscribe(MousePositionChangedEvent.class, e -> {
            cursorX = e.x();
            cursorY = e.y();
        });
        initializeDefaultState();
        setupEventHandlers();
        initializeFonts();
    }
    public float getCursorX() { return cursorX; }
    public float getCursorY() { return cursorY; }

    /**
     * Initializes the fonts used in the application.
     */
    private void initializeFonts() {
        fontLight = app.createFont("Roboto-Light.ttf", DEFAULT_FONT_SIZE);
        fontBold = app.createFont("Roboto-Black.ttf", DEFAULT_FONT_SIZE);
    }

    /**
     * Initializes the default GUI state.
     */
    private void initializeDefaultState() {
        uiStates.put(UIElement.VELOCITY_ARROWS, false);
        uiStates.put(UIElement.SPHERE_NAMES, false);
        uiStates.put(UIElement.SPHERE_WEIGHTS, false);
        uiStates.put(UIElement.SPHERE_TRAILS, false);
        uiStates.put(UIElement.GRAVITY_ENABLED, true);
        uiStates.put(UIElement.BOUNDS_ENABLED, true);
        uiStates.put(UIElement.FREE_CAM, false);
        uiStates.put(UIElement.INTERFACE_VISIBLE, true);
        uiStates.put(UIElement.SIMULATION_PAUSED, false);
    }

    /**
     * Sets up the event handlers for the GUI.
     */
    private void setupEventHandlers() {
        eventManager.subscribe(GUIStateChangedEvent.class, this::handleGUIStateChanges);
        eventManager.subscribe(GUIHoverEvent.class, this::handleHover);
        eventManager.subscribe(SimulationPausedEvent.class, this::handleSimulationPaused);
    }

    /**
     * Handles the simulation paused event (for the "Paused" and "Running" text indicators).
     * @param event The event.
     */
    private void handleSimulationPaused(SimulationPausedEvent event) {
        if (!event.paused()) {
            setUnpausedTimer(app.millis());
        }
    }

    /**
     * Handles hover events triggered in the GUI.
     * @param event the GUI hover event.
     */
    private void handleHover(GUIHoverEvent event) {
        hoverStates.put(event.getElementId(), event.isHovered());
    }

    /**
     * Handles GUI state change events.
     * @param event The event.
     */
    private void handleGUIStateChanges(GUIStateChangedEvent event) {
        boolean previousState = uiStates.get(event.element());

        uiStates.put(event.element(), event.newState());
        if (event.element() == UIElement.SIMULATION_PAUSED &&
                previousState && !event.newState()) {
            setUnpausedTimer(app.millis());
        }
    }

    /**
     * Updates the hover states of the GUI elements.
     * <i>I'm over it.</i>
     */
    public void updateHoverStates() {
        updateHoverState("velocity_arrows", bottomInitX, bottomInitY, 320, itemHeight);
        updateHoverState("sphere_names", bottomInitX, bottomInitY - itemSpacing, 295, itemHeight);
        updateHoverState("sphere_weights", bottomInitX, bottomInitY - itemSpacing * 2, 270, itemHeight);
        updateHoverState("sphere_trails", bottomInitX, bottomInitY - itemSpacing * 3, 220, itemHeight);
        updateHoverState("gravity_enabled", bottomInitX, bottomInitY - itemSpacing * 4, 280, itemHeight);
        updateHoverState("bounds_enabled", bottomInitX, bottomInitY - itemSpacing * 5, 240, itemHeight);
    }

    /**
     * Updates the hover state of a single GUI element.
     * @param elementId ID of the element.
     * @param x X coordinate of the element.
     * @param y Y coordinate of the element.
     * @param width Width of the element.
     * @param height Height of the element.
     */
    private void updateHoverState(String elementId, int x, int y, int width, int height) {
        boolean isHovered = app.mouseX >= x && app.mouseX <= x + width &&
                app.mouseY >= y && app.mouseY <= y + height;

        boolean wasHovered = hoverStates.getOrDefault(elementId, false);
        if (isHovered != wasHovered) {
            hoverStates.put(elementId, isHovered);
            eventManager.publish(new GUIHoverEvent(elementId, isHovered, app.mouseX, app.mouseY));
        }
    }

    /**
     * Handles mouse clicks.
     * <i>It's more of a squeak, really.</i>
     */
    public void handleMouseClick() {
        if (hoverStates.getOrDefault("velocity_arrows", false)) {
            toggleSetting(UIElement.VELOCITY_ARROWS);
        }
        if (hoverStates.getOrDefault("sphere_names", false)) {
            toggleSetting(UIElement.SPHERE_NAMES);
        }
        if (hoverStates.getOrDefault("sphere_weights", false)) {
            toggleSetting(UIElement.SPHERE_WEIGHTS);
        }
        if (hoverStates.getOrDefault("sphere_trails", false)) {
            toggleSetting(UIElement.SPHERE_TRAILS);
        }
        if (hoverStates.getOrDefault("gravity_enabled", false)) {
            toggleSetting(UIElement.GRAVITY_ENABLED);
        }
        if (hoverStates.getOrDefault("bounds_enabled", false)) {
            toggleSetting(UIElement.BOUNDS_ENABLED);
        }
        if (hoverStates.getOrDefault("free_cam", false)) {
            toggleSetting(UIElement.FREE_CAM);
        }
        if (hoverStates.getOrDefault("interface_visible", false)) {
            toggleSetting(UIElement.INTERFACE_VISIBLE);
        }
        if (hoverStates.getOrDefault("simulation_paused", false)) {
            toggleSetting(UIElement.SIMULATION_PAUSED);
        }
    }

    /**
     * Toggles the specified UI element.
     * @param element The element to toggle.
     */
    private void toggleSetting(UIElement element) {
        boolean newValue = !uiStates.get(element);
        uiStates.put(element, newValue);
        eventManager.publish(new GUIStateChangedEvent(element, newValue));
    }


    /**
     * Renders the GUI.
     * <i>Here are some happy little clouds, and some happy little trees.</i>
     */
    public void render() {
        drawTickbox("velocity_arrows", "Show velocity arrows",
                uiStates.get(UIElement.VELOCITY_ARROWS),
                bottomInitX, bottomInitY);

        drawTickbox("sphere_names", "Show sphere names",
                uiStates.get(UIElement.SPHERE_NAMES),
                bottomInitX, bottomInitY - itemSpacing);

        drawTickbox("sphere_weights", "Show sphere weights",
                uiStates.get(UIElement.SPHERE_WEIGHTS),
                bottomInitX, bottomInitY - itemSpacing * 2);

        drawTickbox("sphere_trails", "Show sphere trails",
                uiStates.get(UIElement.SPHERE_TRAILS),
                bottomInitX, bottomInitY - itemSpacing * 3);

        drawTickbox("gravity_enabled", "Enable gravity",
                uiStates.get(UIElement.GRAVITY_ENABLED),
                bottomInitX, bottomInitY - itemSpacing * 4);

        drawTickbox("bounds_enabled", "Enable boundaries",
                uiStates.get(UIElement.BOUNDS_ENABLED),
                bottomInitX, bottomInitY - itemSpacing * 5);
    }

    /**
     * Updates the UI.
     * <i>Draw me like one of your French GUIs.</i>
     */
    public void drawGUI() {
        drawHints();

        if (gravityScroll != null) {
            gravityScroll.update();
            gravityScroll.display();
        }

        if (speedScroll != null) {
            speedScroll.update();
            speedScroll.display();
        }
    }

    /**
     * Draw hints and UI overlays.
     * <i>This really tipped me off.</i>
     */
    public void drawHints() {
        if (!getDisplaySetting(GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)) {
            return;
        }

        app.textFont(fontLight);
        app.fill(255, 255, 0);

        if (isFreeCamEnabled()) {
            app.text("Use wasd/zqsd to move around.", app.width - 575, app.height - 330);
            app.text("Use right-click to move the camera laterally.", app.width - 575, app.height - 280);
        }

        app.text("Press 'f' to toggle freecam", app.width - 575, app.height - 230);
        app.text("Press 'c' to reset camera position.", app.width - 575, app.height - 180);
        app.text("Press 'h' to hide the interface.", app.width - 575, app.height - 130);
        app.text("Press 'r' to restart the simulation.", app.width - 575, app.height - 30);

        if (getDisplaySetting(GUIStateChangedEvent.UIElement.SIMULATION_PAUSED)) {
            app.textFont(fontLight);
            app.text("Press 'p' to unpause the simulation.", app.width - 575, app.height - 80);
            app.textFont(fontBold);
            app.noStroke();
            app.fill(255, 0, 0);
            app.rect(35, 68, 10, 30);
            app.rect(50, 68, 10, 30);
            app.text("PAUSED", 75, 100);
        } else {
            app.textFont(fontLight);
            app.text("Press 'p' to pause the simulation.", app.width - 575, app.height - 80);
            app.textFont(fontBold);
            app.noStroke();
            if (app.millis() - unpausedTimer < 2000) {
                app.fill(0, PApplet.lerp(255, 0, (float) (app.millis() - unpausedTimer) / 2000), 0);
                app.triangle(35, 70, 35, 96, 65, 83);
                app.text("RUNNING", 75, 100);
            }
        }
    }

    /**
     * Draws a tickbox along with its label.
     * Normal color is white, active is yellow, disabled is gray.
     * <i>It's my tick in a box!</i>
     * @param elementId ID of the element.
     * @param elementLabel Label of the element.
     * @param active Whether the element is active.
     * @param xPosition X coordinate of the element.
     * @param yPosition Y coordinate of the element.
     */
    private void drawTickbox(String elementId, String elementLabel, boolean active, int xPosition, int yPosition) {
        boolean hovered = hoverStates.getOrDefault(elementId, false);

        if (hovered) {
            app.fill(51);
        } else {
            app.fill(0);
        }
        app.rect(xPosition, yPosition, 20, 20);

        if (active) {
            app.fill(255, 255, 0);
            app.pushMatrix();
            app.translate(0, 0, 1);
            app.text("X", xPosition, yPosition + 20);
            app.popMatrix();
        }

        if (hovered) {
            app.fill(255);
        } else {
            app.fill(200);
        }
        app.text(elementLabel, xPosition + 30, yPosition + 20);
    }

    public boolean getDisplaySetting(UIElement element) {
        return uiStates.getOrDefault(element, false);
    }

    public boolean isFreeCamEnabled() {
        return uiStates.get(UIElement.FREE_CAM);
    }


    /**
     * Set the gravity constant.
     */
    private void setGravityConstant(float newGravity) {
        eventManager.publish(new GravityChangedEvent(newGravity));
    }

    /**
     * Set the global speed.
     */
    private void setGlobalSpeed(float newSpeed) {
        eventManager.publish(new SpeedChangedEvent((int) newSpeed));
    }


    /**
     * Sets up the GUI sliders.
     * @param width Width of the window.
     * @param height Height of the window.
     * <i>Slide to the left. One hop this time.</i>
     */
    public void setupSliders(int width, int height) {

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
                this.app,
                eventManager,
                this,
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
                this.app,
                eventManager,
                this,
                "speed_scroll",
                true,
                30.0f
        );
    }

    /**
     * Sets the pause timer for the 'pause' text indicator.
     * <i>Time's ticking.</i>
     * @param timer Timer in milliseconds.
     */
    public void setUnpausedTimer(long timer) {
        this.unpausedTimer = timer;
    }

    /**
     * Handle hovering effects.
     * <i>It's hover Hanakin, I ave the igh ground!</i>
     */
    public void hover() {
        updateHoverStates();
    }

    /**
     * Returns the light font.
     * <i>Let there be light!</i>
     * @return The light font.
     */
    public PFont getFontLight() {
        return fontLight;
    }

    /**
     * Returns the bold font.
     * <i>Let there be boldness!</i>
     * @return The bold font.
     */
    public PFont getFontBold() {
        return fontBold;
    }
}
