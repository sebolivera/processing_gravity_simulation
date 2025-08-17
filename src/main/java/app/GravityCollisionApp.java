package app;

import events.core.EventManager;
import events.gui.DisplaySettingsChangedEvent;
import events.physics.CameraChangedEvent;
import events.physics.GravityChangedEvent;
import events.physics.SimulationPausedEvent;
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
    private int sphereCount = 20;
    public static float G = 6.6743f;
    public static boolean DRAW_TRAILS = false;
    public static boolean DRAW_ARROWS = false;
    public static boolean DRAW_NAMES = false;
    public static boolean DRAW_WEIGHTS = false;
    public static boolean ENABLE_GRAVITY = true;
    public static boolean ENABLE_BOUNDS = true;
    public static boolean PAUSED = false;
    int threadCount = Runtime.getRuntime().availableProcessors();

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
        textFont(createFont("Roboto-Black.ttf", 128));
        seed(sphereCount);
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
        eventManager.subscribe(GravityChangedEvent.class, event -> {
            G = event.newGravity();
            System.out.println("Gravity changed to: " + G);
        });

        eventManager.subscribe(SpeedChangedEvent.class, event -> {
            GLOBAL_SPEED = event.newSpeed();
            System.out.println("Speed changed to: " + GLOBAL_SPEED);
        });

        eventManager.subscribe(CameraChangedEvent.class, event -> {
            System.out.println("Camera updated via event system");
        });
        eventManager.subscribe(SimulationPausedEvent.class, event -> {
            PAUSED = event.isPaused();
            System.out.println("Simulation " + (PAUSED ? "paused" : "unpaused"));
        });

        eventManager.subscribe(DisplaySettingsChangedEvent.class, event -> {
            switch (event.getSetting()) {
                case VELOCITY_ARROWS -> DRAW_ARROWS = event.isEnabled();
                case SPHERE_NAMES -> DRAW_NAMES = event.isEnabled();
                case SPHERE_WEIGHTS -> DRAW_WEIGHTS = event.isEnabled();
                case SPHERE_TRAILS -> DRAW_TRAILS = event.isEnabled();
                case BOUNDS_VISIBLE -> ENABLE_BOUNDS = event.isEnabled();
            }
            System.out.println("Display setting " + event.getSetting() + " changed to: " + event.isEnabled());
        });

        eventManager.subscribe(CameraChangedEvent.class, event -> {
            System.out.println("Camera updated via event system");
        });
    }

    public void seed(int amount)//Creates
    {
        spheres = new ArrayList<>();
        sphereBatchThreads = new ArrayList<>();
        int randColor;
        for (int i = 0; i < amount; i++) {
            randColor = color(random(200) + 55, random(200) + 55, random(200) + 55);
            float randX = random(1000.0f);
            float randY = random(1000.0f);
            float randZ = random(1000.0f);
            float randR = random(2.0f + random(10.0f));
            PVector t_pos = new PVector(randX, randY, randZ);
            for (int j = 0; j < spheres.size(); j++) {
                while (PVector.dist(t_pos, spheres.get(j).position) < randR) {
                    randX = random(1000.0f);
                    randY = random(1000.0f);
                    randZ = random(1000.0f);
                    randR = random(2 + random(10.0f));
                    t_pos = new PVector(randX, randY, randZ);
                }
            }
            spheres.add(
                    new PhysicSphere(
                            i,
                            randColor,
                            new PVector(
                                    randX,
                                    randY,
                                    randZ
                            ),
                            new PVector(
                                    1 - random(5),
                                    1 - random(5),
                                    1 - random(5)
                            ),
                            randR,
                            0.5f + random(0.5f)
                    )
            );
        }

        sphereBatchThreads = new ArrayList<>();

        ArrayList<Integer> spheresIdxBatch = new ArrayList<>();
        if (threadCount > amount) {
            for (int i = 0; i < amount; i++) {
                spheresIdxBatch.add(i);
            }
            sphereBatchThreads.add(new SphereBatchThread(spheresIdxBatch, spheres));
            spheresIdxBatch.clear();//Technically useless since the app will most likely crash due to over-allocation of objects in the first place, but every little bit helps, I guess.
        } else {
            int itemsPerThread = amount / threadCount;
            int globalIdx = 0;
            for (int i = 0; i < threadCount; i++) {
                for (int j = 0; j < itemsPerThread; j++) {
                    spheresIdxBatch.add(globalIdx);
                    globalIdx++;
                }
                sphereBatchThreads.add(new SphereBatchThread(spheresIdxBatch, spheres));
                spheresIdxBatch.clear();
            }
            if (amount % threadCount != 0) {
                int remainingObjs = (amount - globalIdx);
                for (int i = 0; i < remainingObjs; i++) {
                    sphereBatchThreads.get(i).addToObjs(globalIdx);
                    globalIdx++;
                }
                spheresIdxBatch.clear();
            }
        }
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
