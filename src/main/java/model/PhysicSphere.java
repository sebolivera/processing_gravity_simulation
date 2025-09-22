package model;

import static misc.MathUtils.getNormalVector;
import static misc.VectorUtils.correctPVectorNaN;
import static misc.VectorUtils.nullifyPVectorNaN;
import static processing.core.PApplet.floor;
import static processing.core.PApplet.lerp;
import static processing.core.PConstants.SQUARE;

import app.GravityCollisionApp;

import java.util.ArrayList;
import java.util.List;

import misc.CollisionIndex;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Base class for the spheres. Core of the simulation. Each sphere has an update function that
 * allows it to look at all other objects and apply their gravity to it, while they do the same for
 * each other. A sphere has position (3D vector), a radius (float>0), a velocity (3D vector), a mass
 * (float>0), an acceleration (3D vector), a color (processing Color>=(255, 255, 255)), an index
 * (int>=0) and a bounciness (0<=float<=1).
 */
public class PhysicSphere {
    private final float maxWidth;
    private final float maxHeight;
    private final float maxDepth;
    private final int sphereColor;
    private final PVector position;
    private PVector velocity;
    private final List<PVector> prevPos = new ArrayList<>();
    // int maxSpeed = 5; // todo: implement this.
    private final float radius;
    private final float mass;
    private final float bounciness;
    private final int index;
    private PVector acceleration;
    private static PApplet app;

    /**
     * Overload for normal bounciness sphere. Bounciness is optional and has been known to cause
     * some issues when too many spheres are colliding, so it is 1 by default (perfect bounciness)
     * todo: Switch to builder pattern, this is going to be a nightmare soon enough.
     *
     * @param indexParam       Sphere index.
     * @param sphereColorParam Sphere color.
     * @param positionParam    Sphere initial position.
     * @param velocityParam    Sphere initial velocity.
     * @param radiusParam      Sphere radius.
     * @param massParam        Sphere mass.
     */
    public PhysicSphere(
            final PApplet appParam,
            final int indexParam,
            final int sphereColorParam,
            final PVector positionParam,
            final PVector velocityParam,
            final float radiusParam,
            final float massParam
    ) {
        this.maxWidth = 1000.0f;
        this.maxHeight = 1000.0f;
        this.maxDepth = 1000.0f;
        this.index = indexParam;
        this.sphereColor = sphereColorParam;
        this.position = positionParam;
        this.velocity = velocityParam;
        this.radius = radiusParam * 2;
        this.mass = massParam;
        this.acceleration = new PVector(0.0f, 0.0f, 0.0f);
        this.bounciness = 1.0f;
        setApp(appParam);
    }

    /**
     * Sets the Processing app for all spheres.
     *
     * @param appParam Processing app.
     *                 <i>Omagaaad, setaaaapp!</i>
     */
    public static void setApp(final PApplet appParam) {
        if (app == null) {
            app = appParam;
        }
    }

    /**
     * Draws an arrow. Adapted from <a
     * href="https://forum.processing.org/one/topic/drawing-an-arrow.html">here</a>. <i>It's only
     * stealing if it comes from StackOverflow, otherwise it's citing sources.</i>
     *
     * @param originX         X-coordinate of the arrow's origin.
     * @param originY         Y-coordinate of the arrow's origin.
     * @param originZ         Z-coordinate of the arrow's origin.
     * @param lengthScalar    Scalar applied to the length of the arrow.
     * @param targetDirection Target direction, expressed as a PVector.
     */
    private void drawArrow(
            final float originX,
            final float originY,
            final float originZ,
            final float lengthScalar,
            final PVector targetDirection
    ) {
        app.pushMatrix();
        app.strokeWeight(radius / 2);
        app.translate(originX, originY, originZ);
        final PVector targetDirectionCopy = targetDirection.copy();
        targetDirectionCopy.normalize();
        targetDirectionCopy.mult(lengthScalar * targetDirectionCopy.mag() * 10);

        app.stroke(
                255 - app.red(sphereColor),
                255 - app.green(sphereColor),
                255 - app.blue(sphereColor));
        app.line(0, 0, 0, targetDirectionCopy.x, targetDirectionCopy.y, targetDirectionCopy.z);
        app.fill(
                255 - app.red(sphereColor),
                255 - app.green(sphereColor),
                255 - app.blue(sphereColor));
        final float halfBaseSize = radius / 2;
        final float tipLength = radius;
        app.translate(targetDirectionCopy.x, targetDirectionCopy.y, targetDirectionCopy.z);
        app.noStroke();
        app.beginShape();
        app.vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(
                tipLength * targetDirectionCopy.x / 100,
                tipLength * targetDirectionCopy.y / 100,
                tipLength * targetDirectionCopy.z / 100);
        app.endShape();
        app.beginShape();
        app.vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(
                tipLength * targetDirectionCopy.x / 100,
                tipLength * targetDirectionCopy.y / 100,
                tipLength * targetDirectionCopy.z / 100);
        app.endShape();

        app.beginShape();
        app.vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(
                tipLength * targetDirectionCopy.x / 100,
                tipLength * targetDirectionCopy.y / 100,
                tipLength * targetDirectionCopy.z / 100);
        app.endShape();

        app.beginShape();
        app.vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(
                tipLength * targetDirectionCopy.x / 100,
                tipLength * targetDirectionCopy.y / 100,
                tipLength * targetDirectionCopy.z / 100);
        app.endShape();

        app.popMatrix();
    }

    /**
     * Checks whether one given sphere is colliding with the current instance. Accounts for both the
     * current frame and the next one in case of high speeds. <i>Are you hitting on me?</i>
     *
     * @param other Another instance of a sphere.
     * @return {@code true} if the spheres are colliding.
     */
    private boolean isCollidingWith(final PhysicSphere other) {
        if (index != other.index) {
            final boolean isFrameColliding =
                    other.position.dist(position) < other.radius * 2 + radius * 2;
            final PVector vectorizedPosition = position.copy();
            vectorizedPosition.dot(velocity);
            final PVector otherVectorizedPosition = other.position.copy();
            otherVectorizedPosition.dot(other.velocity);
            final boolean isVectorColliding =
                    vectorizedPosition.dist(otherVectorizedPosition)
                            < other.radius * 2 + radius * 2;
            return isFrameColliding || isVectorColliding;
        }
        return false;
    }

    /**
     * Handles collision detection and resolution between the current PhysicSphere instance and the
     * provided PhysicSphere instance. See: - Momentum conservation in angle collisions between two
     * spherical bodies: <a
     * href="https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html">here</a> -
     * Elastic collision and exchange of momentum between two bodies with different masses: <a
     * href="https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution">here</a>
     * - todo: add rotation to the equation (check <a
     * href="https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm">here</a>).
     * ISSUE: don't know how to handle rotation of objects yet <i>Are you insured?</i>
     *
     * @param other The other sphere involved in the collision.
     */
    private void collideWith(final PhysicSphere other) {
        if (isCollidingWith(other) && CollisionIndex.tryLock(this.index, other.index)) {
            try {
                final PVector impulseSelf =
                        getNormalVector(
                                velocity,
                                position,
                                other.position); // selfImpulseVector & v_imp_1 are swapped
                final PVector impulseOther =
                        getNormalVector(other.velocity, other.position, position);

                final PVector residualVelocityOther = velocity.copy();
                residualVelocityOther.sub(impulseOther);

                final PVector residualVelocitySelf = other.velocity.copy();
                residualVelocitySelf.sub(impulseSelf);

                PVector impactVelocitySelf = velocity.copy();
                final PVector impactVelocityOther;

                impactVelocityOther =
                        new PVector(
                                impactVelocitySelf.mag()
                                        * (residualVelocitySelf.x / residualVelocitySelf.mag()),
                                impactVelocitySelf.mag()
                                        * (residualVelocitySelf.y / residualVelocitySelf.mag()),
                                impactVelocitySelf.mag()
                                        * (residualVelocitySelf.z
                                        / residualVelocitySelf.mag()));
                impactVelocitySelf =
                        new PVector(
                                impactVelocityOther.mag()
                                        * (residualVelocityOther.x
                                        / residualVelocityOther.mag()),
                                impactVelocityOther.mag()
                                        * (residualVelocityOther.y
                                        / residualVelocityOther.mag()),
                                impactVelocityOther.mag()
                                        * (residualVelocityOther.z
                                        / residualVelocityOther.mag()));

                final PVector momentumSelf = impactVelocitySelf.copy();
                momentumSelf.mult(mass);

                final PVector momentumOther = impactVelocityOther.copy();
                momentumOther.mult(other.mass);

                final PVector totalMomentum = momentumSelf.copy();
                totalMomentum.add(momentumOther);

                final PVector bounceVelocitySelf = impactVelocitySelf.copy();
                bounceVelocitySelf.mult(bounciness);
                final PVector bounceVelocityOther = impactVelocityOther.copy();
                bounceVelocityOther.mult(bounciness);

                // b_part => (((1-bounciness)(v_1_i-v_2_i)+v_1_f)*other.mass)/mass
                final PVector restitutionDelta = bounceVelocitySelf.copy();
                restitutionDelta.sub(bounceVelocityOther);

                // v_1_f => (v_1_i*mass+v_2_i*other.mass-(1-bounciness)*(v_1_i-v_2_i))/(mass+1)
                final PVector finalVelocitySelf = totalMomentum.copy();
                finalVelocitySelf.sub(restitutionDelta);

                finalVelocitySelf.div(mass + 1.0f);

                // v_2_f => (1-bounciness)*(v_1_i-v_2_i)+v_1_f
                final PVector finalVelocityOther = finalVelocitySelf.copy();
                finalVelocityOther.add(restitutionDelta);

                if (!Float.isNaN(
                        finalVelocitySelf.x + finalVelocitySelf.y + finalVelocitySelf.z)) {
                    velocity = finalVelocitySelf;
                }

                if (!Float.isNaN(
                        finalVelocityOther.x + finalVelocityOther.y + finalVelocityOther.z)) {
                    other.velocity = finalVelocityOther;
                }

                correctClipping(other);
            } finally {
                CollisionIndex.unlock(this.index, other.index);
            }
        }
    }

    /**
     * Attempts to correct some of the clipping issues. Note: very unoptimal.
     *
     * @param other Other sphere involved in the collision.
     */
    private void correctClipping(final PhysicSphere other) {
        final PVector tCorrector = velocity.copy();
        tCorrector.sub(getNormalVector(velocity, position, other.position));
        tCorrector.mult(.1f);
        int safetyCounter = 0;
        while (isCollidingWith(other) && safetyCounter < 10_000) {
            position.add(tCorrector);
            safetyCounter++;
        }
    }

    /**
     * Applies gravity forces (provided they are enabled) to a sphere as well as all the others.
     * Note: this part accesses other spheres without going through the thread structures, as the
     * movement from one frame to the other should be negligible. <i>I think I'm falling for
     * you.</i>
     *
     * @param others List of all the other spheres.
     */
    public void applyAttraction(final List<PhysicSphere> others) {
        final PVector finalAcc = new PVector(0, 0, 0);
        PVector tAcc = new PVector(0, 0, 0);
        for (final PhysicSphere other : others) {
            collideWith(other);
            if (other.index != index && SimulationHandler.isGravityEnabled()) {
                float smallGFactor = -other.mass * SimulationHandler.getGravityConstant();
                smallGFactor /= other.position.dist(position) * other.position.dist(position);
                final PVector smallG = position.copy();
                smallG.sub(other.position);
                smallG.mult(smallGFactor);
                tAcc = smallG.copy();
                tAcc.mult(mass);
                finalAcc.add(smallG);
            }
            if (!Float.isNaN(tAcc.x + tAcc.y + tAcc.z)) {
                acceleration = finalAcc.copy();
            }
        }
    }

    /**
     * Updates the position of the sphere. <i>Take care of them.</i>
     */
    public void update() {
        final float fpsCount = 60.0f;
        if (SimulationHandler.getTargetPhysicsFPS() < fpsCount) {
            final int frameInterval = Math.round(fpsCount / SimulationHandler.getTargetPhysicsFPS());
            if (GravityCollisionApp.getFrames() % frameInterval == 0) {
                updatePhysics();
            }
        } else {
            int physicsStepsThisFrame = Math.round(SimulationHandler.getTargetPhysicsFPS() / fpsCount);
            physicsStepsThisFrame = Math.max(1, Math.min(physicsStepsThisFrame, 10));

            for (int i = 0; i < physicsStepsThisFrame; i++) {
                updatePhysics();
            }
        }
    }

    /**
     * Updates the position and the velocity of the sphere.
     */
    private void updatePhysics() {
        prevPos.add(position.copy());
        velocity.add(acceleration);
        if (SimulationHandler.areBoundsEnabled()) {
            if (position.x <= 0 && velocity.x < 0) {
                velocity.x = -velocity.x * bounciness;
            } else if (position.x >= maxWidth && velocity.x > 0) {
                velocity.x = -velocity.x * bounciness;
            }
            if (position.y <= 0 && velocity.y < 0) {
                velocity.y = -velocity.y * bounciness;
            } else if (position.y >= maxHeight && velocity.y > 0) {
                velocity.y = -velocity.y * bounciness;
            }
            if (position.z <= 0 && velocity.z < 0) {
                velocity.z = -velocity.z * bounciness;
            } else if (position.z >= maxDepth && velocity.z > 0) {
                velocity.z = -velocity.z * bounciness;
            }
        }
        nullifyPVectorNaN(velocity);
        position.add(velocity);
        correctPVectorNaN(position, prevPos);
    }

    /**
     * Draws a sphere according to its position radius, color, index (name) and tail effect.
     * <i>Datnoplay.</i>
     */
    public void display() {
        app.pushMatrix();
        app.translate(position.x, position.y, position.z);
        app.noStroke();
        app.fill(sphereColor, 200);
        app.sphere(radius * 2);
        app.popMatrix();
        app.fill(
                255 - app.red(sphereColor),
                255 - app.green(sphereColor),
                255 - app.blue(sphereColor));
        app.textSize(radius * 3);
        if (SimulationHandler.isDrawNames()) {
            app.text(
                    (char) (index + 65),
                    lerp(maxWidth * 0.05f, maxWidth * 0.95f, (position.x - radius) / maxWidth),
                    lerp(maxHeight * 0.05f, maxHeight * 0.95f, (position.y + radius) / maxHeight)
                            + 100f,
                    position.z + radius * 2f);
        }
        if (SimulationHandler.isDrawWeights()) {
            app.text(
                    floor(mass * 100),
                    lerp(maxWidth * 0.05f, maxWidth * 0.95f, (position.x - radius) / maxWidth),
                    lerp(maxHeight * 0.05f, maxHeight * 0.95f, (position.y + radius) / maxHeight),
                    position.z + radius * 2);
        }
        app.noFill();
        app.beginShape();
        app.curveVertex(position.x, position.y, position.z);
        app.strokeCap(SQUARE);
        if (SimulationHandler.isDrawTrails()) {
            // Note: Processing's way of drawing strokes gives them no depth on the Z axis, which
            // makes them look flat when the balls turn at sharp angles or face slightly away from
            // the camera.
            for (int i = prevPos.isEmpty() ? 0 : prevPos.size() - 1;
                 i > (prevPos.size() > 20 ? prevPos.size() - 20 : 0);
                 i--) {
                app.stroke(
                        sphereColor,
                        lerp(
                                255f,
                                25f,
                                ((float) (prevPos.size() < 20 ? i : prevPos.size() - i))
                                        / Math.min(prevPos.size(), 20)));
                app.strokeWeight(
                        lerp(
                                0,
                                radius * 2,
                                lerp(
                                        1.0f,
                                        0,
                                        ((float) (prevPos.size() - i))
                                                / Math.min(prevPos.size(), 20))));
                app.curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z);
            }
        }
        app.endShape();

        if (index >= 0 && SimulationHandler.isDrawArrows()) {
            drawArrow(position.x, position.y, position.z, radius, velocity);
        }
    }

    /**
     * Returns the position of the sphere.
     *
     * @return The position of the sphere. <i>Ah, I finally found my bearings.</i>
     */
    public PVector getPosition() {
        return position;
    }
}
