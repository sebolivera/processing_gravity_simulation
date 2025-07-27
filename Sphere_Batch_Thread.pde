class SphereBatchThread extends Thread {
  /* 
   * Thread wrapper for batches of sphere objects
   * Spheres are distributed as evenly as possible in the threads so that the balancing is somewhat reasonable. 
   */
  ArrayList<Integer> objIdxList;
  ArrayList<PhysicSphere> colliderList;
  ArrayList<Integer> indexes = new ArrayList<Integer>();

  SphereBatchThread(ArrayList<Integer> t_obj_idx_list, ArrayList<PhysicSphere> tColliderList) {
    objIdxList = new ArrayList<Integer>();
    t_obj_idx_list.forEach((n)-> objIdxList.add(n));
    colliderList = new ArrayList<PhysicSphere>();
    tColliderList.forEach((e)-> colliderList.add(e));
    for (int i = 0; i<colliderList.size(); i++)
    {
      indexes.add(colliderList.get(i).index);
    }
  }

  public void addToObjs(int objIdx)
  {
    objIdxList.add(objIdx);
  }

  @Override
    public void run() {
    for (int i = 0; i<objIdxList.size(); i++) {
      colliderList.get(objIdxList.get(i)).applyAttraction(colliderList);
      colliderList.get(objIdxList.get(i)).update();
    }
  }
}
