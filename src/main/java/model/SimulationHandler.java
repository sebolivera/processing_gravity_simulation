package model;

import events.core.EventManager;
import events.graphics.gui.GUIStateChangedEvent;
import events.physics.GravityChangedEvent;
import events.physics.SpeedChangedEvent;
import events.simulation.SimulationRestartEvent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Manages the physics simulation, sphere creation, and threading. <i>The conductor of the physics
 * orchestra.</i>
 */
public class SimulationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationHandler.class);
    private static float gravityConstant = 6.6743f;
    private static float targetPhysicsFPS = 60.0f;
    private static boolean gravityEnabled;
    private static boolean boundsEnabled;
    private static boolean paused;
    private static boolean drawTrails;
    private static boolean drawArrows;
    private static boolean drawNames;
    private static boolean drawWeights;

    public static final int DEFAULT_SPHERE_COUNT = 20;

    private final PApplet app;
    private final EventManager eventManager;
    private final int threadCount;

    private List<PhysicSphere> spheres = new ArrayList<>();
    private List<SphereBatchThread> sphereBatchThreads = new ArrayList<>();

    public SimulationHandler(final PApplet appParam, final EventManager eventManagerParam) {
        this.app = appParam;
        this.eventManager = eventManagerParam;
        this.threadCount = Runtime.getRuntime().availableProcessors();
        setDefaultSimulationProps();
        setupEventHandlers();
    }

    /** Set up the simulation default properties. */
    private static void setDefaultSimulationProps() {
        paused = false;
        drawTrails = false;
        drawArrows = false;
        drawNames = false;
        drawWeights = false;
        gravityEnabled = true;
        boundsEnabled = true;
    }

    /** Set up event handlers for simulation-related events. */
    private void setupEventHandlers() {
        eventManager.subscribe(
                SimulationRestartEvent.class,
                event -> {
                    seed(event.sphereCount());
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Simulation restarted with {} spheres", event.sphereCount());
                    }
                });

        eventManager.subscribe(
                GravityChangedEvent.class,
                event -> {
                    gravityConstant = event.newGravity();
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Gravity changed to: {}", gravityConstant);
                    }
                });

        eventManager.subscribe(
                SpeedChangedEvent.class,
                event -> {
                    targetPhysicsFPS = event.newSpeed();
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Speed changed to: {}", targetPhysicsFPS);
                    }
                });

        eventManager.subscribe(
                GUIStateChangedEvent.class,
                event -> {
                    switch (event.element()) {
                        case SIMULATION_PAUSED -> {
                            paused = event.newState();
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Simulation {}", paused ? "paused" : "unpaused");
                            }
                        }
                        case GRAVITY_ENABLED -> {
                            gravityEnabled = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Gravity {}", gravityEnabled);
                            }
                        }
                        case BOUNDS_ENABLED -> {
                            boundsEnabled = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Bounds {}", boundsEnabled);
                            }
                        }
                        case VELOCITY_ARROWS -> {
                            drawArrows = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Velocity arrows {}", drawArrows);
                            }
                        }
                        case SPHERE_NAMES -> {
                            drawNames = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Sphere names {}", drawNames);
                            }
                        }
                        case SPHERE_WEIGHTS -> {
                            drawWeights = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Sphere weights {}", drawWeights);
                            }
                        }
                        case SPHERE_TRAILS -> {
                            drawTrails = event.newState();
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Sphere trails {}", drawTrails);
                            }
                        }
                        default -> {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Unknown GUI element: {}", event.element());
                            }
                        }
                    }
                });
    }

    /**
     * Seeds the simulation's random params. <i>If you were a kiss, I'd be a nod, and If you were a
     * seed, well, I'd be a pod.</i>
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

            for (final PhysicSphere sphere : spheres) {
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
                            new PVector(1 - app.random(5), 1 - app.random(5), 1 - app.random(5)),
                            randR,
                            0.5f + app.random(0.5f)));
        }

        setupBatchThreads(amount);
    }

    /** Set up batch threads for parallel physics processing. */
    private void setupBatchThreads(final int amount) {
        sphereBatchThreads = new ArrayList<>();
        final List<Integer> spheresIdxBatch = new ArrayList<>();

        if (threadCount > amount) {
            for (int i = 0; i < amount; i++) {
                spheresIdxBatch.add(i);
            }
            sphereBatchThreads.add(new SphereBatchThread(spheresIdxBatch, spheres));
            spheresIdxBatch.clear();
        } else {
            final int itemsPerThread = amount / threadCount;
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
                final int remainingObjs = amount - globalIdx;
                for (int i = 0; i < remainingObjs; i++) {
                    sphereBatchThreads.get(i).addToObjects(globalIdx);
                    globalIdx++;
                }
            }
        }
    }

    /** Update the physics simulation. */
    public void update() throws InterruptedException {
        if (!paused) {
            sphereBatchThreads.forEach(Thread::run);
        }

        for (final Thread t : sphereBatchThreads) {
            try {
                t.join();
            } catch (InterruptedException exc) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(
                            "Interrupted while waiting for thread to finish: {}", exc.getMessage());
                }
                throw exc;
            }
        }
    }

    /** Render all spheres. <i>Show me them balls.</i> */
    public void renderSpheres() {
        spheres.forEach(PhysicSphere::display);
    }

    /** Initialize the simulation with default settings. <i>And thus, the universe was born.</i> */
    public void initialize() {
        seed(DEFAULT_SPHERE_COUNT);
    }

    /**
     * Returns whether the simulation is paused.
     *
     * @return The pause state. <i>Are you awake?</i>
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns whether bounds are enabled.
     *
     * @return The bounds' state. <i>You're pushing the limits.</i>
     */
    public static boolean areBoundsEnabled() {
        return boundsEnabled;
    }

    /**
     * Returns the gravity constant.
     *
     * @return The gravity constant. <i>Heavy dude.</i>
     */
    public static float getGravityConstant() {
        return gravityConstant;
    }

    /**
     * Returns the target physics FPS.
     *
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
