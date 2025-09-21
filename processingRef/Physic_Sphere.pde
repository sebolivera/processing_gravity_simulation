ArrayList<Pair> collisionIndex = new ArrayList<Pair>();//locks pairs of colliding spheres in the event that they belong to different threads trying to calculate their collisions at the same time

class PhysicSphere {//Base class for the spheres
  /*
   * Core of the simulation. Each sphere has an update function that allows it to look at all other objects and apply their gravity to it, while they do the same for eachother
   * A sphere has position (3D vector), a radius (float>0), a velocity (3D vector), a mass (float>0), an acceleration (3D vector), a color (processing Color>=(255, 255, 255)), an index (int>=0) and a bounciness (0<=float<=1)
   */
  private static final float maxWidth = 1000.0;
  private static final float maxHeight = 1000.0;
  private static final float maxDepth = 1000.0;
  color sphereColor;
  PVector position;
  PVector velocity;
  ArrayList<PVector> prevPos = new ArrayList<PVector>();
  float radius;
  float mass;
  float bounciness;
  int index;
  public PVector acceleration;

  PhysicSphere(int tIndex, color tC, PVector tPos, PVector tVel, float tRadius, float tMass) {//Bounciness is optional, and has been known to cause some issues when too many spheres are colliding, so it is 1 by default (perfect bounciness)
    index = tIndex;
    sphereColor = tC;
    position = tPos;
    velocity = tVel;
    radius = tRadius*2;
    mass = tMass;
    acceleration = new PVector(0.0, 0.0, 0.0);
    bounciness = 1.0;
  }

  PhysicSphere(int tIndex, color tC, PVector tPos, PVector tVel, float tRadius, float tMass, float tBounciness) {
    index = tIndex;
    sphereColor = tC;
    position = tPos;
    velocity = tVel;
    radius = tRadius*2;
    mass = tMass;
    acceleration = new PVector(0.0, 0.0, 0.0);
    bounciness = tBounciness;
  }


  void drawArrow(float cx, float cy, float cz, float len, PVector dest)
  {//Drawing an arrow proved itself to be quite the challenge. I took inspiration from from https://forum.processing.org/one/topic/drawing-an-arrow.html and tweaked it a bit.
    pushMatrix();
    strokeWeight(radius/2);
    translate(cx, cy, cz);
    dest = dest.copy();
    dest.normalize();
    dest.mult(len*dest.mag()*10);

    stroke(255-red(sphereColor), 255-green(sphereColor), 255-blue(sphereColor));
    line(0, 0, 0, dest.x, dest.y, dest.z);
    fill(255-red(sphereColor), 255-green(sphereColor), 255-blue(sphereColor));
    float w = (float)radius/2;
    float h = (float)radius;
    translate(dest.x, dest.y, dest.z);
    noStroke();
    beginShape();
    vertex(-w, -w, -w);
    vertex( w, -w, -w);
    vertex(h*dest.x/100, h*dest.y/100, h*dest.z/100);
    endShape();
    beginShape();
    vertex( w, -w, -w);
    vertex( w, w, -w);
    vertex(h*dest.x/100, h*dest.y/100, h*dest.z/100);
    endShape();

    beginShape();
    vertex( w, w, -w);
    vertex(-w, w, -w);
    vertex(h*dest.x/100, h*dest.y/100, h*dest.z/100);
    endShape();

    beginShape();
    vertex(-w, w, -w);
    vertex(-w, -w, -w);
    vertex(h*dest.x/100, h*dest.y/100, h*dest.z/100);
    endShape();

    popMatrix();
  }


  boolean isCollidingWith(PhysicSphere other)
  {
    if (index!=other.index) {
      boolean isFrameColliding = other.position.dist(position) < other.radius*2 + radius*2;
      PVector vectorizedPosition = position.copy();
      vectorizedPosition.dot(velocity);
      PVector otherVectorizedPosition = other.position.copy();
      otherVectorizedPosition.dot(other.velocity);
      boolean isVectorColliding = vectorizedPosition.dist(otherVectorizedPosition) < other.radius*2 + radius*2;
      return isFrameColliding || isVectorColliding;//we account both for the current frame and the next one in case of high speeds
    }
    return false;
  }
    /**
     * Handles collision detection and resolution between the current PhysicSphere
     * instance and the provided PhysicSphere instance.
     * <i>Are you insured?</i>
     *
     * @param other The other sphere involved in the collision.
     */
    void collideWith(PhysicSphere other) {
        if (isCollidingWith(other)) {
            if (CollisionIndex.tryLock(this.index, other.index)) {
                try {
                    // Safety copies of the velocities in case of large clumping of objects
                    // Cumulative implementation of angle collisions and massed elastic collisions
                    // See:
                    // - Momentum conservation in angle collisions between two spherical bodies : https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html
                    // - Elastic collision and exchange of momentum between two bodies with different masses : https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution
                    // - TODO: add rotation to the equation (check https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm). ISSUE: don't know how to get rotation of object in processing 3

                    PVector impulseSelf = MathUtils.getNormalVector(velocity, position, other.position);//selfImpulseVector & v_imp_1 are swapped
                    PVector impulseOther = MathUtils.getNormalVector(other.velocity, other.position, position);

                    // Apply the normal vectors to the velocity.

                    PVector residualVelocityOther = velocity.copy();
                    residualVelocityOther.sub(impulseOther);

                    PVector residualVelocitySelf = other.velocity.copy();
                    residualVelocitySelf.sub(impulseSelf);

                    //elastic collision part (or how to account for mass)

                    // apply the normals to the movement vectors by accounting for the mass (see second link)
                    PVector impactVelocitySelf = velocity.copy();
                    PVector impactVelocityOther;

                    //Takes the normal impulse vector and applies it to the opposite force's magnitude. Since I couldn't find a simulation where accounted for both angle as well as mass, I had to improvise
                    impactVelocityOther = new PVector(impactVelocitySelf.mag() * (residualVelocitySelf.x / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.y / residualVelocitySelf.mag()), impactVelocitySelf.mag() * (residualVelocitySelf.z / residualVelocitySelf.mag()));
                    impactVelocitySelf = new PVector(impactVelocityOther.mag() * (residualVelocityOther.x / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.y / residualVelocityOther.mag()), impactVelocityOther.mag() * (residualVelocityOther.z / residualVelocityOther.mag()));

                    PVector momentumSelf = impactVelocitySelf.copy();
                    momentumSelf.mult(mass);//Is what is recommended in the provided link and corroborated by wikipedia & wolfram, but some mass transfers don't seem to conserve the proper amount of energyswap with m1v1i.mult(other.mass/mass); in case of unexpected energy transfer?

                    PVector momentumOther = impactVelocityOther.copy();
                    momentumOther.mult(other.mass);

                    PVector totalMomentum = momentumSelf.copy();
                    totalMomentum.add(momentumOther);

                    PVector bounceVelocitySelf = impactVelocitySelf.copy();
                    bounceVelocitySelf.mult(bounciness);
                    PVector bounceVelocityOther = impactVelocityOther.copy();
                    bounceVelocityOther.mult(bounciness);

                    //b_part => (((1-bounciness)(v_1_i-v_2_i)+v_1_f)*other.mass)/mass
                    PVector restitutionDelta = bounceVelocitySelf.copy();
                    restitutionDelta.sub(bounceVelocityOther);

                    //v_1_f => (v_1_i*mass+v_2_i*other.mass-(1-bounciness)*(v_1_i-v_2_i))/(mass+1)
                    PVector finalVelocitySelf = totalMomentum.copy();
                    finalVelocitySelf.sub(restitutionDelta);

                    finalVelocitySelf.div(mass + 1.0f);

                    //v_2_f => (1-bounciness)*(v_1_i-v_2_i)+v_1_f
                    PVector finalVelocityOther = finalVelocitySelf.copy();
                    finalVelocityOther.add(restitutionDelta);

                    if (!Float.isNaN(finalVelocitySelf.x + finalVelocitySelf.y + finalVelocitySelf.z)) {
                        velocity = finalVelocitySelf;
                    }

                    if (!Float.isNaN(finalVelocityOther.x + finalVelocityOther.y + finalVelocityOther.z)) {
                        other.velocity = finalVelocityOther;
                    }

                    correctClipping(other);
                }
                finally {
                    CollisionIndex.unlock(this.index, other.index);
                }
            }
        }
    }

  void correctClipping(PhysicSphere other)
  {
    PVector tCorrector = velocity.copy();
    tCorrector.sub(MathUtils.getNormalVector(velocity, position, other.position));
    tCorrector.mult(.1);
    int sc = 0;
    while (isCollidingWith(other) && sc<10000)
    {
      position.add(tCorrector);
      sc++;
    }
  }

  void applyAttraction(ArrayList<PhysicSphere> others)//applies gravity forces (provided they are enabled) to a sphere as well as all the others. Do note that this part accesses other spheres without going through the Thread structures, as the movement from one frame to the other should be negligible.
  {
    PVector finalAcc = new PVector(0, 0, 0);
    PVector tAcc = new PVector(0, 0, 0);
    for (int i = 0; i<others.size(); i++)
    {
      collideWith(others.get(i));
      if (others.get(i).index!=index)
      {
        if (gravityEnabled) {
          float smallGFactor = -others.get(i).mass*gravityConstant;
          smallGFactor /= others.get(i).position.dist(position)*others.get(i).position.dist(position);
          PVector smallG = position.copy();
          smallG.sub(others.get(i).position);
          smallG.mult(smallGFactor);
          tAcc = smallG.copy();
          tAcc.mult(mass);
          finalAcc.add(smallG);
        }
      }
      if (!Float.isNaN(tAcc.x+tAcc.y+tAcc.z)) {
        acceleration = finalAcc.copy();
      }
    }
  }

  void update() {//Updates the position of the sphere. It is to be noted that this and the correctClipping function are the only parts of the code that should access the position directly
    if (framesElapsed%(globalSpeed+1)==0) {
      prevPos.add(position.copy());
      velocity.add(acceleration);
      if (boundsEnabled) {
        if (position.x<=0 && velocity.x<0)
        {
          velocity.x = -velocity.x*bounciness;
        } else if (position.x>=maxWidth && velocity.x>0)
        {
          velocity.x = -velocity.x*bounciness;
        }
        if (position.y<=0 && velocity.y<0)
        {
          velocity.y = -velocity.y*bounciness;
        } else if (position.y>=maxHeight && velocity.y>0)
        {
          velocity.y = -velocity.y*bounciness;
        }
        if (position.z<=0 && velocity.z<0)
        {
          velocity.z = -velocity.z*bounciness;
        } else if (position.z>=maxDepth && velocity.z>0)
        {
          velocity.z = -velocity.z*bounciness;
        }
      }
      nullifyPVectorNaN(velocity);//there *might* be a chance for spheres to get trapped inbetween two bouncing spheres that apply opposite forces, which will result in them over-correcting their velocities. This prevents an impossible velocity from being applied to a sphere, which allows them to clip a little bit, but ultimately prevents both crashes and excessive clipping.
      position.add(velocity);
      correctPVectorNaN(position, prevPos);//prevents the positions from being altered too much after fast hitting spheres. While very rares, instances where one sphere gets knocked off screen tend to crash the simulation due to the gravitational forces being skewed toward it after a while.
    }
  }

  void display() {//draws a sphere according to its position radius, color, index (which gives the name) and tail effect.
    pushMatrix();
    translate(position.x, position.y, position.z);
    noStroke();
    fill(sphereColor, 200);
    sphere(radius*2);
    popMatrix();
    fill(255-red(sphereColor), 255-green(sphereColor), 255-blue(sphereColor));
    textSize(radius*3);
    if (namesDisplayed) {
      text((char) (index+65), lerp(maxWidth*0.05, maxWidth*0.95, (position.x - radius)/maxWidth), lerp(maxHeight*.05, maxHeight*0.95, (position.y+radius)/maxHeight)+100, position.z+radius*2);//index+65 will print ascii characters starting at 'A'. I am aware that it won't be able to print some of them, but this mostly decorative or for debugging.
    }
    if (weightsDisplayed) {
      text(((int)floor(mass*100)), lerp(maxWidth*0.05, maxWidth*0.95, (position.x - radius)/maxWidth), lerp(maxHeight*.05, maxHeight*0.95, (position.y+radius)/maxHeight), position.z+radius*2);
    }
    noFill();
    beginShape();
    curveVertex(position.x, position.y, position.z);
    strokeCap(SQUARE);
    if (tailsDisplayed) {//Processing's way of drawing strokes gives them no depth on the Z axis, which makes them look flat when the balls turn at sharp angles or face slightly away from the camera.
      for (int i = prevPos.size()>0?prevPos.size()-1:0; i>(prevPos.size()>20?prevPos.size()-20:0); i--)
      {
        stroke(sphereColor, lerp(255, 25, ((float)(prevPos.size()<20?i:prevPos.size()-i))/(prevPos.size()<20?prevPos.size():20)));
        strokeWeight(lerp(0, radius*2, lerp(1.0, 0, ((float)(prevPos.size()<20?prevPos.size()-i:prevPos.size()-i))/(prevPos.size()<20?prevPos.size():20))));
        curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z );
      }
    }
    endShape();

    if (index>=0 && arrowsDisplayed) {
      drawArrow(position.x, position.y, position.z, radius, velocity);
    }
  }
}
