public void seed(int amount)//Creates
{
  spheres = new ArrayList<>();
  threaded_spheres = new ArrayList<>();
  color rand_color;
  for (int i = 0; i<amount; i++)
  {
    rand_color = color(random(200)+55, random(200)+55, random(200)+55);
    float randX = random(600.0);
    float randY = random(600.0);
    float randZ = random(1200.0)-600.0;
    float randR = random( 2.0+random(10.0));
    PVector t_pos = new PVector(randX, randY, randZ);
    for (int j = 0; j<spheres.size(); j++) {
      while (PVector.dist(t_pos, spheres.get(j).pos)<randR)
      {
        randX = random(600.0);
        randY = random(600.0);
        randZ = random(1200.0)-600.0;
        randR = random( 2+random(10.0));
        t_pos = new PVector(randX, randY, randZ);
      }
    }
    spheres.add(new Physic_Sphere(i, rand_color, new PVector(randX, randY, randZ), new PVector(1-random(5), 1-random(5), 1-random(5)), randR, 0.5+random(0.5)));
  }

  threaded_spheres = new ArrayList<Sphere_Batch_Thread>();

  ArrayList<Integer> spheres_idx_batch = new ArrayList<>();
  if (THREAD_COUNT>amount)
  {
    for (int i = 0; i<amount; i++)
    {
      spheres_idx_batch.add(i);
    }
    threaded_spheres.add(new Sphere_Batch_Thread(spheres_idx_batch, spheres));
    spheres_idx_batch.clear();//Technically useless since the app will most likely crash due to over-allocation of objects in the first place, but every little bit helps, I guess.
  } else
  {
    int items_per_thread = amount/THREAD_COUNT;
    int global_idx = 0;
    for (int i = 0; i<THREAD_COUNT; i++)
    {
      for (int j = 0; j < items_per_thread; j++)
      {
        spheres_idx_batch.add(global_idx);
        global_idx++;
      }
      threaded_spheres.add(new Sphere_Batch_Thread(spheres_idx_batch, spheres));
      spheres_idx_batch.clear();
    }
    if (amount%THREAD_COUNT!=0)
    {
      int remaining_objs = (amount-global_idx);
      for (int i = 0; i<remaining_objs; i++)
      {
        threaded_spheres.get(i).add_to_objs(global_idx);
        global_idx++;
      }
      spheres_idx_batch.clear();
    }
  }
}
