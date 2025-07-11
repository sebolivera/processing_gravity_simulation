package misc;

import processing.core.PVector;

import java.util.ArrayList;

/**
 * Utility static class for Vectors.
 * <i>Implementations for Grus pending.</i>
 */
public class VectorUtils {
    /**
     * Fixes all NaN values in a PVector.
     * TODO: Find a viable fix.
     *
     * @param input_vect PVector instance to modify.
     */
    public static void nullifyPVectorNaN(PVector input_vect) {
        if (Float.isNaN(input_vect.x)) {
            input_vect.x = 0;
        }
        if (Float.isNaN(input_vect.y)) {
            input_vect.y = 0;
        }
        if (Float.isNaN(input_vect.z)) {
            input_vect.z = 0;
        }
    }

    /**
     * Corrects NaN values in the specified PVector by replacing them with values from a list
     * of previous PVectors. It iterates in reverse through the provided list to find a suitable
     * replacement for any NaN component in the input vector.
     * Helps in reducing clipping during large sphere agglomerates.
     *
     * @param input_vect PVector to fix.
     * @param initial_vects List of previous candidates to fix the values with.
     */
    public static void correctPVectorNaN(PVector input_vect, ArrayList<PVector> initial_vects) {
        for (int i = initial_vects.size()-1; i>=0; i--) {
            PVector initial_vect = initial_vects.get(i);
            if (Float.isNaN(input_vect.x)) {
                input_vect.x = initial_vect.x;
            }
            if (Float.isNaN(input_vect.y)) {
                input_vect.y = initial_vect.y;
            }
            if (Float.isNaN(input_vect.z)) {
                input_vect.z = initial_vect.z;
            }
        }
    }
}
