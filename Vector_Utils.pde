void nullifyPVectorNaN(PVector input_vect) {
  if (Float.isNaN(input_vect.x)) {
    input_vect.x = 0;
  }
  if (Float.isNaN(input_vect.y)) {
    input_vect.y = 0;
  }
  if (Float.isNaN(input_vect.z)) {
    input_vect.z = 0;
  }
}

void correctPVectorNaN(PVector input_vect, ArrayList<PVector> initial_vects) {
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
