package model;

import java.util.ArrayList;

/**
 * Thread wrapper for batches of sphere objects
 * Spheres are distributed as evenly as possible in the threads so that the balancing is somewhat reasonable.
 */
public class SphereBatchThread extends Thread {
    ArrayList<Integer> objectIndexList;
    ArrayList<PhysicSphere> colliderList;
    ArrayList<Integer> indexes = new ArrayList<>();

    public SphereBatchThread(ArrayList<Integer> objectIndexList,
                             ArrayList<PhysicSphere> colliderList) {

        this.objectIndexList = new ArrayList<>(objectIndexList);
        this.colliderList = new ArrayList<>(colliderList);
        this.indexes.addAll(
                this.colliderList.stream()
                        .map(ps -> ps.index)
                        .toList()
        );
    }

    /**
     * Add an object index to the list of objects to be processed.
     * @param objIdx Object index.
     * <i>One more to add to my collection.</i>
     */
    public void addToObjects(int objIdx) {
        objectIndexList.add(objIdx);
    }

    @Override
    public void run() {
        for (Integer integer : objectIndexList) {
            colliderList.get(integer).applyAttraction(colliderList);
            colliderList.get(integer).update();
        }
    }
}
