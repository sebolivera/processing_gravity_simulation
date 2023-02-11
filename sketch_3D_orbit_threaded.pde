float G = 1;
import java.util.ArrayList;
import java.lang.Object;
boolean DRAW_TRAILS = true;
boolean DRAW_ARROWS = true;
boolean DRAW_NAME = true;
boolean DRAW_WEIGHT = true;
int idx_counter = 0;
int MAX_THREAD_SIZE = Runtime.getRuntime().availableProcessors();
boolean ENABLE_GRAVITY = true;
boolean paused = false;
int collisionsPerFrame = 0;

public class InvalidObjectAmount extends Exception {
  public InvalidObjectAmount(String message) {
    super(message);
  }
}

public class Pair<X, Y> {
  public final X x;
  public final Y y;
  public Pair(X x, Y y) {
    this.x = x;
    this.y = y;
  }
  public boolean equals(Pair other)
  {
    return (other.x==x && other.y ==y) || (other.y == x && other.x==y);
  }
  public boolean equals_strict(Pair other)
  {
    return (other.y == x && other.x==y);
  }
}

ArrayList<Pair> collision_index_fakemutex = new ArrayList<Pair>();

boolean ArrayListPairContains(ArrayList<Pair> HS, Pair other, boolean can_be_reverse)
{
  for (int i = 0; i<HS.size(); i++)
  {
    if (can_be_reverse && HS.get(i).equals(other))
    {
      return true;
    }
    if (!can_be_reverse && HS.get(i).equals_strict(other))
    {
      return true;
    }
  }
  return false;
}

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

int ArrayListPairGetAt(ArrayList<Pair> HS, Pair other)
{
  for (int i = 0; i<HS.size(); i++)
  {
    if (HS.get(i).equals(other))
    {
      return i;
    }
  }
  return -1;
}


float getXYZAngleForImpactVector (PVector vel, PVector delta_vel) {
  //gets xyz angle between the direction of an object and the vector between it and the other ball
  PVector normalized_vel = vel.copy();
  PVector delta = delta_vel.copy();
  normalized_vel.normalize();
  normalized_vel = new PVector(abs(normalized_vel.x), abs(normalized_vel.y), abs(normalized_vel.z));
  float numerator = delta.x*normalized_vel.x+delta.y*normalized_vel.y+delta.z*normalized_vel.z;
  float denominator = sqrt((delta.x+delta.y+delta.z)*(normalized_vel.x+normalized_vel.y+normalized_vel.z));
  return acos(numerator/denominator);
}

PVector getCenterVector (PVector vel, PVector delta_vel)
{
  PVector finalVect = vel.copy();
  finalVect.mult(getXYZAngleForImpactVector(vel, delta_vel));
  return finalVect;
}

PVector getNormalizedDelta(PVector pos_1, PVector pos_2)
{
  PVector delta = pos_2.copy();
  delta.sub(pos_1);
  delta = new PVector(abs(delta.x), abs(delta.y), abs(delta.z));
  delta.normalize();
  return delta;
}

PVector getNormalVector(PVector vel, PVector pos_1, PVector pos_2) {
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

class Celestial_object {
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
  Celestial_object(int t_index, color t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass) {
    index = t_index;
    c = t_c;
    pos = t_pos;
    vel = t_vel;
    radius = t_radius*2;
    mass = t_mass;
    acc = new PVector(0.0, 0.0, 0.0);
    bounciness = 1.0;
  }
  Celestial_object(int t_index, color t_c, PVector t_pos, PVector t_vel, float t_radius, float t_mass, float t_bounciness) {
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
  {//How is drawing a proper arrow literally harder than the whole math part? I had to resort to copying code from https://forum.processing.org/one/topic/drawing-an-arrow.html
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


  void update() {

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
    nullifyPVectorNaN(vel);//there *might* be a chance for balls to get trapped inbetween two bouncing balls that apply opposite forces, which will result in them over-correcting their velocities. This prevents an impossible velocity from being applied to a ball, which allows them to clip a little bit, but ultimately prevents both crashes and excessive clipping.
    pos.add(vel);
    correctPVectorNaN(pos, prevPos);//prevents the positions from being altered too much after fast hitting balls. While very rares, instances where one ball gets knocked off screen tend to crash the simulation due to the gravitational forces being skewed toward it after a while.
  }
  void display() {
    pushMatrix();
    noStroke();
    translate(pos.x, pos.y, pos.z);
    fill(lerp(red(c)/10, red(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)), lerp(green(c)/10, green(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)), lerp(blue(c)/10, blue(c), (pos.z+MAX_DEPTH)/(MAX_DEPTH*2)));
    sphere(radius*2);
    popMatrix();
    fill(255-red(c), 255-green(c), 255-blue(c));
    textSize(radius*3);
    if (DRAW_NAME) {
      text((char) (index+65), lerp(MAX_WIDTH*0.05, MAX_WIDTH*0.95, (pos.x - radius)/MAX_WIDTH), lerp(MAX_HEIGHT*.05, MAX_HEIGHT*0.95, (pos.y+radius)/MAX_HEIGHT)+100, pos.z+radius*2);//index+65 will print ascii characters starting at 'A'
    }
    if (DRAW_WEIGHT) {
      text(((int)floor(mass*100)), lerp(MAX_WIDTH*0.05, MAX_WIDTH*0.95, (pos.x - radius)/MAX_WIDTH), lerp(MAX_HEIGHT*.05, MAX_HEIGHT*0.95, (pos.y+radius)/MAX_HEIGHT), pos.z+radius*2);
    }
    noFill();
    beginShape();
    curveVertex(pos.x, pos.y, pos.z);
    strokeCap(SQUARE);
    if (DRAW_TRAILS) {
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
  boolean isCollidingWith(Celestial_object other)
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

  void collideWith(Celestial_object other) {
    Pair duo = new Pair(index, other.index);
    if (isCollidingWith(other)) {
      if (!ArrayListPairContains(collision_index_fakemutex, duo, true)) {//prevents the "other" ball from doing the same calculation if it belongs to another thread
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

  void correctClipping(Celestial_object other)
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

  void applyAttraction(ArrayList<Celestial_object> others)
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
}


class Celestial_Thread extends Thread {
  ArrayList<Integer> obj_idx_list;
  ArrayList<Celestial_object> collider_list;
  ArrayList<Integer> indexes = new ArrayList<Integer>();

  Celestial_Thread(ArrayList<Integer> t_obj_idx_list, ArrayList<Celestial_object> t_collider_list) {
    obj_idx_list = new ArrayList<Integer>();
    t_obj_idx_list.forEach((n)-> obj_idx_list.add(n));
    collider_list = new ArrayList<Celestial_object>();
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


ArrayList<Celestial_object> balls;
ArrayList<Celestial_Thread> threaded_balls;
Celestial_object sun;
int OBJ_AMT = 20;
void restart() throws Exception
{
  balls = new ArrayList<>();
  threaded_balls = new ArrayList<>();
  color whatever;
  for (int i = 0; i<OBJ_AMT; i++)
  {
    whatever = color(random(200)+55, random(200)+55, random(200)+55);
    float randX = random(600.0);
    float randY = random(600.0);
    float randZ = random(1200.0)-600.0;
    float randR = random( 2+random(10.0));
    PVector t_pos = new PVector(randX, randY, randZ);
    boolean clipped = false;
    for (int j = 0; j<balls.size(); j++) {
      while (PVector.dist(t_pos, balls.get(j).pos)<randR)
      {
        clipped = true;
        randX = random(600.0);
        randY = random(600.0);
        randZ = random(1200.0)-600.0;
        randR = random( 2+random(10.0));
        t_pos = new PVector(randX, randY, randZ);
      }
      if (clipped) {
        System.out.println("Clipped");
        j = 0;
      }
    }
    balls.add(new Celestial_object(i, whatever, new PVector(randX, randY, randZ), new PVector(1-random(5), 1-random(5), 1-random(5)), randR, 0.5+random(0.5)));
  }

  threaded_balls = new ArrayList<Celestial_Thread>();

  ArrayList<Integer> balls_idx_batch = new ArrayList<>();
  if (MAX_THREAD_SIZE>OBJ_AMT)
  {
    for (int i = 0; i<OBJ_AMT; i++)
    {
      balls_idx_batch.add(i);
    }
    threaded_balls.add(new Celestial_Thread(balls_idx_batch, balls));
    balls_idx_batch.clear();//Technically useless since the app will most likely crash due to over-allocation of objects in the first place, but every little bit helps, I guess.
  } else
  {
    int items_per_thread = OBJ_AMT/MAX_THREAD_SIZE;
    int global_idx = 0;
    for (int i = 0; i<MAX_THREAD_SIZE; i++)
    {
      for (int j = 0; j < items_per_thread; j++)
      {
        balls_idx_batch.add(global_idx);
        global_idx++;
      }
      threaded_balls.add(new Celestial_Thread(balls_idx_batch, balls));
      balls_idx_batch.clear();
    }
    if (OBJ_AMT%MAX_THREAD_SIZE!=0)
    {//fill in the rest of the balls if some are left after even distribution
      try {//mostly leaving this here so I don't forget the syntax for exceptions in java...
        if (OBJ_AMT-global_idx<=0) {
          throw new InvalidObjectAmount("No items remain after initial insertion.");
        }
      }
      catch (InvalidObjectAmount e)
      {
        throw new InvalidObjectAmount("No items remain after initial insertion.");
      }
      int remaining_objs = (OBJ_AMT-global_idx);
      for (int i = 0; i<remaining_objs; i++)
      {
        threaded_balls.get(i).add_to_objs(global_idx);
        global_idx++;
      }
      balls_idx_batch.clear();
    }
  }
}

int rectX, rectY;      // Position of bottom rectangle
int rectSize = 20;
color rectColor;
color rectHighlight;
boolean arrowEnableOverVel = false;
boolean arrowEnableOverName = false;
boolean arrowEnableOverWeight = false;
boolean arrowEnableOverTrail = false;
boolean arrowEnableOverGravity = false;

void setup() {
  size(1000, 1000, P3D);
  rectColor = color(0);
  rectHighlight = color(51);
  try {
    restart();
  }
  catch (Exception e) {
    System.out.println(e);
  }
  rectX = 50;
  rectY = height-50;
  smooth(0);
}


boolean overRect(int x, int y, int width, int height) {
  if (mouseX >= x && mouseX <= x+width &&
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

void mousePressed() {
  if (arrowEnableOverVel) {
    DRAW_ARROWS = !DRAW_ARROWS;
  }
  if (arrowEnableOverName) {
    DRAW_NAME = !DRAW_NAME;
  }
  if (arrowEnableOverWeight) {
    DRAW_WEIGHT = !DRAW_WEIGHT;
  }
  if (arrowEnableOverTrail) {
    DRAW_TRAILS = !DRAW_TRAILS;
  }
  if (arrowEnableOverGravity) {
    ENABLE_GRAVITY = !ENABLE_GRAVITY;
  }
}
void hover() {
  if ( overRect(rectX, rectY, rectSize+300, rectSize) ) {
    arrowEnableOverVel = true;
  } else {
    arrowEnableOverVel = false;
  }
  if (overRect(rectX, rectY-50, rectSize+275, rectSize)) {
    arrowEnableOverName = true;
  } else {
    arrowEnableOverName = false;
  }
  if (overRect(rectX, rectY-100, rectSize+250, rectSize)) {
    arrowEnableOverWeight = true;
  } else {
    arrowEnableOverWeight = false;
  }
  if (overRect(rectX, rectY-150, rectSize+200, rectSize)) {
    arrowEnableOverTrail = true;
  } else {
    arrowEnableOverTrail = false;
  }
  if (overRect(rectX+330, rectY, rectSize+200, rectSize)) {
    arrowEnableOverGravity = true;
  } else {
    arrowEnableOverGravity = false;
  }
}

void draw() {
  background(0);
  stroke(0);
  lights();
  hover();
  translate(200, 150);


  if (!paused) {
    for (int i = 0; i<threaded_balls.size(); i++)
    {
      threaded_balls.get(i).run();
    }
  }
  for (int i = 0; i<balls.size(); i++)
  {
    balls.get(i).display();
  }

  for (int i = 0; i<threaded_balls.size(); i++)
  {
    try {
      threaded_balls.get(i).join();
    }
    catch (InterruptedException e) {
      System.out.println("Collision error. One of the threads couldn't manage the collision calculations in time.");//Safety coutner in the de-clipping function should prevent this from happening, though it makes the de-clipping a bit more approximate than actually ideal.
      e.printStackTrace();
    }
  }
  pushMatrix();
  translate(-200, -150);
  stroke(255);
  strokeWeight(1);
  textSize(30);

  //enable arrows
  if (arrowEnableOverVel) {
    fill(rectHighlight);
  } else {
    fill(rectColor);
  }
  rect(rectX, rectY, rectSize, rectSize);
  if (DRAW_ARROWS) {
    fill(0, 255, 0);
    text("✓", rectX, rectY+20);
  }
  fill(255);
  text("Show velocity arrows", 80, height-30);

  //enable names

  if (arrowEnableOverName) {
    fill(rectHighlight);
  } else {
    fill(rectColor);
  }
  rect(rectX, rectY-50, rectSize, rectSize);
  if (DRAW_NAME) {
    fill(0, 255, 0);
    text("✓", rectX, rectY-30);
  }
  fill(255);
  text("Show ball names", 80, height-80);


  //enable weights

  if (arrowEnableOverWeight) {
    fill(rectHighlight);
  } else {
    fill(rectColor);
  }
  rect(rectX, rectY-100, rectSize, rectSize);
  if (DRAW_WEIGHT) {
    fill(0, 255, 0);
    text("✓", rectX, rectY-80);
  }
  fill(255);
  text("Show ball weights", 80, height-130);

  //enable trails

  if (arrowEnableOverTrail) {
    fill(rectHighlight);
  } else {
    fill(rectColor);
  }
  rect(rectX, rectY-150, rectSize, rectSize);
  if (DRAW_TRAILS) {
    fill(0, 255, 0);
    text("✓", rectX, rectY-130);
  }
  fill(255);
  text("Show trails", 80, height-180);
  
  
  //enable gravity

  if (arrowEnableOverGravity) {
    fill(rectHighlight);
  } else {
    fill(rectColor);
  }
  rect(rectX+350, rectY, rectSize, rectSize);
  if (ENABLE_GRAVITY) {
    fill(0, 255, 0);
    text("✓", rectX+350, rectY+20);
  }
  fill(255);
  text("Enable gravity", 430, height-30);

  popMatrix();
}



void keyReleased() {
  if (keyCode == 82) {
    try {
      restart();
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }
  if (keyCode == 80)
  {
    paused = !paused;
  }
}
