package model;

import events.core.EventManager;
import events.simulation.SimulationRestartEvent;
import events.physics.GravityChangedEvent;
import events.physics.SpeedChangedEvent;
import events.gui.GUIStateChangedEvent;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Manages the physics simulation, sphere creation, and threading.
 * <i>The conductor of the physics orchestra.</i>
 */
public class SimulationManager {
    public static float G = 6.6743f;
    public static float targetPhysicsFPS = 60.0f;
    public static boolean gravityEnabled = true;
    public static boolean boundsEnabled = true;
    public static boolean isPaused = false;

    public static boolean drawTrails = false;
    public static boolean drawArrows = false;
    public static boolean drawNames = false;
    public static boolean drawWeights = false;

    public static final int DEFAULT_SPHERE_COUNT = 20;

    private final PApplet app;
    private final EventManager eventManager;
    private final int threadCount;

    private ArrayList<PhysicSphere> spheres = new ArrayList<>();
    private ArrayList<SphereBatchThread> sphereBatchThreads = new ArrayList<>();

    public SimulationManager(PApplet app, EventManager eventManager) {
        this.app = app;
        this.eventManager = eventManager;
        this.threadCount = Runtime.getRuntime().availableProcessors();

        setupEventHandlers();
    }

    /**
     * Set up event handlers for simulation-related events.
     */
    private void setupEventHandlers() {
        eventManager.subscribe(SimulationRestartEvent.class, event -> {
            seed(event.sphereCount());
            System.out.println("Simulation restarted with " + event.sphereCount() + " spheres");
        });

        eventManager.subscribe(GravityChangedEvent.class, event -> {
            G = event.newGravity();
            System.out.println("Gravity changed to: " + G);
        });

        eventManager.subscribe(SpeedChangedEvent.class, event -> {
            targetPhysicsFPS = event.newSpeed();
            System.out.println("Speed changed to: " + targetPhysicsFPS);
        });

        eventManager.subscribe(GUIStateChangedEvent.class, event -> {
            switch (event.element()) {
                case SIMULATION_PAUSED -> {
                    isPaused = event.newState();
                    System.out.println("Simulation " + (isPaused ? "paused" : "unpaused"));
                }
                case GRAVITY_ENABLED -> {
                    gravityEnabled = event.newState();
                    System.out.println("Gravity " + (gravityEnabled ? "enabled" : "disabled"));
                }
                case BOUNDS_ENABLED -> {
                    boundsEnabled = event.newState();
                    System.out.println("Bounds " + (boundsEnabled ? "enabled" : "disabled"));
                }
                case VELOCITY_ARROWS -> {
                    drawArrows = event.newState();
                    System.out.println("Velocity arrows " + (drawArrows ? "enabled" : "disabled"));
                }
                case SPHERE_NAMES -> {
                    drawNames = event.newState();
                    System.out.println("Sphere names " + (drawNames ? "enabled" : "disabled"));
                }
                case SPHERE_WEIGHTS -> {
                    drawWeights = event.newState();
                    System.out.println("Sphere weights " + (drawWeights ? "enabled" : "disabled"));
                }
                case SPHERE_TRAILS -> {
                    drawTrails = event.newState();
                    System.out.println("Sphere trails " + (drawTrails ? "enabled" : "disabled"));
                }
            }
        });
    }

    /**
     * Seeds the simulation's random params.
     * <i>If you were a kiss, I'd be a nod, and If you were a seed, well, I'd be a pod.</i>
     *
     * @param amount Number of spheres to seed.
     */
    public void seed(int amount) {
        spheres = new ArrayList<>();
        sphereBatchThreads = new ArrayList<>();
        int randColor;

        for (int i = 0; i < amount; i++) {
            randColor = app.color(app.random(200) + 55, app.random(200) + 55, app.random(200) + 55);
            float randX = app.random(1000.0f);
            float randY = app.random(1000.0f);
            float randZ = app.random(1000.0f);
            float randR = app.random(2.0f + app.random(10.0f));
            PVector t_pos = new PVector(randX, randY, randZ);

            for (PhysicSphere sphere : spheres) {
                while (PVector.dist(t_pos, sphere.position) < randR) {
                    randX = app.random(1000.0f);
                    randY = app.random(1000.0f);
                    randZ = app.random(1000.0f);
                    randR = app.random(2 + app.random(10.0f));
                    t_pos = new PVector(randX, randY, randZ);
                }
            }

            spheres.add(
                    new PhysicSphere(
                            app,
                            i,
                            randColor,
                            new PVector(randX, randY, randZ),
                            new PVector(
                                    1 - app.random(5),
                                    1 - app.random(5),
                                    1 - app.random(5)
                            ),
                            randR,
                            0.5f + app.random(0.5f)
                    )
            );
        }

        setupBatchThreads(amount);
    }

    /**
     * Set up batch threads for parallel physics processing.
     */
    private void setupBatchThreads(int amount) {
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
            }
        }
    }

    /**
     * Update the physics simulation.
     */
    public void update() {
        if (!isPaused) {
            sphereBatchThreads.forEach(Thread::run);
        }

        for (Thread t : sphereBatchThreads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Render all spheres.
     */
    public void render() {
        spheres.forEach(PhysicSphere::display);
    }

    /**
     * Initialize the simulation with default settings.
     */
    public void initialize() {
        seed(DEFAULT_SPHERE_COUNT);
    }

    public ArrayList<PhysicSphere> getSpheres() {
        return spheres;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    public boolean areBoundsEnabled() {
        return boundsEnabled;
    }

    public float getGravityConstant() {
        return G;
    }

    public float getTargetPhysicsFPS() {
        return targetPhysicsFPS;
    }
}
