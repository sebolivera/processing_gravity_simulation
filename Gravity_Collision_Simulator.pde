import java.util.ArrayList;
import java.lang.Object;
import peasy.*;

PeasyCam cam;//peasycam registers handlers for click movements.
PeasyDragHandler PanDragHandler;
PeasyDragHandler ZoomDragHandler;
PeasyDragHandler RotateDragHandler;

float G = 1;//Gravity constant
int BOTTOM_INIT_X, BOTTOM_INIT_Y;
boolean DRAW_TRAILS = false;//Shows a trail effect as spheres move
boolean DRAW_ARROWS = false;//Shows velocity arrows of each sphere
boolean DRAW_NAMES = false;//Assigns a letter to each sphere and displays it
boolean DRAW_WEIGHTS = false;//Shows the weight of a sphere (multiplied by 100 and rounded)
boolean ENABLE_GRAVITY = true;//Enables attraction between the spheres
boolean ENABLE_BOUNDS = true;//Enable box bounds. Due to the way I implemented this, these values are hard-coded, but I will probably allow the walls to be dynamically adjusted in the future.
boolean PAUSED = false;//Handles the logic part of the physic simulation
int SPHERE_COUNT = 20;//Default amount of spheres, feel free to edit it to try out other simulations.
int THREAD_COUNT = Runtime.getRuntime().availableProcessors();//Creates as many threads as there are cores available. Should never be less than 1 unless bad things are about to happen.
boolean SHOW_INTERFACE = true;//Handles the display of the GUI.
int UNPAUSED_TIMER = -3000;//Handles the fade-out for the "Running" text on unpause action.

ArrayList<Physic_Sphere> spheres;//global collection of sphres, used for display and collision detection. They are independent of the threads by design, but might be replaced in the future.
ArrayList<Sphere_Batch_Thread> threaded_spheres;//Collection of batches split into several threads to ease the ressource usage during computation. Is only relevant for amounts of spheres>100 for normal settings, but doesn't hurt.

PFont fontBold, fontLight;//A custom font has to be used otherwise the text appears pixellated on some OS.
color TICKBOX_COLOR;//color of the tickboxes when
color TICKBOX_HIGHLIGHT_COLOR;

void setup() {
  size(1000, 1000, P3D);//OpenGL didn't show any significant difference in performance, feel free to use it instead.

  cam = new PeasyCam(this, width/2, height/2, height+800, 100);//using peasycam as I don't want to have to code my entire camera system myself.
  PanDragHandler = cam.getPanDragHandler();
  ZoomDragHandler = cam.getZoomDragHandler();
  RotateDragHandler = cam.getRotateDragHandler();
  cam.setLeftDragHandler(PanDragHandler);
  cam.setRightDragHandler(RotateDragHandler);
  fontBold = createFont("Roboto-Black.ttf", 128);
  fontLight = createFont("Roboto-Light.ttf", 30);
  textFont(fontBold);
  TICKBOX_COLOR = color(0);
  TICKBOX_HIGHLIGHT_COLOR = color(51);
  seed(SPHERE_COUNT);//See Seed Tab
  BOTTOM_INIT_X = 50;
  BOTTOM_INIT_Y = height-50;
}

void draw() {
  background(0);
  lights();
  hover();//see GUI tab for details
  drawBounds();
  stroke(0);

  if (!PAUSED) {//management of the physics of the sphere
    for (int i = 0; i<threaded_spheres.size(); i++)
    {
      threaded_spheres.get(i).run();
    }
  }
  
  for (int i = 0; i<spheres.size(); i++)//Display of each sphere has to happen even when the game is paused as the GUI stays active
  {
    spheres.get(i).display();
  }
  for (int i = 0; i<threaded_spheres.size(); i++)
  {
    try {
      threaded_spheres.get(i).join();
    }
    catch (InterruptedException e) {//Safety counter in the de-clipping function should prevent this from ever happening, though it makes the de-clipping a bit more approximate than actually ideal.
      System.out.println("Collision error. One of the threads couldn't manage the collision calculations in time.");
      e.printStackTrace();
    }
  }
  pushMatrix();
  stroke(255);
  strokeWeight(1);
  textSize(30);
  rotateY((float) frames/1000);
  drawGUI();//See GUI Tab
  popMatrix();
}
