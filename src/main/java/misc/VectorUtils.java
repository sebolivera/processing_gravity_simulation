package misc;

import java.util.List;

import processing.core.PVector;

/** Utility static class for Vectors. <i>Implementations for Grus pending.</i> */
public final class VectorUtils {
    private VectorUtils() {}
    /**
     * Fixes all NaN values in a PVector. <i>Your naans are worthless to me.</i> todo: Find a viable
     * fix.
     *
     * @param inputVect PVector instance to modify.
     */
    public static void nullifyPVectorNaN(final PVector inputVect) {
        if (Float.isNaN(inputVect.x)) {
            inputVect.x = 0;
        }
        if (Float.isNaN(inputVect.y)) {
            inputVect.y = 0;
        }
        if (Float.isNaN(inputVect.z)) {
            inputVect.z = 0;
        }
    }

    /**
     * Corrects NaN values in the specified PVector by replacing them with values from a list of
     * previous PVectors. Iterates in reverse through the provided list to find a suitable
     * replacement for any NaN component in the input vector. Helps in reducing clipping during
     * large sphere agglomerates.
     *
     * @param inputVector PVector to fix.
     * @param initialVectors List of previous candidates to fix the values with.
     */
    public static void correctPVectorNaN(
            final PVector inputVector,
            final List<PVector> initialVectors
    ) {
        for (int i = initialVectors.size() - 1; i >= 0; i--) {
            final PVector initialVect = initialVectors.get(i);
            if (Float.isNaN(inputVector.x)) {
                inputVector.x = initialVect.x;
            }
            if (Float.isNaN(inputVector.y)) {
                inputVector.y = initialVect.y;
            }
            if (Float.isNaN(inputVector.z)) {
                inputVector.z = initialVect.z;
            }
        }
    }
}
