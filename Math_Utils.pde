
float getXYZAngleForImpactVector (PVector vel, PVector delta_vel) {
  //gets xyz angle between the direction of an object and the vector between it and the other ball. The PVector.angleBetween function gave odd results.
  PVector normalized_vel = vel.copy();
  PVector delta = delta_vel.copy();
  normalized_vel.normalize();
  normalized_vel = new PVector(abs(normalized_vel.x), abs(normalized_vel.y), abs(normalized_vel.z));
  float numerator = delta.x*normalized_vel.x+delta.y*normalized_vel.y+delta.z*normalized_vel.z;
  float denominator = sqrt((delta.x+delta.y+delta.z)*(normalized_vel.x+normalized_vel.y+normalized_vel.z));
  return acos(numerator/denominator);
}

PVector getCenterVector (PVector vect, PVector delta)//applies the angle to the original vector, which it gets from the normalized delta center-vector impact
{
  PVector finalVect = vect.copy();
  finalVect.mult(getXYZAngleForImpactVector(vect, delta));
  return finalVect;
}

PVector getNormalizedDelta(PVector pos_1, PVector pos_2)//gets the angle between two colliding spheres and normalizes it.
{
  PVector delta = pos_2.copy();
  delta.sub(pos_1);
  delta = new PVector(abs(delta.x), abs(delta.y), abs(delta.z));
  delta.normalize();
  return delta;
}

PVector getNormalVector(PVector vel, PVector pos_1, PVector pos_2) {//Takes two positions and a velocity, and sends back the opposite impact vector (impact vectors get transfered from the velocity of the other object).
  PVector delta = getNormalizedDelta(pos_1, pos_2);
  PVector v_final = getCenterVector(vel, delta);//impact vector for the current object
  PVector xz = new PVector(pos_2.x-pos_1.x, 0, pos_2.z-pos_1.z);
  PVector xy = new PVector(pos_2.x-pos_1.x, pos_2.y-pos_1.y, 0);

  float angle_xz = PVector.angleBetween(xz, new PVector(0, 0, 1));
  float angle_xy = PVector.angleBetween(xy, new PVector(1, 0, 0));

  float impulse_x = v_final.mag()*sin(angle_xz)*cos(angle_xy);
  float impulse_y = v_final.mag()*sin(angle_xz)*sin(angle_xy);
  float impulse_z = v_final.mag()*cos(angle_xz);
  return new PVector (impulse_x, impulse_y, impulse_z);
}


public static float easeOut(float a, float b, float f, float fac) {
  return lerp(a, b, (float)Math.pow(1.0-f, fac));
}
