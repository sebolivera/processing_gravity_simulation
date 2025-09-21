void nullifyPVectorNaN(PVector inputVect) {//Removes all possible miscalclations from a vector by setting NaN values to 0. It is a patchowork until I figure out a way to make it better.
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

void correctPVectorNaN(PVector input_vect, ArrayList<PVector> initial_vects) {//Replaces the value of a vector by a previous value picked from the array I used to trace the tails. It shouldn't happen much but it helps in reducing the clipping when too many spheres are agglutinated.
  for (int i = initial_vects.size()-1; i>=0; i--) {
    PVector initial_vect = initial_vects.get(i);
    if (Float.isNaN(input_vect.x)) {
      input_vect.x = initial_vect.x;
    }
    if (Float.isNaN(input_vect.y)) {
      input_vect.y = initial_vect.y;
    }
    if (Float.isNaN(input_vect.z)) {
      input_vect.z = initial_vect.z;
    }
  }
}
