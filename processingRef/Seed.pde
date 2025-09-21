public void seed(int amount)//Creates
{
  spheres = new ArrayList<>();
  threadedSpheres = new ArrayList<>();
  color randColor;
  for (int i = 0; i<amount; i++)
  {
    randColor = color(random(200)+55, random(200)+55, random(200)+55);
    float randX = random(1000.0);
    float randY = random(1000.0);
    float randZ = random(1000.0);
    float randR = random( 2.0+random(10.0));
    PVector t_pos = new PVector(randX, randY, randZ);
    for (int j = 0; j<spheres.size(); j++) {
      while (PVector.dist(t_pos, spheres.get(j).position)<randR)
      {
        randX = random(1000.0);
        randY = random(1000.0);
        randZ = random(1000.0);
        randR = random( 2+random(10.0));
        t_pos = new PVector(randX, randY, randZ);
      }
    }
    spheres.add(new PhysicSphere(i, randColor, new PVector(randX, randY, randZ), new PVector(1-random(5), 1-random(5), 1-random(5)), randR, 0.5+random(0.5)));
  }

  threadedSpheres = new ArrayList<SphereBatchThread>();

  ArrayList<Integer> spheresIdxBatch = new ArrayList<>();
  if (threadCount>amount)
  {
    for (int i = 0; i<amount; i++)
    {
      spheresIdxBatch.add(i);
    }
    threadedSpheres.add(new SphereBatchThread(spheresIdxBatch, spheres));
    spheresIdxBatch.clear();//Technically useless since the app will most likely crash due to over-allocation of objects in the first place, but every little bit helps, I guess.
  } else
  {
    int itemsPerThread = amount/threadCount;
    int globalIdx = 0;
    for (int i = 0; i<threadCount; i++)
    {
      for (int j = 0; j < itemsPerThread; j++)
      {
        spheresIdxBatch.add(globalIdx);
        globalIdx++;
      }
      threadedSpheres.add(new SphereBatchThread(spheresIdxBatch, spheres));
      spheresIdxBatch.clear();
    }
    if (amount%threadCount!=0)
    {
      int remainingObjs = (amount-globalIdx);
      for (int i = 0; i<remainingObjs; i++)
      {
        threadedSpheres.get(i).addToObjs(globalIdx);
        globalIdx++;
      }
      spheresIdxBatch.clear();
    }
  }
}
