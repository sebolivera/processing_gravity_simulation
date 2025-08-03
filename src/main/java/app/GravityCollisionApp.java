package app;

import events.core.EventManager;
import events.physics.CameraChangedEvent;
import events.physics.GravityChangedEvent;
import events.physics.SpeedChangedEvent;
import gui.HScrollBar;
import model.PhysicSphere;
import model.SphereBatchThread;
import processing.core.*;
import java.util.ArrayList;
import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {


    public static float CAM_PAN_STEP = 20;
    public static int FRAMES = 0;
    public static float CAM_DOLLY_STEP = 20;
    public static int GLOBAL_SPEED = 0;
    public boolean firstMousePress = false;
    public static float G = 6.6743f;
    public static boolean DRAW_TRAILS = false;
    public static boolean DRAW_ARROWS = false;
    public static boolean DRAW_NAMES = false;
    public static boolean DRAW_WEIGHTS = false;
    public static boolean ENABLE_GRAVITY = true;
    public static boolean ENABLE_BOUNDS = true;
    public static boolean PAUSED = false;

    private EventManager eventManager;
    private CameraChangedEvent cameraEvent = new CameraChangedEvent();

    private ArrayList<PhysicSphere> spheres = new ArrayList<>();
    private ArrayList<SphereBatchThread> sphereBatchThreads = new ArrayList<>();

    private HScrollBar gravityScroll;
    private HScrollBar speedScroll;

    @Override
    public void settings() {
        size(1000, 1000, P3D);
    }

    @Override
    public void setup() {
        eventManager = new EventManager();
        setupEventHandlers();
        cameraEvent.ResetCameraEvent(width, height);
        /* …everything that was in the old setup() stays here … */
        textFont(createFont("Roboto-Black.ttf", 128));
        seed(SPHERE_COUNT);
        initGUI();
        noCursor();
    }

    @Override
    public void draw() {
        background(0);
        lights();
        cameraEvent.FeedEvent();

        hover();
        drawBounds();

        if (!PAUSED) {
            sphereBatchThreads.forEach(Thread::run);
        }
        spheres.forEach(PhysicSphere::display);

        for (Thread t : sphereBatchThreads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        drawGUI();
        move();
        FRAMES++;
    }

    /* === callbacks still belong here =================================== */

    @Override
    public void mousePressed() { /* same code as before */ }

    @Override
    public void mouseWheel(MouseEvent evt) { /* … */ }

    @Override
    public void keyPressed() { /* … */ }

    @Override
    public void keyReleased() { /* … */ }


    /**
     * Set up event handlers for various events.
     * <i>The party planning committee.</i>
     */
    private void setupEventHandlers() {
        // Handle gravity changes
        eventManager.subscribe(GravityChangedEvent.class, event -> {
            G = event.getNewGravity();
            System.out.println("Gravity changed to: " + G);
        });

        // Handle speed changes
        eventManager.subscribe(SpeedChangedEvent.class, event -> {
            GLOBAL_SPEED = event.getNewSpeed();
            System.out.println("Speed changed to: " + GLOBAL_SPEED);
        });

        // Camera changes.
        eventManager.subscribe(CameraChangedEvent.class, event -> {
            System.out.println("Camera updated via event system");
        });

    }


    /**
     * Get the event manager.
     * <i>I want to speak to your manager.</i>
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    public static void main(String[] args) {
        PApplet.main(GravityCollisionApp.class);
    }
}
