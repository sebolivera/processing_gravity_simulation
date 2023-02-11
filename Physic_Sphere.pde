ArrayList<Pair> collision_index_fakemutex = new ArrayList<Pair>();//locks pairs of colliding spheres in the event that they belong to different threads trying to calculate their collisions at the same time

class Physic_Sphere {//Base class for the spheres
  /*
   * Core of the simulation. Each sphere has an update function that allows it to look at all other objects and apply their gravity to it, while they do the same for eachother
   * A sphere has position (3D vector), a radius (float>0), a velocity (3D vector), a mass (float>0), an acceleration (3D vector), a color (processing Color>=(255, 255, 255)), an index (int>=0) and a bounciness (0<=float<=1)
   */
  float MAX_WIDTH = 600.0;
  float MAX_HEIGHT = 600.0;
  float MAX_DEPTH = 600.0;
  color c;
  PVector pos;
  PVector vel;
  ArrayList<PVector> prevPos = new ArrayList<PVector>();
  int max_speed = 5;
  float radius;
  float mass;
  float bounciness;
  int index;
  public PVector acc;

  Physic_Sphere(int t_index, color t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass) {//Bounciness is optional, and has been known to cause some issues when too many spheres are colliding, so it is 1 by default (perfect bounciness)
    index = t_index;
    c = t_c;
    pos = t_pos;
    vel = t_vel;
    radius = t_radius*2;
    mass = t_mass;
    acc = new PVector(0.0, 0.0, 0.0);
    bounciness = 1.0;
  }

  Physic_Sphere(int t_index, color t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass, float t_bounciness) {
    index = t_index;
    c = t_c;
    pos = t_pos;
    vel = t_vel;
    radius = t_radius*2;
    mass = t_mass;
    acc = new PVector(0.0, 0.0, 0.0);
    bounciness = t_bounciness;
  }


  void drawArrow(float cx, float cy, float cz, float len, PVector dest)
  {//Drawing an arrow proved itself to be quite the challenge. I took inspiration from from https://forum.processing.org/one/topic/drawing-an-arrow.html and tweaked it a bit.
    pushMatrix();
    strokeWeight(radius/2);
    translate(cx, cy, cz);
    dest = dest.copy();
    dest.normalize();
    dest.mult(len*dest.mag()*10);

    stroke(255-red(c), 255-green(c), 255-blue(c));
    line(0, 0, 0, dest.x, dest.y, dest.z);
    fill(255-red(c), 255-green(c), 255-blue(c));
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


  boolean isCollidingWith(Physic_Sphere other)
  {
    if (index!=other.index) {
      boolean isFrameColliding = other.pos.dist(pos) < other.radius*2 + radius*2;
      PVector vectorized_position = pos.copy();
      vectorized_position.dot(vel);
      PVector other_vectorized_position = other.pos.copy();
      other_vectorized_position.dot(other.vel);
      boolean isVectorColliding = vectorized_position.dist(other_vectorized_position) < other.radius*2 + radius*2;
      return isFrameColliding || isVectorColliding;//we account both for the current frame and the next one in case of high speeds
    }
    return false;
  }

  void collideWith(Physic_Sphere other) {
    Pair duo = new Pair(index, other.index);
    if (isCollidingWith(other)) {
      if (!ArrayListPairContains(collision_index_fakemutex, duo, true)) {//prevents the "other" sphere from doing the same calculation if it belongs to another thread
        //safety copies of the velocities in case of large clumping of objects
        // Cumulative implementation of angle collisions and massed elastic collisions
        // see:
        // - Momentum conservation in angle collisions between two spherical bodies : https://atmos.illinois.edu/courses/atmos100/userdocs/3Dcollisions.html
        // - Elastic collision and exchange of momentum between two bodies with different masses : https://physics.stackexchange.com/questions/681396/elastic-collision-3d-eqaution
        // - TODO: add rotation to the equation (check https://www.euclideanspace.com/physics/dynamics/collision/threed/index.htm). ISSUE: don't know how to get rotation of object in processing (nor if it is even possible)

        PVector v_imp_2 = getNormalVector(vel, pos, other.pos);//v_imp_2 & v_imp_1 are swapped
        PVector v_imp_1 = getNormalVector(other.vel, other.pos, pos);

        // Apply the normal vectors to the velocity

        PVector v_normal_1 = vel.copy();
        v_normal_1.sub(v_imp_1);

        PVector v_normal_2 = other.vel.copy();
        v_normal_2.sub(v_imp_2);

        //elastic collision part (or how to account for mass)

        // apply the normals to the movement vectors by accounting for the mass (see second link)
        PVector v_1_i = vel.copy();
        PVector v_2_i = other.vel.copy();

        //Takes the normal impulse vector and applies it to the opposite force's magnitude. Since I couldn't find a simulation where accounted for both angle as well as mass, I had to improvise
        v_2_i = new PVector(v_1_i.mag() * (v_normal_2.x/v_normal_2.mag()), v_1_i.mag() * (v_normal_2.y/v_normal_2.mag()), v_1_i.mag() * (v_normal_2.z/v_normal_2.mag()));
        v_1_i = new PVector(v_2_i.mag() * (v_normal_1.x/v_normal_1.mag()), v_2_i.mag() * (v_normal_1.y/v_normal_1.mag()), v_2_i.mag() * (v_normal_1.z/v_normal_1.mag()));

        PVector m1v1i = v_1_i.copy();
        m1v1i.mult(mass);//Is what is recommended in the provided link and corroborated by wikipedia & wolfram, but some mass transfers don't seem to conserve the proper amount of energyswap with m1v1i.mult(other.mass/mass); in case of unexpected energy transfer?

        PVector m2v2i = v_2_i.copy();
        m2v2i.mult(other.mass);

        PVector a_part = m1v1i.copy();
        a_part.add(m2v2i);

        PVector b_v_1_i = v_1_i.copy();
        b_v_1_i.mult((float)bounciness);
        PVector b_v_2_i = v_2_i.copy();
        b_v_2_i.mult((float)bounciness);

        //b_part => (((1-bounciness)(v_1_i-v_2_i)+v_1_f)*other.mass)/mass
        PVector b_part = b_v_1_i.copy();
        b_part.sub(b_v_2_i);

        //v_1_f => (v_1_i*mass+v_2_i*other.mass-(1-bounciness)*(v_1_i-v_2_i))/(mass+1)
        PVector v_1_f = a_part.copy();
        v_1_f.sub(b_part);

        v_1_f.div(mass+1.0);

        //v_2_f => (1-bounciness)*(v_1_i-v_2_i)+v_1_f
        PVector v_2_f = v_1_f.copy();
        v_2_f.add(b_part);

        if (!Float.isNaN(v_1_f.x+v_1_f.y+v_1_f.z)) {
          vel = v_1_f;
        }

        if (!Float.isNaN(v_2_f.x+v_2_f.y+v_2_f.z)) {
          other.vel = v_2_f;
        }

        correctClipping(other);
      }
    } else
    {
      if (ArrayListPairContains(collision_index_fakemutex, duo, false)) {
        collision_index_fakemutex.remove(ArrayListPairGetAt(collision_index_fakemutex, duo));
      }
    }
  }

  void correctClipping(Physic_Sphere other)
  {
    PVector t_corrector = vel.copy();
    t_corrector.sub(getNormalVector(vel, pos, other.pos));
    t_corrector.mult(.1);
    int sc = 0;
    while (isCollidingWith(other) && sc<10000)
    {
      pos.add(t_corrector);
      sc++;
    }
  }

  void applyAttraction(ArrayList<Physic_Sphere> others)//applies gravity forces (provided they are enabled) to a sphere as well as all the others. Do note that this part accesses other spheres without going through the Thread structures, as the movement from one frame to the other should be negligible.
  {
    PVector final_acc = new PVector(0, 0, 0);
    PVector t_acc = new PVector(0, 0, 0);
    for (int i = 0; i<others.size(); i++)
    {
      collideWith(others.get(i));
      if (others.get(i).index!=index)
      {
        if (ENABLE_GRAVITY) {
          float small_g_factor = -others.get(i).mass*G;
          small_g_factor /= others.get(i).pos.dist(pos)*others.get(i).pos.dist(pos);
          PVector small_g = pos.copy();
          small_g.sub(others.get(i).pos);
          small_g.mult(small_g_factor);
          t_acc = small_g.copy();
          t_acc.mult(mass);
          final_acc.add(small_g);
        }
      }
      if (!Float.isNaN(t_acc.x+t_acc.y+t_acc.z)) {
        acc = final_acc.copy();
      }
    }
  }
  
  void update() {//Updates the position of the sphere. It is to be noted that this and the correctClipping function are the only parts of the code that should access the position directly
    prevPos.add(pos.copy());
    vel.add(acc);

    if (pos.x<=0 && vel.x<0)
    {
      vel.x = -vel.x*bounciness;
    } else if (pos.x>=MAX_WIDTH && vel.x>0)
    {
      vel.x = -vel.x*bounciness;
    }
    if (pos.y<=0 && vel.y<0)
    {
      vel.y = -vel.y*bounciness;
    } else if (pos.y>=MAX_HEIGHT && vel.y>0)
    {
      vel.y = -vel.y*bounciness;
    }
    if (pos.z>=0 && vel.z>0)
    {
      vel.z = -vel.z*bounciness;
    } else if (pos.z<=-MAX_DEPTH && vel.z<0)
    {
      vel.z = -vel.z*bounciness;
    }
    nullifyPVectorNaN(vel);//there *might* be a chance for spheres to get trapped inbetween two bouncing spheres that apply opposite forces, which will result in them over-correcting their velocities. This prevents an impossible velocity from being applied to a sphere, which allows them to clip a little bit, but ultimately prevents both crashes and excessive clipping.
    pos.add(vel);
    correctPVectorNaN(pos, prevPos);//prevents the positions from being altered too much after fast hitting spheres. While very rares, instances where one sphere gets knocked off screen tend to crash the simulation due to the gravitational forces being skewed toward it after a while.
  }
  
  void display() {//draws a sphere according to its position radius, color, index (which gives the name) and tail effect.
    pushMatrix();
    noStroke();
    translate(pos.x, pos.y, pos.z);
    fill(lerp(red(c)/10, red(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)), lerp(green(c)/10, green(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)), lerp(blue(c)/10, blue(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)));
    sphere(radius*2);
    popMatrix();
    fill(255-red(c), 255-green(c), 255-blue(c));
    textSize(radius*3);
    if (DRAW_NAMES) {
      text((char) (index+65), lerp(MAX_WIDTH*0.05, MAX_WIDTH*0.95, (pos.x - radius)/MAX_WIDTH), lerp(MAX_HEIGHT*.05, MAX_HEIGHT*0.95, (pos.y+radius)/MAX_HEIGHT)+100, pos.z+radius*2);//index+65 will print ascii characters starting at 'A'. I am aware that it won't be able to print some of them, but this mostly decorative or for debugging.
    }
    if (DRAW_WEIGHTS) {
      text(((int)floor(mass*100)), lerp(MAX_WIDTH*0.05, MAX_WIDTH*0.95, (pos.x - radius)/MAX_WIDTH), lerp(MAX_HEIGHT*.05, MAX_HEIGHT*0.95, (pos.y+radius)/MAX_HEIGHT), pos.z+radius*2);
    }
    noFill();
    beginShape();
    curveVertex(pos.x, pos.y, pos.z);
    strokeCap(SQUARE);
    if (DRAW_TRAILS) {//Processing's way of drawing strokes gives them no depth on the Z axis, which makes them look flat when the balls turn at sharp angles or face slightly away from the camera.
      for (int i = prevPos.size()>0?prevPos.size()-1:0; i>(prevPos.size()>20?prevPos.size()-20:0); i--)
      {
        stroke(c, lerp(255, 0, ((float)(prevPos.size()<20?i:prevPos.size()-i))/(prevPos.size()<20?prevPos.size():20)));
        strokeWeight(lerp(0, radius*2, lerp(1.0, 0, ((float)(prevPos.size()<20?prevPos.size()-i:prevPos.size()-i))/(prevPos.size()<20?prevPos.size():20))));
        curveVertex(prevPos.get(i).x, prevPos.get(i).y, prevPos.get(i).z );
      }
    }
    endShape();

    if (index>=0 && DRAW_ARROWS) {
      drawArrow(pos.x, pos.y, pos.z, radius, vel);
    }
  }
}
