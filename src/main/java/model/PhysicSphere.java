package model;

import app.GravityCollisionApp;
import kotlin.Pair;
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
    int c;
    PVector pos;
    PVector vel;
    ArrayList<PVector> prevPos = new ArrayList<PVector>();
    int max_speed = 5;
    float radius;
    float mass;
    float bounciness;
    int index;
    public PVector acc;

    /**
     * Overload for normal bounciness sphere.
     * TODO: Switch to builder pattern, this is going to be a nightmare soon enough.
     * @param t_index Sphere index.
     * @param t_c Sphere color.
     * @param t_pos Sphere initial position.
     * @param t_vel Sphere initial velocity.
     * @param t_radius Sphere radius.
     * @param t_mass Sphere mass.
     */
    PhysicSphere(int t_index, int t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass) {//Bounciness is optional and has been known to cause some issues when too many spheres are colliding, so it is 1 by default (perfect bounciness)
        index = t_index;
        c = t_c;
        pos = t_pos;
        vel = t_vel;
        radius = t_radius * 2;
        mass = t_mass;
        acc = new PVector(0.0f, 0.0f, 0.0f);
        bounciness = 1.0f;
    }

    /**
     * Creates an instance of a sphere.
     * @param t_index Sphere index.
     * @param t_c Sphere color.
     * @param t_pos Sphere initial position.
     * @param t_vel Sphere initial velocity.
     * @param t_radius Sphere radius.
     * @param t_mass Sphere mass.
     * @param t_bounciness Sphere bounciness.
     */
    PhysicSphere(int t_index, int t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass, float t_bounciness) {
        index = t_index;
        c = t_c;
        pos = t_pos;
        vel = t_vel;
        radius = t_radius * 2;
        mass = t_mass;
        acc = new PVector(0.0f, 0.0f, 0.0f);
        bounciness = t_bounciness;
    }


    /**
     * Drawing an arrow proved itself to be quite the challenge. I took inspiration from
     * <a href="https://forum.processing.org/one/topic/drawing-an-arrow.html">this</a> and
     * tweaked it a bit.
     * @param cx X-coordinate of the arrow's origin.
     * @param cy Y-coordinate of the arrow's origin.
     * @param cz Z-coordinate of the arrow's origin.
     * @param len Scalar applied to the length of the arrow.
     * @param dest Target direction, expressed as a PVector.
     */
    void drawArrow(float cx, float cy, float cz, float len, PVector dest) {//
        pushMatrix();
        strokeWeight(radius / 2);
        translate(cx, cy, cz);
        dest = dest.copy();
        dest.normalize();
        dest.mult(len * dest.mag() * 10);

        stroke(255 - red(c), 255 - green(c), 255 - blue(c));
        line(0, 0, 0, dest.x, dest.y, dest.z);
        fill(255 - red(c), 255 - green(c), 255 - blue(c));
        float w = radius / 2;
        float h = radius;
        translate(dest.x, dest.y, dest.z);
        noStroke();
        beginShape();
        vertex(-w, -w, -w);
        vertex(w, -w, -w);
        vertex(h * dest.x / 100, h * dest.y / 100, h * dest.z / 100);
        endShape();
        beginShape();
        vertex(w, -w, -w);
        vertex(w, w, -w);
        vertex(h * dest.x / 100, h * dest.y / 100, h * dest.z / 100);
        endShape();

        beginShape();
        vertex(w, w, -w);
        vertex(-w, w, -w);
        vertex(h * dest.x / 100, h * dest.y / 100, h * dest.z / 100);
        endShape();

        beginShape();
        vertex(-w, w, -w);
        vertex(-w, -w, -w);
        vertex(h * dest.x / 100, h * dest.y / 100, h * dest.z / 100);
        endShape();

        popMatrix();
    }


    /**
     * Checks whether one given sphere is colliding with the current instance.
     * <i>Are you hitting on me?</i>
     * @param other Another instance of a sphere.
     * @return {@code true} if the spheres are colliding.
     */
    boolean isCollidingWith(PhysicSphere other) {
        if (index != other.index) {
            boolean isFrameColliding = other.pos.dist(pos) < other.radius * 2 + radius * 2;
            PVector vectorized_position = pos.copy();
            vectorized_position.dot(vel);
            PVector other_vectorized_position = other.pos.copy();
            other_vectorized_position.dot(other.vel);
            boolean isVectorColliding = vectorized_position.dist(other_vectorized_position) < other.radius * 2 + radius * 2;
            return isFrameColliding || isVectorColliding;//we account both for the current frame and the next one in case of high speeds
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
        Pair<Integer, Integer> duo = new Pair<Integer, Integer>(index, other.index);
        if (isCollidingWith(other)) {
            if (CollisionIndex.tryLock(this.index, other.index)) {
                try {
                    //safety copies of the velocities in case of large clumping of objects
                    // Cumulative implementation of angle collisions and massed elastic collisions
                    // see:
                    // - Momentum conservation in angle collisions between two spherical bodies : https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html
                    // - Elastic collision and exchange of momentum between two bodies with different masses : https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution
                    // - TODO: add rotation to the equation (check https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm). ISSUE: don't know how to get rotation of object in processing 3

                    PVector v_imp_2 = getNormalVector(vel, pos, other.pos);//v_imp_2 & v_imp_1 are swapped
                    PVector v_imp_1 = getNormalVector(other.vel, other.pos, pos);

                    // Apply the normal vectors to the velocity

                    PVector v_normal_1 = vel.copy();
                    v_normal_1.sub(v_imp_1);

                    PVector v_normal_2 = other.vel.copy();
                    v_normal_2.sub(v_imp_2);

                    //elastic collision part (or how to account for mass)

                    // apply the normals to the movement vectors by accounting for the mass (see second link)
                    PVector v_1_i = vel.copy();
                    PVector v_2_i = other.vel.copy();

                    //Takes the normal impulse vector and applies it to the opposite force's magnitude. Since I couldn't find a simulation where accounted for both angle as well as mass, I had to improvise
                    v_2_i = new PVector(v_1_i.mag() * (v_normal_2.x / v_normal_2.mag()), v_1_i.mag() * (v_normal_2.y / v_normal_2.mag()), v_1_i.mag() * (v_normal_2.z / v_normal_2.mag()));
                    v_1_i = new PVector(v_2_i.mag() * (v_normal_1.x / v_normal_1.mag()), v_2_i.mag() * (v_normal_1.y / v_normal_1.mag()), v_2_i.mag() * (v_normal_1.z / v_normal_1.mag()));

                    PVector m1v1i = v_1_i.copy();
                    m1v1i.mult(mass);//Is what is recommended in the provided link and corroborated by wikipedia & wolfram, but some mass transfers don't seem to conserve the proper amount of energyswap with m1v1i.mult(other.mass/mass); in case of unexpected energy transfer?

                    PVector m2v2i = v_2_i.copy();
                    m2v2i.mult(other.mass);

                    PVector a_part = m1v1i.copy();
                    a_part.add(m2v2i);

                    PVector b_v_1_i = v_1_i.copy();
                    b_v_1_i.mult(bounciness);
                    PVector b_v_2_i = v_2_i.copy();
                    b_v_2_i.mult(bounciness);

                    //b_part => (((1-bounciness)(v_1_i-v_2_i)+v_1_f)*other.mass)/mass
                    PVector b_part = b_v_1_i.copy();
                    b_part.sub(b_v_2_i);

                    //v_1_f => (v_1_i*mass+v_2_i*other.mass-(1-bounciness)*(v_1_i-v_2_i))/(mass+1)
                    PVector v_1_f = a_part.copy();
                    v_1_f.sub(b_part);

                    v_1_f.div(mass + 1.0f);

                    //v_2_f => (1-bounciness)*(v_1_i-v_2_i)+v_1_f
                    PVector v_2_f = v_1_f.copy();
                    v_2_f.add(b_part);

                    if (!Float.isNaN(v_1_f.x + v_1_f.y + v_1_f.z)) {
                        vel = v_1_f;
                    }

                    if (!Float.isNaN(v_2_f.x + v_2_f.y + v_2_f.z)) {
                        other.vel = v_2_f;
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
        PVector t_corrector = vel.copy();
        t_corrector.sub(getNormalVector(vel, pos, other.pos));
        t_corrector.mult(.1f);
        int sc = 0;
        while (isCollidingWith(other) && sc < 10000) {
            pos.add(t_corrector);
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
                if (ENABLE_GRAVITY) {
                    float small_g_factor = -other.mass * GravityCollisionApp.G;
                    small_g_factor /= other.pos.dist(pos) * other.pos.dist(pos);
                    PVector small_g = pos.copy();
                    small_g.sub(other.pos);
                    small_g.mult(small_g_factor);
                    t_acc = small_g.copy();
                    t_acc.mult(mass);
                    final_acc.add(small_g);
                }
            }
            if (!Float.isNaN(t_acc.x + t_acc.y + t_acc.z)) {
                acc = final_acc.copy();
            }
        }
    }

    /**
     * Updates the position of the sphere.
     */
    void update() {
        if (FRAMES % (GLOBAL_SPEED + 1) == 0) {
            prevPos.add(pos.copy());
            vel.add(acc);
            if (ENABLE_BOUNDS) {
                if (pos.x <= 0 && vel.x < 0) {
                    vel.x = -vel.x * bounciness;
                } else if (pos.x >= MAX_WIDTH && vel.x > 0) {
                    vel.x = -vel.x * bounciness;
                }
                if (pos.y <= 0 && vel.y < 0) {
                    vel.y = -vel.y * bounciness;
                } else if (pos.y >= MAX_HEIGHT && vel.y > 0) {
                    vel.y = -vel.y * bounciness;
                }
                if (pos.z <= 0 && vel.z < 0) {
                    vel.z = -vel.z * bounciness;
                } else if (pos.z >= MAX_DEPTH && vel.z > 0) {
                    vel.z = -vel.z * bounciness;
                }
            }
            nullifyPVectorNaN(vel);
            // Note: there *might* be a chance for spheres to get trapped in-between two
            // bouncing spheres that apply opposite forces, which will result in them
            // over-correcting their velocities. This prevents an impossible velocity from
            // being applied to a sphere, which allows them to clip a little bit, but
            // ultimately prevents both crashes and excessive clipping.
            pos.add(vel);
            correctPVectorNaN(pos, prevPos);
            // Note: Prevents the positions from being altered too much from fast-hitting
            // spheres. While very rare, instances where one sphere gets knocked off-screen
            // tend to crash the simulation due to the gravitational forces being skewed
            // toward it after a while.
        }
    }

    /**
     * Draws a sphere according to its position radius, color, index (name) and tail effect.
     */
    void display() {
        pushMatrix();
        translate(pos.x, pos.y, pos.z);
        noStroke();
        fill(c, 200);
        sphere(radius * 2);
        popMatrix();
        fill(255 - red(c), 255 - green(c), 255 - blue(c));
        textSize(radius * 3);
        if (DRAW_NAMES) {
            text((char) (index + 65), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (pos.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (pos.y + radius) / MAX_HEIGHT) + 100f, pos.z + radius * 2f);
            // index+65 will print ascii characters starting at 'A'. I am aware that it won't be able to print some of them, but this mostly for debugging.
        }
        if (DRAW_WEIGHTS) {
            text(floor(mass * 100), lerp(MAX_WIDTH * 0.05f, MAX_WIDTH * 0.95f, (pos.x - radius) / MAX_WIDTH), lerp(MAX_HEIGHT * 0.05f, MAX_HEIGHT * 0.95f, (pos.y + radius) / MAX_HEIGHT), pos.z + radius * 2);
        }
        noFill();
        beginShape();
        curveVertex(pos.x, pos.y, pos.z);
        strokeCap(SQUARE);
        if (DRAW_TRAILS) {//Processing's way of drawing strokes gives them no depth on the Z axis, which makes them look flat when the balls turn at sharp angles or face slightly away from the camera.
            for (int i = !prevPos.isEmpty() ? prevPos.size() - 1 : 0; i > (prevPos.size() > 20 ? prevPos.size() - 20 : 0); i--) {
                stroke(c, lerp(255f, 25f, ((float) (prevPos.size() < 20 ? i : prevPos.size() - i)) / (Math.min(prevPos.size(), 20))));
                strokeWeight(lerp(0, radius * 2, lerp(1.0f, 0, ((float) (prevPos.size() - i)) / (Math.min(prevPos.size(), 20)))));
                curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z);
            }
        }
        endShape();

        if (index >= 0 && DRAW_ARROWS) {
            drawArrow(pos.x, pos.y, pos.z, radius, vel);
        }
    }
}