package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread wrapper for batches of sphere objects Spheres are distributed as evenly as possible in the
 * threads so that the balancing is somewhat reasonable.
 */
public class SphereBatchThread extends Thread {
    private final List<Integer> objectIndexList;
    private final List<PhysicSphere> colliderList;

    public SphereBatchThread(
            final List<Integer> objectIndexListParam, final List<PhysicSphere> colliderListParam) {

        this.objectIndexList = new ArrayList<>(objectIndexListParam);
        this.colliderList = new ArrayList<>(colliderListParam);
    }

    /**
     * Add an object index to the list of objects to be processed.
     *
     * @param objIdx Object index. <i>One more to add to my collection.</i>
     */
    public void addToObjects(final int objIdx) {
        objectIndexList.add(objIdx);
    }

    /** Apply attraction to all objects in the batch. <i>RUN FORREST, RUN!</i> */
    @Override
    public void run() {
        for (final Integer objectIndex : objectIndexList) {
            colliderList.get(objectIndex).applyAttraction(colliderList);
            colliderList.get(objectIndex).update();
        }
    }
}
