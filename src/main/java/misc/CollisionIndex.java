package misc;

import kotlin.Pair;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents threads from accessing the same sphere at once.
 * <i>Prevents them from touching each other's balls, if you will.</i>
 */
public final class CollisionIndex {
    /**
     * Holds a pair of colliding spheres.
     * <i>They always come in two.</i>
     */
    private static final Set<Pair<Integer, Integer>> LOCKS = ConcurrentHashMap.newKeySet();

    /**
     * <i>NO TOUCHY.</i>
     */
    private CollisionIndex() {}

    /**
     * Normalizes pairs so that the lower one always comes first.
     * <i>One of them is always lower.</i>
     * @param i First sphere.
     * @param j Second sphere.
     * @return A pair of spheres, with the lower value one first.
     */
    private static Pair<Integer, Integer> key(int i, int j) {
        return new Pair<>(MathUtils.min(i, j), MathUtils.max(i, j));
    }

    /**
     * Attempts to get a lock on a pair of Spheres.
     * <i>I am not so certain about this chastity belt idea.</i>
     * @param i First sphere to lock.
     * @param j Second sphere to lock.
     * @return {@code true} if the pair wasn't already part of the set.
     */
    public static boolean tryLock(int i, int j) {
        return LOCKS.add(key(i, j));
    }

    /**
     * Releases the lock on a sphere pair.
     * <i>Please let go of my balls.</i>
     * @param i First sphere to unlock.
     * @param j Second sphere to unlock.
     */
    public static void unlock(int i, int j) {
        LOCKS.remove(key(i, j));
    }

}
