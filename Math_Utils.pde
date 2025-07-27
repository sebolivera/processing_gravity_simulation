import processing.core.PApplet;
import processing.core.PVector;

/**
 * Utility static class for mathematical operations.
 * <i>Here's where I store my mathematical mops, buckets, and bleach.</i>
 */
public static final class MathUtils extends PApplet {
    /**
     * <i>NO TOUCHY.</i>
     */
    private MathUtils() {}

    /**
     * Gets xyz angle between the direction of an object and the vector between it and the other ball.
     * Note: PVector.angleBetween function gave odd results in processing 3.
     * <i>Do you get my angle?</i>
     * @param velocity Velocity vector.
     * @param deltaVelocity Delta velocity vector.
     * @return The angle between the two vectors, expressed in radians.
     */
    public static float getXYZAngleForImpactVector (PVector velocity, PVector deltaVelocity) {
        PVector normalized_vel = velocity.copy();
        PVector delta = deltaVelocity.copy();
        normalized_vel.normalize();
        normalized_vel = new PVector(abs(normalized_vel.x), abs(normalized_vel.y), abs(normalized_vel.z));
        float numerator = delta.x*normalized_vel.x+delta.y*normalized_vel.y+delta.z*normalized_vel.z;
        float denominator = sqrt((delta.x+delta.y+delta.z)*(normalized_vel.x+normalized_vel.y+normalized_vel.z));
        return acos(numerator/denominator);
    }

    /**
     * Applies the angle between the direction of an object and the vector between it and the other ball to the
     * original vector.
     * <i>Relax, find your center.</i>
     * @param vect Original vector.
     * @param delta Delta vector.
     * @return The resulting vector, after applying the angle between the two vectors.
     */
    public static PVector getCenterVector (PVector vect, PVector delta)
    {
        PVector finalVect = vect.copy();
        finalVect.mult(getXYZAngleForImpactVector(vect, delta));
        return finalVect;
    }

    /**
     * Gets the angle between two colliding spheres and normalizes it.
     * <i>Other manners of rivers joining the sea are to be considered abnormal.</i>
     * @param pos1 First sphere position.
     * @param pos2 Second sphere position.
     * @return The angle between the two spheres, expressed in radians, normalized.
     */
    public static PVector getNormalizedDelta(PVector pos1, PVector pos2)
    {
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
     * @param velocity Velocity vector of the other object.
     * @param pos1 First object position.
     * @param pos2 Second object position.
     * @return The opposite impact vector, expressed in the direction of the other object's velocity.'
     */
    public static PVector getNormalVector(PVector velocity, PVector pos1, PVector pos2) {
        PVector delta = getNormalizedDelta(pos1, pos2);
        PVector v_final = getCenterVector(velocity, delta);//impact vector for the current object
        PVector xz = new PVector(pos2.x-pos1.x, 0, pos2.z-pos1.z);
        PVector xy = new PVector(pos2.x-pos1.x, pos2.y-pos1.y, 0);

        float angle_xz = PVector.angleBetween(xz, new PVector(0, 0, 1));
        float angle_xy = PVector.angleBetween(xy, new PVector(1, 0, 0));

        float impulse_x = v_final.mag()*sin(angle_xz)*cos(angle_xy);
        float impulse_y = v_final.mag()*sin(angle_xz)*sin(angle_xy);
        float impulse_z = v_final.mag()*cos(angle_xz);
        return new PVector (impulse_x, impulse_y, impulse_z);
    }

    /**
     * Ease out function for linear interpolation.
     * <i>Finesse your way out of this.</i>
     * @param startVal Start value.
     * @param endVal End value.
     * @param frame Current frame.
     * @param factor Factor of the easing.
     * @return The interpolated value, between the start and end values, at the current frame.
     */
    public static float easeOut(float startVal, float endVal, float frame, float factor) {
        return lerp(startVal, endVal, MathUtils.pow(1.0f-frame, factor));
    }
}
