package misc;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Utility static class for mathematical operations.
 * <i>Here's where I store my mathematical mops, buckets, and bleach.</i>
 */
public final class MathUtils extends PApplet {
    /**
     * <i>NO TOUCHY.</i>
     */
    private MathUtils() {
    }


    /**
     * Represents a functional interface for operations that take a {@code Float} value and perform an update or action
     * with it.
     * <i>A truly titanic function.</i>
     */
    public interface FloatFunction {
        void update(Float f);
    }

    /**
     * Gets xyz angle between the direction of an object and the vector between it and the other ball.
     * Note: PVector.angleBetween function gave odd results in processing 3.
     * <i>Do you get my angle?</i>
     *
     * @param velocity      Velocity vector.
     * @param deltaVelocity Delta velocity vector.
     * @return The angle between the two vectors, expressed in radians.
     */
    public static float getXYZAngleForImpactVector(final PVector velocity, final PVector deltaVelocity) {
        PVector normalizedVel = velocity.copy();
        PVector delta = deltaVelocity.copy();
        normalizedVel.normalize();
        normalizedVel = new PVector(abs(normalizedVel.x), abs(normalizedVel.y), abs(normalizedVel.z));
        float numerator = delta.x * normalizedVel.x + delta.y * normalizedVel.y + delta.z * normalizedVel.z;
        float denominator = sqrt((delta.x + delta.y + delta.z) * (normalizedVel.x + normalizedVel.y + normalizedVel.z));
        return acos(numerator / denominator);
    }

    /**
     * Applies the angle between the direction of an object and the vector between it and the other ball to the
     * original vector.
     * <i>Relax, find your center.</i>
     *
     * @param vect  Original vector.
     * @param delta Delta vector.
     * @return The resulting vector, after applying the angle between the two vectors.
     */
    public static PVector getCenterVector(final PVector vect, final PVector delta) {
        PVector finalVect = vect.copy();
        finalVect.mult(getXYZAngleForImpactVector(vect, delta));
        return finalVect;
    }

    /**
     * Gets the angle between two colliding spheres and normalizes it.
     * <i>Other manners of rivers joining the sea are to be considered abnormal.</i>
     *
     * @param pos1 First sphere position.
     * @param pos2 Second sphere position.
     * @return The angle between the two spheres, expressed in radians, normalized.
     */
    public static PVector getNormalizedDelta(final PVector pos1, final PVector pos2) {
        PVector delta = pos2.copy();
        delta.sub(pos1);
        delta = new PVector(abs(delta.x), abs(delta.y), abs(delta.z));
        delta.normalize();
        return delta;
    }

    /**
     * Takes two positions and a velocity and sends back the opposite impact vector (impact vectors get transferred
     * from the velocity of the other object).
     * <br/><br/>
     * <i>WHY CAN'T YOU JUST BE NORMAL?</i>
     *
     * @param velocity Velocity vector of the other object.
     * @param pos1     First object position.
     * @param pos2     Second object position.
     * @return The opposite impact vector, expressed in the direction of the other object's velocity.'
     */
    public static PVector getNormalVector(final PVector velocity, final PVector pos1, final PVector pos2) {
        PVector delta = getNormalizedDelta(pos1, pos2);
        PVector vFinal = getCenterVector(velocity, delta); //impact vector for the current object
        PVector xz = new PVector(pos2.x - pos1.x, 0, pos2.z - pos1.z);
        PVector xy = new PVector(pos2.x - pos1.x, pos2.y - pos1.y, 0);

        float angleXZ = PVector.angleBetween(xz, new PVector(0, 0, 1));
        float angleXY = PVector.angleBetween(xy, new PVector(1, 0, 0));

        float impulseX = vFinal.mag() * sin(angleXZ) * cos(angleXY);
        float impulseY = vFinal.mag() * sin(angleXZ) * sin(angleXY);
        float impulseZ = vFinal.mag() * cos(angleXZ);
        return new PVector(impulseX, impulseY, impulseZ);
    }
}
