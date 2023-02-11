class Sphere_Batch_Thread extends Thread {
  /* 
   * Thread wrapper for batches of sphere objects
   * Spheres are distributed as evenly as possible in the threads so that the balancing is somewhat reasonable. 
   */
  ArrayList<Integer> obj_idx_list;
  ArrayList<Physic_Sphere> collider_list;
  ArrayList<Integer> indexes = new ArrayList<Integer>();

  Sphere_Batch_Thread(ArrayList<Integer> t_obj_idx_list, ArrayList<Physic_Sphere> t_collider_list) {
    obj_idx_list = new ArrayList<Integer>();
    t_obj_idx_list.forEach((n)-> obj_idx_list.add(n));
    collider_list = new ArrayList<Physic_Sphere>();
    t_collider_list.forEach((e)-> collider_list.add(e));
    for (int i = 0; i<collider_list.size(); i++)
    {
      indexes.add(collider_list.get(i).index);
    }
  }

  public void add_to_objs(int obj_idx)
  {
    obj_idx_list.add(obj_idx);
  }

  @Override
    public void run() {
    for (int i = 0; i<obj_idx_list.size(); i++) {
      collider_list.get(obj_idx_list.get(i)).applyAttraction(collider_list);
      collider_list.get(obj_idx_list.get(i)).update();
    }
  }
}
