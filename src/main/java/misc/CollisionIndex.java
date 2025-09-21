package misc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents threads from accessing the same sphere at once.
 * <i>Prevents them from touching each other's balls, if you will.</i>
 */
public final class CollisionIndex {
    /**
     * Holds a pair of colliding spheres.
     */
    private static final Set<Map.Entry<Integer, Integer>> LOCKS = ConcurrentHashMap.newKeySet();

    /**
     * <i>NO TOUCHY.</i>
     */
    private CollisionIndex() {
    }

    /**
     * Normalizes pairs so that the lower one always comes first.
     * <i>One of them is always lower.</i>
     *
     * @param sphere1 First sphere.
     * @param sphere2 Second sphere.
     * @return A pair of spheres, with the lower value one first.
     */
    private static Map.Entry<Integer, Integer> key(final int sphere1, final int sphere2) {
        return Map.entry(MathUtils.min(sphere1, sphere2), MathUtils.max(sphere1, sphere2));
    }

    /**
     * Attempts to get a lock on a pair of Spheres.
     * <i>Chastity belt.</i>
     *
     * @param sphere1 First sphere to lock.
     * @param sphere2 Second sphere to lock.
     * @return {@code true} if the pair wasn't already part of the set.
     */
    public static boolean tryLock(final int sphere1, final int sphere2) {
        return LOCKS.add(key(sphere1, sphere2));
    }

    /**
     * Releases the lock on a sphere pair.
     * <i>Please let go of my balls.</i>
     *
     * @param sphere1 Index of the first sphere to unlock.
     * @param sphere2 Index of the second sphere to unlock.
     */
    public static void unlock(final int sphere1, final int sphere2) {
        LOCKS.remove(key(sphere1, sphere2));
    }
}
