package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import events.core.EventManager;
import events.simulation.SimulationRestartEvent;
import events.physics.GravityChangedEvent;
import events.physics.SpeedChangedEvent;
import events.graphics.gui.GUIStateChangedEvent;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Manages the physics simulation, sphere creation, and threading.
 * <i>The conductor of the physics orchestra.</i>
 */
public class SimulationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationHandler.class);
    private static float gravityConstant = 6.6743f;
    private static float targetPhysicsFPS = 60.0f;
    private static boolean gravityEnabled = true;
    private static boolean boundsEnabled = true;
    private static boolean isPaused = false;

    private static boolean drawTrails = false;
    private static boolean drawArrows = false;
    private static boolean drawNames = false;
    private static boolean drawWeights = false;

    public static final int DEFAULT_SPHERE_COUNT = 20;

    private final PApplet app;
    private final EventManager eventManager;
    private final int threadCount;

    private ArrayList<PhysicSphere> spheres = new ArrayList<>();
    private ArrayList<SphereBatchThread> sphereBatchThreads = new ArrayList<>();

    public SimulationHandler(final PApplet appParam, final EventManager eventManagerParam) {
        this.app = appParam;
        this.eventManager = eventManagerParam;
        this.threadCount = Runtime.getRuntime().availableProcessors();

        setupEventHandlers();
    }

    /**
     * Set up event handlers for simulation-related events.
     */
    private void setupEventHandlers() {
        eventManager.subscribe(SimulationRestartEvent.class, event -> {
            seed(event.sphereCount());
            LOGGER.info("Simulation restarted with {} spheres", event.sphereCount());
        });

        eventManager.subscribe(GravityChangedEvent.class, event -> {
            gravityConstant = event.newGravity();
            LOGGER.info("Gravity changed to: {}", gravityConstant);
        });

        eventManager.subscribe(SpeedChangedEvent.class, event -> {
            targetPhysicsFPS = event.newSpeed();
            LOGGER.info("Speed changed to: {}", targetPhysicsFPS);
        });

        eventManager.subscribe(GUIStateChangedEvent.class, event -> {
            switch (event.element()) {
                case SIMULATION_PAUSED -> {
                    isPaused = event.newState();
                    LOGGER.info("Simulation {}", (isPaused ? "paused" : "unpaused"));
                }
                case GRAVITY_ENABLED -> {
                    gravityEnabled = event.newState();
                    LOGGER.info("Gravity {}", (gravityEnabled ? "enabled" : "disabled"));
                }
                case BOUNDS_ENABLED -> {
                    boundsEnabled = event.newState();
                    LOGGER.info("Bounds {}", (boundsEnabled ? "enabled" : "disabled"));
                }
                case VELOCITY_ARROWS -> {
                    drawArrows = event.newState();
                    LOGGER.info("Velocity arrows {}", (drawArrows ? "enabled" : "disabled"));
                }
                case SPHERE_NAMES -> {
                    drawNames = event.newState();
                    LOGGER.info("Sphere names {}", (drawNames ? "enabled" : "disabled"));
                }
                case SPHERE_WEIGHTS -> {
                    drawWeights = event.newState();
                    LOGGER.info("Sphere weights {}", (drawWeights ? "enabled" : "disabled"));
                }
                case SPHERE_TRAILS -> {
                    drawTrails = event.newState();
                    LOGGER.info("Sphere trails {}", (drawTrails ? "enabled" : "disabled"));
                }
                default -> {
                    LOGGER.warn("Unknown GUI element: {}", event.element());
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
    public void seed(final int amount) {
        spheres = new ArrayList<>();
        sphereBatchThreads = new ArrayList<>();
        int randColor;

        for (int i = 0; i < amount; i++) {
            randColor = app.color(app.random(200) + 55, app.random(200) + 55, app.random(200) + 55);
            float randX = app.random(1000.0f);
            float randY = app.random(1000.0f);
            float randZ = app.random(1000.0f);
            float randR = app.random(2.0f + app.random(10.0f));
            PVector tPos = new PVector(randX, randY, randZ);

            for (PhysicSphere sphere : spheres) {
                while (PVector.dist(tPos, sphere.getPosition()) < randR) {
                    randX = app.random(1000.0f);
                    randY = app.random(1000.0f);
                    randZ = app.random(1000.0f);
                    randR = app.random(2 + app.random(10.0f));
                    tPos = new PVector(randX, randY, randZ);
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
    private void setupBatchThreads(final int amount) {
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
                    sphereBatchThreads.get(i).addToObjects(globalIdx);
                    globalIdx++;
                }
            }
        }
    }

    /**
     * Update the physics simulation.
     */
    public void update() throws InterruptedException {
        if (!isPaused) {
            sphereBatchThreads.forEach(Thread::run);
        }

        for (Thread t : sphereBatchThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for thread to finish: {}", e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Render all spheres.
     * <i>Show me them balls.</i>
     */
    public void renderSpheres() {
        spheres.forEach(PhysicSphere::display);
    }

    /**
     * Initialize the simulation with default settings.
     * <i>And thus, the universe was born.</i>
     */
    public void initialize() {
        seed(DEFAULT_SPHERE_COUNT);
    }

    /**
     * Returns whether the simulation is paused.
     * @return The pause state.
     * <i>Are you awake?</i>
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Returns whether bounds are enabled.
     * @return The bounds' state.
     * <i>You're pushing the limits.</i>
     */
    public static boolean areBoundsEnabled() {
        return boundsEnabled;
    }

    /**
     * Returns the gravity constant.
     * @return The gravity constant.
     * <i>Heavy dude.</i>
     */
    public static float getGravityConstant() {
        return gravityConstant;
    }

    /**
     * Returns the target physics FPS.
     * @return The target physics FPS.
     */
    public static int getTargetPhysicsFPS() {
        return (int) targetPhysicsFPS;
    }

    public static boolean isGravityEnabled() {
        return gravityEnabled;
    }

    public static boolean isDrawTrails() {
        return drawTrails;
    }

    public static boolean isDrawArrows() {
        return drawArrows;
    }

    public static boolean isDrawNames() {
        return drawNames;
    }

    public static boolean isDrawWeights() {
        return drawWeights;
    }
}
