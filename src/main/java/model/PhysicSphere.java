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
public class PhysicSphere extends PApplet {
    float MAX_WIDTH = 1000.0f;
    float MAX_HEIGHT = 1000.0f;
    float MAX_DEPTH = 1000.0f;
    int sphereColor;
    public PVector position;
    PVector velocity;
    ArrayList<PVector> prevPos = new ArrayList<>();
    int maxSpeed = 5; // TODO: implement this.
    float radius;
    float mass;
    float bounciness;
    int index;
    public PVector acceleration;

    /**
     * Overload for normal bounciness sphere.
     * TODO: Switch to builder pattern, this is going to be a nightmare soon enough.
     * @param index Sphere index.
     * @param sphereColor Sphere color.
     * @param position Sphere initial position.
     * @param velocity Sphere initial velocity.
     * @param radius Sphere radius.
     * @param mass Sphere mass.
     */
    public PhysicSphere(int index, int sphereColor, PVector position, PVector velocity, float radius, float mass) {//Bounciness is optional and has been known to cause some issues when too many spheres are colliding, so it is 1 by default (perfect bounciness)
        this.index = index;
        this.sphereColor = sphereColor;
        this.position = position;
        this.velocity = velocity;
        this.radius = radius * 2;
        this.mass = mass;
        this.acceleration = new PVector(0.0f, 0.0f, 0.0f);
        this.bounciness = 1.0f;
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
        pushMatrix();
        strokeWeight(radius / 2);
        translate(originX, originY, originZ);
        targetDirection = targetDirection.copy();
        targetDirection.normalize();
        targetDirection.mult(lengthScalar * targetDirection.mag() * 10);

        stroke(255 - red(sphereColor), 255 - green(sphereColor), 255 - blue(sphereColor));
        line(0, 0, 0, targetDirection.x, targetDirection.y, targetDirection.z);
        fill(255 - red(sphereColor), 255 - green(sphereColor), 255 - blue(sphereColor));
        float halfBaseSize = radius / 2;
        float tipLength = radius;
        translate(targetDirection.x, targetDirection.y, targetDirection.z);
        noStroke();
        beginShape();
        vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        endShape();
        beginShape();
        vertex(halfBaseSize, -halfBaseSize, -halfBaseSize);
        vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        endShape();

        beginShape();
        vertex(halfBaseSize, halfBaseSize, -halfBaseSize);
        vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        endShape();

        beginShape();
        vertex(-halfBaseSize, halfBaseSize, -halfBaseSize);
        vertex(-halfBaseSize, -halfBaseSize, -halfBaseSize);
        vertex(tipLength * targetDirection.x / 100, tipLength * targetDirection.y / 100, tipLength * targetDirection.z / 100);
        endShape();

        popMatrix();
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
     * <i>Are you insured?</i>
     *
     * @param other The other sphere involved in the collision.
     */
    void collideWith(PhysicSphere other) {
        if (isCollidingWith(other)) {
            if (CollisionIndex.tryLock(this.index, other.index)) {
                try {
                    // Safety copies of the velocities in case of large clumping of objects
                    // Cumulative implementation of angle collisions and massed elastic collisions
                    // See:
                    // - Momentum conservation in angle collisions between two spherical bodies : https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html
                    // - Elastic collision and exchange of momentum between two bodies with different masses : https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution
                    // - TODO: add rotation to the equation (check https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm). ISSUE: don't know how to get rotation of object in processing 3

                    PVector impulseSelf = getNormalVector(velocity, position, other.position);//selfImpulseVector & v_imp_1 are swapped
                    PVector impulseOther = getNormalVector(other.velocity, other.position, position);

                    // Apply the normal vectors to the velocity.

                    PVector residualVelocityOther = velocity.copy();
                    residualVelocityOther.sub(impulseOther);

                    PVector residualVelocitySelf = other.velocity.copy();
                    residualVelocitySelf.sub(impulseSelf);

                    //elastic collision part (or how to account for mass)

                    // apply the normals to the movement vectors by accounting for the mass (see second link)
                    PVector impactVelocitySelf = velocity.copy();
                    PVector impactVelocityOther;

                    //Takes the normal impulse vector and applies it to the opposite force's magnitude. Since I couldn't find a simulation where accounted for both angle as well as mass, I had to improvise
                    impactVelocityOther = new PVector(impactVelocitySelf.mag() * (residualVelocitySelf.x / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.y / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.z / residualVelocitySelf.mag()));
                    impactVelocitySelf = new PVector(impactVelocityOther.mag() * (residualVelocityOther.x / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.y / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.z / residualVelocityOther.mag()));

                    PVector momentumSelf = impactVelocitySelf.copy();
                    momentumSelf.mult(mass);//Is what is recommended in the provided link and corroborated by wikipedia & wolfram, but some mass transfers don't seem to conserve the proper amount of energyswap with m1v1i.mult(other.mass/mass); in case of unexpected energy transfer?

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

    void correctClipping(PhysicSphere other) {
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
     */
    void update() {
        if (FRAMES % (GLOBAL_SPEED + 1) == 0) {
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
            // Note: there *might* be a chance for spheres to get trapped in-between two
            // bouncing spheres that apply opposite forces, which will result in them
            // over-correcting their velocities. This prevents an impossible velocity from
            // being applied to a sphere, which allows them to clip a little bit, but
            // ultimately prevents both crashes and excessive clipping.
            position.add(velocity);
            correctPVectorNaN(position, prevPos);
            // Note: Prevents the positions from being altered too much from fast-hitting
            // spheres. While very rare, instances where one sphere gets knocked off-screen
            // tend to crash the simulation due to the gravitational forces being skewed
            // toward it after a while.
        }
    }

    /**
     * Draws a sphere according to its position radius, color, index (name) and tail effect.
     */
    public void display() {
        pushMatrix();
        translate(position.x, position.y, position.z);
        noStroke();
        fill(sphereColor, 200);
        sphere(radius * 2);
        popMatrix();
        fill(255 - red(sphereColor), 255 - green(sphereColor), 255 - blue(sphereColor));
        textSize(radius * 3);
        if (drawNames) {
            text((char) (index + 65), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (position.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (position.y + radius) / MAX_HEIGHT) + 100f, position.z + radius * 2f);
            // index+65 will print ascii characters starting at 'A'. I am aware that it won't be able to print some of them, but this mostly for debugging.
        }
        if (drawWeights) {
            text(floor(mass * 100), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (position.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (position.y + radius) / MAX_HEIGHT), position.z + radius * 2);
        }
        noFill();
        beginShape();
        curveVertex(position.x, position.y, position.z);
        strokeCap(SQUARE);
        if (drawTrails) {//Processing's way of drawing strokes gives them no depth on the Z axis, which makes them look flat when the balls turn at sharp angles or face slightly away from the camera.
            for (int i = !prevPos.isEmpty() ? prevPos.size() - 1 : 0; i > (prevPos.size() > 20 ? prevPos.size() - 20 : 0); i--) {
                stroke(sphereColor, lerp(255f, 25f, ((float) (prevPos.size() < 20 ? i : prevPos.size() - i)) / (Math.min(prevPos.size(), 20))));
                strokeWeight(lerp(0, radius * 2, lerp(1.0f, 0, ((float) (prevPos.size() - i)) / (Math.min(prevPos.size(), 20)))));
                curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z);
            }
        }
        endShape();

        if (index >= 0 && drawArrows) {
            drawArrow(position.x, position.y, position.z, radius, velocity);
        }
    }
}
