class SphereBatchThread extends Thread {
  /* 
   * Thread wrapper for batches of sphere objects
   * Spheres are distributed as evenly as possible in the threads so that the balancing is somewhat reasonable. 
   */
  ArrayList<Integer> objectIndexList;
  ArrayList<PhysicSphere> colliderList;
  ArrayList<Integer> indexes = new ArrayList<Integer>();

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

  public void addToObjs(int objIdx)
  {
    objectIndexList.add(objIdx);
  }

  @Override
    public void run() {
    for (int i = 0; i<objectIndexList.size(); i++) {
      colliderList.get(objectIndexList.get(i)).applyAttraction(colliderList);
      colliderList.get(objectIndexList.get(i)).update();
    }
  }
}
