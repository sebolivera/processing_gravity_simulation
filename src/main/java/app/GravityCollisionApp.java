package app;

import events.core.EventManager;
import events.gui.DisplaySettingsChangedEvent;
import events.gui.GUIManager;
import events.gui.GUIStateChangedEvent;
import events.physics.CameraChangedEvent;
import events.physics.GravityChangedEvent;
import events.physics.SimulationPausedEvent;
import events.physics.SpeedChangedEvent;
import gui.HScrollBar;
import misc.MathUtils;
import model.PhysicSphere;
import model.SphereBatchThread;
import processing.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import processing.event.MouseEvent;

public final class GravityCollisionApp extends PApplet {


    public static float CAM_PAN_STEP = 20;
    public static int FRAMES = 0;
    public static float CAM_DOLLY_STEP = 20;
    public static int GLOBAL_SPEED = 0;
    private final int SPHERE_COUNT = 20;
    public boolean firstMousePress = false;
    public static float G = 6.6743f;
    public static boolean drawTrails = false;
    public static boolean drawArrows = false;
    public static boolean drawNames = false;
    public static boolean drawWeights = false;
    public static boolean gravityEnabled = true;
    public static boolean boundsEnabled = true;
    public static boolean isPaused = false;

    int threadCount = Runtime.getRuntime().availableProcessors();


    private final Set<String> keysDown = new HashSet<>();
    private final Set<String> moveKeys = Set.of("z", "q", "s", "d", "w", "a");
    private boolean isShiftDown = false;
    private boolean isAzerty = false; // For keyboard layout toggle

    private EventManager eventManager;
    private final CameraChangedEvent cameraEvent = new CameraChangedEvent();

    private ArrayList<PhysicSphere> spheres = new ArrayList<>();
    private ArrayList<SphereBatchThread> sphereBatchThreads = new ArrayList<>();

    private HScrollBar gravityScroll;
    private HScrollBar speedScroll;
    private GUIManager guiManager;

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
        seed(SPHERE_COUNT);
        guiManager = new GUIManager(eventManager, this);
        initGUI();
        noCursor();
    }

    private void initGUI() {
        int bottomInitX = 50;
        int bottomInitY = height - 50;

        MathUtils.FloatFunction editGLambda = this::setGravityConstant;
        MathUtils.FloatFunction editGLOBAL_SPEEDLambda = this::setGlobalSpeed;

        gravityScroll = new HScrollBar(
                bottomInitX,
                bottomInitY - 280,
                width / 3,
                16,
                0,
                2,
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
                bottomInitY - 350,
                width / 3,
                16,
                0,
                100,
                "Simulation speed scale",
                1.0f,
                editGLOBAL_SPEEDLambda,
                false,
                "Slow",
                "Fast",
                this,
                eventManager,
                guiManager,
                "speed_scroll"
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
     * <i>Wilson's not on anti-depressants.</i>
     */
    private void setGlobalSpeed(float newSpeed) {
        eventManager.publish(new SpeedChangedEvent((int) newSpeed));
    }

    /**
     * Updates the UI.
     * <i>Draw me like one of your French GUIs.</i>
     */
    private void drawGUI() {
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
     * Handle hovering effects.
     * <i>It's hover Hanakin, I ave the igh ground!</i>
     */
    private void hover() {
        // The GUIManager handles all hover detection and publishes hover events
        if (guiManager != null) {
            guiManager.updateHoverStates();
        }
    }


    /**
     * Draws the boundaries of the simulation, if enabled.
     * <i>This is where I would draw the line. IF I HAD ONE.</i>
     */
    void drawBounds() {
        if (boundsEnabled) {
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

    /**
     * Handle camera/object movement.
     * <i>Get out the way!</i>
     */
    private void move() {
        float yaw = radians(mouseX - pmouseX) * 0.125f;
        float pitch = radians(mouseY - pmouseY) * 0.125f;

        if (guiManager.isFreeCamEnabled()) {
            boolean cameraChanged = false;
            if (mousePressed && mouseButton == RIGHT) {
                float truckAmount = -(mouseX - pmouseX);
                float boomAmount = (mouseY - pmouseY);

                if (truckAmount != 0) {
                    cameraEvent.CameraTruckEvent(truckAmount);
                    cameraChanged = true;
                }
                if (boomAmount != 0) {
                    cameraEvent.CameraBoomEvent(boomAmount);
                    cameraChanged = true;
                }
            } else if (yaw != 0 || pitch != 0) {
                // Pan and tilt with mouse movement
                cameraEvent.CameraPanEvent(yaw);
                cameraEvent.CameraTiltEvent(pitch);
                cameraEvent.CameraRollEvent(0);
                cameraChanged = true;
            }

            // Handle keyboard-based camera movement
            float speedMult = isShiftDown ? 4 : 1;

            for (String currentKey : keysDown) {
                switch (currentKey) {
                    case "z", "w" -> {
                        cameraEvent.CameraDollyEvent(-CAM_DOLLY_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "s" -> {
                        cameraEvent.CameraDollyEvent(CAM_DOLLY_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "q", "a" -> {
                        cameraEvent.CameraTruckEvent(-CAM_PAN_STEP * speedMult);
                        cameraChanged = true;
                    }
                    case "d" -> {
                        cameraEvent.CameraTruckEvent(CAM_PAN_STEP * speedMult);
                        cameraChanged = true;
                    }
                }
            }

            // Publish camera event if any changes were made
            if (cameraChanged) {
                eventManager.publish(cameraEvent);
            }
        }
    }


    @Override
    public void draw() {
        background(0);
        lights();
        cameraEvent.FeedEvent();

        hover();
        drawBounds();

        if (!isPaused) {
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
    public void mousePressed() {
        if (!guiManager.isFreeCamEnabled()) {
            if (guiManager != null) {
                guiManager.handleMouseClick();
            }

            if (!firstMousePress) {
                firstMousePress = true;
            }
        } else {
            if (guiManager != null) {
                guiManager.handleMouseClick();
            }
        }
    }

    @Override
    public void mouseWheel(MouseEvent evt) {
        // Handle camera dolly in both free cam and regular mode for consistency with Processing
        float dollyAmount = evt.getCount() * CAM_DOLLY_STEP;
        cameraEvent.CameraDollyEvent(-dollyAmount);
        eventManager.publish(cameraEvent);
    }


    @Override
    public void keyPressed() {
        String keyStr = key == CODED ?
                (keyCode == SHIFT ? "SHIFT" : String.valueOf(keyCode)) :
                String.valueOf(key).toLowerCase();

        if (moveKeys.contains(keyStr)) {
            keysDown.add(keyStr);
        }

        if (keyCode == SHIFT) {
            isShiftDown = true;
        }

        switch (keyStr) {
            case "l" -> {
                isAzerty = !isAzerty;
                System.out.println("Keyboard layout switched to: " + (isAzerty ? "AZERTY" : "QWERTY"));
            }
            case "f" -> eventManager.publish(new GUIStateChangedEvent(
                    GUIStateChangedEvent.UIElement.FREE_CAM,
                    !guiManager.isFreeCamEnabled()
            ));
            case "c" -> {
                cameraEvent.ResetCameraEvent(width, height);
                eventManager.publish(cameraEvent);
            }
        }
    }


    @Override
    public void keyReleased() {
        String keyStr = key == CODED ?
                (keyCode == SHIFT ? "SHIFT" : String.valueOf(keyCode)) :
                String.valueOf(key).toLowerCase();

        if (moveKeys.contains(keyStr)) {
            keysDown.remove(keyStr);
        }

        if (keyCode == SHIFT) {
            isShiftDown = false;
        }

        switch (keyCode) {
            case 82 -> {
                seed(SPHERE_COUNT);
                System.out.println("Simulation restarted with " + SPHERE_COUNT + " spheres");
            }
            case 80 -> eventManager.publish(new GUIStateChangedEvent(
                    GUIStateChangedEvent.UIElement.SIMULATION_PAUSED,
                    !isPaused
            ));
            case 72 -> eventManager.publish(new GUIStateChangedEvent(
                    GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE,
                    !guiManager.getDisplaySetting(GUIStateChangedEvent.UIElement.INTERFACE_VISIBLE)
            ));
        }
    }

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

        eventManager.subscribe(CameraChangedEvent.class, event ->
                System.out.println("Camera updated via event system"));

        eventManager.subscribe(SimulationPausedEvent.class, event -> {
            isPaused = event.isPaused();
            System.out.println("Simulation " + (isPaused ? "paused" : "unpaused"));
        });

        eventManager.subscribe(DisplaySettingsChangedEvent.class, event -> {
            switch (event.getSetting()) {
                case VELOCITY_ARROWS -> drawArrows = event.isEnabled();
                case SPHERE_NAMES -> drawNames = event.isEnabled();
                case SPHERE_WEIGHTS -> drawWeights = event.isEnabled();
                case SPHERE_TRAILS -> drawTrails = event.isEnabled();
                case BOUNDS_VISIBLE -> boundsEnabled = event.isEnabled();
            }
            System.out.println("Display setting " + event.getSetting() + " changed to: " + event.isEnabled());
        });

        // Handle GUI state changes (new event handler)
        eventManager.subscribe(GUIStateChangedEvent.class, event -> {
            switch (event.getElement()) {
                case SIMULATION_PAUSED -> {
                    isPaused = event.getNewState();
                    System.out.println("Simulation " + (isPaused ? "paused" : "unpaused"));
                }
                case GRAVITY_ENABLED -> {
                    gravityEnabled = event.getNewState();
                    System.out.println("Gravity " + (gravityEnabled ? "enabled" : "disabled"));
                }
                case BOUNDS_ENABLED -> {
                    boundsEnabled = event.getNewState();
                    System.out.println("Bounds " + (boundsEnabled ? "enabled" : "disabled"));
                }
                case VELOCITY_ARROWS -> {
                    drawArrows = event.getNewState();
                    System.out.println("Velocity arrows " + (drawArrows ? "enabled" : "disabled"));
                }
                case SPHERE_NAMES -> {
                    drawNames = event.getNewState();
                    System.out.println("Sphere names " + (drawNames ? "enabled" : "disabled"));
                }
                case SPHERE_WEIGHTS -> {
                    drawWeights = event.getNewState();
                    System.out.println("Sphere weights " + (drawWeights ? "enabled" : "disabled"));
                }
                case SPHERE_TRAILS -> {
                    drawTrails = event.getNewState();
                    System.out.println("Sphere trails " + (drawTrails ? "enabled" : "disabled"));
                }
                case FREE_CAM -> System.out.println("Free cam " + (event.getNewState() ? "enabled" : "disabled"));
                case INTERFACE_VISIBLE -> System.out.println("Interface " + (event.getNewState() ? "visible" : "hidden"));
            }
        });
    }


    /**
     * Seeds the simulation's random params.
     * <i>And if you were a kiss, I'd be a nod, and If you were a seed, well, I'd be a pod.</i>
     *
     * @param amount Number of spheres to seed.
     */
    public void seed(int amount) {
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
            for (PhysicSphere sphere : spheres) {
                while (PVector.dist(t_pos, sphere.position) < randR) {
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
            spheresIdxBatch.clear();
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
