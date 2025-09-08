package model;

import app.GravityCollisionApp;
import misc.CollisionIndex;
import processing.core.*;

import java.util.ArrayList;

import static app.GravityCollisionApp.*;
import static misc.MathUtils.getNormalVector;
import static misc.VectorUtils.correctPVectorNaN;
import static misc.VectorUtils.nullifyPVectorNaN;

/**
 * Base class for the spheres.
 * Core of the simulation. Each sphere has an update function that allows it to look at
 * all other objects and apply their gravity to it, while they do the same for each other.
 * A sphere has position (3D vector), a radius (float>0), a velocity (3D vector), a mass
 * (float>0), an acceleration (3D vector), a color (processing Color>=(255, 255, 255)),
 * an index (int>=0) and a bounciness (0<=float<=1).
 */
public class PhysicSphere {
    float MAX_WIDTH = 1000.0f;
    float MAX_HEIGHT = 1000.0f;
    float MAX_DEPTH = 1000.0f;
    int sphereColor;
    public PVector position;
    PVector velocity;
    ArrayList<PVector> prevPos = new ArrayList<>();
    // int maxSpeed = 5; // TODO: implement this.
    float radius;
    float mass;
    float bounciness;
    int index;
    public PVector acceleration;
    public static PApplet app;

    /**
     * Overload for normal bounciness sphere.
     * Bounciness is optional and has been known to cause some issues when too many spheres are colliding,
     * so it is 1 by default (perfect bounciness)
     * TODO: Switch to builder pattern, this is going to be a nightmare soon enough.
     * @param index Sphere index.
     * @param sphereColor Sphere color.
     * @param position Sphere initial position.
     * @param velocity Sphere initial velocity.
     * @param radius Sphere radius.
     * @param mass Sphere mass.
     */
    public PhysicSphere(PApplet app, int index, int sphereColor, PVector position, PVector velocity, float radius, float mass) {
        this.index = index;
        this.sphereColor = sphereColor;
        this.position = position;
        this.velocity = velocity;
        this.radius = radius * 2;
        this.mass = mass;
        this.acceleration = new PVector(0.0f, 0.0f, 0.0f);
        this.bounciness = 1.0f;
        PhysicSphere.app = app;
    }

    /**
     * Creates an instance of a sphere.
     * @param index Sphere index.
     * @param sphereColor Sphere color.
     * @param position Sphere initial position.
     * @param velocity Sphere initial velocity.
     * @param radius Sphere radius.
     * @param mass Sphere mass.
     * @param bounciness Sphere bounciness.
     */
    PhysicSphere(int index, int sphereColor, PVector position, PVector velocity, float radius, float mass, float bounciness) {
        this.index = index;
        this.sphereColor = sphereColor;
        this.position = position;
        this.velocity = velocity;
        this.radius = radius * 2;
        this.mass = mass;
        this.acceleration = new PVector(0.0f, 0.0f, 0.0f);
        this.bounciness = bounciness;
    }


    /**
     * Draws an arrow. Adapted from <a href="https://forum.processing.org/one/topic/drawing-an-arrow.html">here</a>.
     * <i>It's only stealing if it comes from StackOverflow, otherwise it's citing sources.</i>
     * @param originX X-coordinate of the arrow's origin.
     * @param originY Y-coordinate of the arrow's origin.
     * @param originZ Z-coordinate of the arrow's origin.
     * @param lengthScalar Scalar applied to the length of the arrow.
     * @param targetDirection Target direction, expressed as a PVector.
     */
    void drawArrow(float originX, float originY, float originZ, float lengthScalar, PVector targetDirection) {//
        app.pushMatrix();
        app.strokeWeight(radius / 2);
        app.translate(originX, originY, originZ);
        targetDirection = targetDirection.copy();
        targetDirection.normalize();
        targetDirection.mult(lengthScalar * targetDirection.mag() * 10);

        app.stroke(255 - app.red(sphereColor), 255 - app.green(sphereColor), 255 - app.blue(sphereColor));
        app.line(0, 0, 0, targetDirection.x, targetDirection.y, targetDirection.z);
        app.fill(255 - app.red(sphereColor), 255 - app.green(sphereColor), 255 - app.blue(sphereColor));
        float halfBaseSize = radius / 2;
        float tipLength = radius;
        app.translate(targetDirection.x, targetDirection.y, targetDirection.z);
        app.noStroke();
        app.beginShape();
        app.vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        app.endShape();
        app.beginShape();
        app.vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        app.endShape();

        app.beginShape();
        app.vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        app.endShape();

        app.beginShape();
        app.vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        app.vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        app.vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        app.endShape();

        app.popMatrix();
    }


    /**
     * Checks whether one given sphere is colliding with the current instance.
     * Accounts for both the current frame and the next one in case of high speeds.
     * <i>Are you hitting on me?</i>
     * @param other Another instance of a sphere.
     * @return {@code true} if the spheres are colliding.
     */
    boolean isCollidingWith(PhysicSphere other) {
        if (index != other.index) {
            boolean isFrameColliding = other.position.dist(position) < other.radius * 2 + radius * 2;
            PVector vectorizedPosition = position.copy();
            vectorizedPosition.dot(velocity);
            PVector otherVectorizedPosition = other.position.copy();
            otherVectorizedPosition.dot(other.velocity);
            boolean isVectorColliding = vectorizedPosition.dist(otherVectorizedPosition) < other.radius * 2 + radius * 2;
            return isFrameColliding || isVectorColliding;
        }
        return false;
    }

    /**
     * Handles collision detection and resolution between the current PhysicSphere
     * instance and the provided PhysicSphere instance.
     * See:
     *  - Momentum conservation in angle collisions between two spherical bodies:
     *      <a href="https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html">here</a>
     *  - Elastic collision and exchange of momentum between two bodies with different masses:
     *      <a href="https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution">here</a>
     *  - TODO: add rotation to the equation (check
     *  <a href="https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm">here</a>).
     * ISSUE: don't know how to handle rotation of objects yet
     * <i>Are you insured?</i>
     *
     * @param other The other sphere involved in the collision.
     */
    void collideWith(PhysicSphere other) {
        if (isCollidingWith(other)) {
            if (CollisionIndex.tryLock(this.index, other.index)) {
                try {
                    PVector impulseSelf = getNormalVector(velocity, position, other.position);//selfImpulseVector & v_imp_1 are swapped
                    PVector impulseOther = getNormalVector(other.velocity, other.position, position);

                    PVector residualVelocityOther = velocity.copy();
                    residualVelocityOther.sub(impulseOther);

                    PVector residualVelocitySelf = other.velocity.copy();
                    residualVelocitySelf.sub(impulseSelf);

                    PVector impactVelocitySelf = velocity.copy();
                    PVector impactVelocityOther;

                    impactVelocityOther = new PVector(impactVelocitySelf.mag() * (residualVelocitySelf.x / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.y / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.z / residualVelocitySelf.mag()));
                    impactVelocitySelf = new PVector(impactVelocityOther.mag() * (residualVelocityOther.x / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.y / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.z / residualVelocityOther.mag()));

                    PVector momentumSelf = impactVelocitySelf.copy();
                    momentumSelf.mult(mass);

                    PVector momentumOther = impactVelocityOther.copy();
                    momentumOther.mult(other.mass);

                    PVector totalMomentum = momentumSelf.copy();
                    totalMomentum.add(momentumOther);

                    PVector bounceVelocitySelf = impactVelocitySelf.copy();
                    bounceVelocitySelf.mult(bounciness);
                    PVector bounceVelocityOther = impactVelocityOther.copy();
                    bounceVelocityOther.mult(bounciness);

                    //b_part => (((1-bounciness)(v_1_i-v_2_i)+v_1_f)*other.mass)/mass
                    PVector restitutionDelta = bounceVelocitySelf.copy();
                    restitutionDelta.sub(bounceVelocityOther);

                    //v_1_f => (v_1_i*mass+v_2_i*other.mass-(1-bounciness)*(v_1_i-v_2_i))/(mass+1)
                    PVector finalVelocitySelf = totalMomentum.copy();
                    finalVelocitySelf.sub(restitutionDelta);

                    finalVelocitySelf.div(mass + 1.0f);

                    //v_2_f => (1-bounciness)*(v_1_i-v_2_i)+v_1_f
                    PVector finalVelocityOther = finalVelocitySelf.copy();
                    finalVelocityOther.add(restitutionDelta);

                    if (!Float.isNaN(finalVelocitySelf.x + finalVelocitySelf.y + finalVelocitySelf.z)) {
                        velocity = finalVelocitySelf;
                    }

                    if (!Float.isNaN(finalVelocityOther.x + finalVelocityOther.y + finalVelocityOther.z)) {
                        other.velocity = finalVelocityOther;
                    }

                    correctClipping(other);
                }
                finally {
                    CollisionIndex.unlock(this.index, other.index);
                }
            }
        }
    }

    /**
     * Attempts to correct some of the clipping issues.
     * Note: very unoptimal.
     * @param other Other sphere involved in the collision.
     */
    private void correctClipping(PhysicSphere other) {
        PVector t_corrector = velocity.copy();
        t_corrector.sub(getNormalVector(velocity, position, other.position));
        t_corrector.mult(.1f);
        int sc = 0;
        while (isCollidingWith(other) && sc < 10000) {
            position.add(t_corrector);
            sc++;
        }
    }

    /**
     * Applies gravity forces (provided they are enabled) to a sphere as well as all the
     * others.
     * Note: this part accesses other spheres without going through the
     * thread structures, as the movement from one frame to the other should be negligible.
     * <i>I think I'm falling for you.</i>
     * @param others List of all the other spheres.
     */
    void applyAttraction(ArrayList<PhysicSphere> others)
    {
        PVector final_acc = new PVector(0, 0, 0);
        PVector t_acc = new PVector(0, 0, 0);
        for (PhysicSphere other : others) {
            collideWith(other);
            if (other.index != index) {
                if (gravityEnabled) {
                    float small_g_factor = -other.mass * GravityCollisionApp.G;
                    small_g_factor /= other.position.dist(position) * other.position.dist(position);
                    PVector small_g = position.copy();
                    small_g.sub(other.position);
                    small_g.mult(small_g_factor);
                    t_acc = small_g.copy();
                    t_acc.mult(mass);
                    final_acc.add(small_g);
                }
            }
            if (!Float.isNaN(t_acc.x + t_acc.y + t_acc.z)) {
                acceleration = final_acc.copy();
            }
        }
    }

    /**
     * Updates the position of the sphere.
     * <i>Take care of them.</i>
     */
    void update() {
        float targetPhysicsFPS = GravityCollisionApp.targetPhysicsFPS;
        if (targetPhysicsFPS < 60.0f) {
            int frameInterval = Math.round(60.0f / targetPhysicsFPS);
            if (FRAMES % frameInterval == 0) {
                updatePhysics();
            }
        } else {
            int physicsStepsThisFrame = Math.round(targetPhysicsFPS / 60.0f);
            physicsStepsThisFrame = Math.max(1, Math.min(physicsStepsThisFrame, 10));

            for (int i = 0; i < physicsStepsThisFrame; i++) {
                updatePhysics();
            }
        }
    }

    /**
     * Updates the position and the velocity of the sphere.
     * <i>I'm not defying the laws of physics - merely updating them.</i>
     */
    private void updatePhysics() {
        prevPos.add(position.copy());
        velocity.add(acceleration);
        if (boundsEnabled) {
            if (position.x <= 0 && velocity.x < 0) {
                velocity.x = -velocity.x * bounciness;
            } else if (position.x >= MAX_WIDTH && velocity.x > 0) {
                velocity.x = -velocity.x * bounciness;
            }
            if (position.y <= 0 && velocity.y < 0) {
                velocity.y = -velocity.y * bounciness;
            } else if (position.y >= MAX_HEIGHT && velocity.y > 0) {
                velocity.y = -velocity.y * bounciness;
            }
            if (position.z <= 0 && velocity.z < 0) {
                velocity.z = -velocity.z * bounciness;
            } else if (position.z >= MAX_DEPTH && velocity.z > 0) {
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
        app.fill(255 - app.red(sphereColor), 255 - app.green(sphereColor), 255 - app.blue(sphereColor));
        app.textSize(radius * 3);
        if (drawNames) {
            app.text((char) (index + 65), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (position.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (position.y + radius) / MAX_HEIGHT) + 100f, position.z + radius * 2f);
        }
        if (drawWeights) {
            app.text(floor(mass * 100), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (position.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (position.y + radius) / MAX_HEIGHT), position.z + radius * 2);
        }
        app.noFill();
        app.beginShape();
        app.curveVertex(position.x, position.y, position.z);
        app.strokeCap(SQUARE);
        if (drawTrails) {
            // Note: Processing's way of drawing strokes gives them no depth on the Z axis, which makes them look flat when the balls turn at sharp angles or face slightly away from the camera.
            for (int i = !prevPos.isEmpty() ? prevPos.size() - 1 : 0; i > (prevPos.size() > 20 ? prevPos.size() - 20 : 0); i--) {
                app.stroke(sphereColor, lerp(255f, 25f, ((float) (prevPos.size() < 20 ? i : prevPos.size() - i)) / (Math.min(prevPos.size(), 20))));
                app.strokeWeight(lerp(0, radius * 2, lerp(1.0f, 0, ((float) (prevPos.size() - i)) / (Math.min(prevPos.size(), 20)))));
                app.curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z);
            }
        }
        app.endShape();

        if (index >= 0 && drawArrows) {
            drawArrow(position.x, position.y, position.z, radius, velocity);
        }
    }
}
