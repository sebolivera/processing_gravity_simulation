import java.util.ArrayList;
import java.lang.Object;
float G = 1;//Gravity constant
int BOTTOM_INIT_X, BOTTOM_INIT_Y;
boolean DRAW_TRAILS = false;//Shows a trail effect as spheres move
boolean DRAW_ARROWS = false;//Shows velocity arrows of each sphere
boolean DRAW_NAMES = false;//Assigns a letter to each sphere and displays it
boolean DRAW_WEIGHTS = false;//Shows the weight of a sphere (multiplied by 100 and rounded)
boolean ENABLE_GRAVITY = true;//Enables attraction between the spheres
boolean PAUSED = false;//Handles the logic part of the physic simulation
int SPHERE_COUNT = 20;//Default amount of spheres, feel free to edit it to try out other simulations.
int THREAD_COUNT = Runtime.getRuntime().availableProcessors();//Creates as many threads as there are cores available. Should never be less than 1 unless bad things are about to happen.
boolean SHOW_INTERFACE = true;//Handles the display of the GUI.
int UNPAUSED_TIMER = -3000;//Handles the fade-out for the "Running" text on unpause action.

ArrayList<Physic_Sphere> spheres;
ArrayList<Physic_Sphere_Threaded> threaded_spheres;

PFont fontBold, fontLight;
color rectColor;
color rectHighlight;

void setup() {
  size(1000, 1000, P3D);//OpenGL didn't show any significant difference, feel free to use it instead.
  fontBold = createFont("Roboto-Black.ttf", 128);//A custom font has to be used otherwise the text appears pixellated on some OS.
  fontLight = createFont("Roboto-Light.ttf", 30);
  textFont(fontBold);
  rectColor = color(0);
  rectHighlight = color(51);
  seed(SPHERE_COUNT);//See Seed Tab
  BOTTOM_INIT_X = 50;
  BOTTOM_INIT_Y = height-50;
}

void draw() {
  background(0);
  stroke(0);
  lights();
  hover();
  translate(200, 150);
  if (!PAUSED) {
    for (int i = 0; i<threaded_spheres.size(); i++)
    {
      threaded_spheres.get(i).run();
    }
  }
  for (int i = 0; i<spheres.size(); i++)
  {
    spheres.get(i).display();
  }

  for (int i = 0; i<threaded_spheres.size(); i++)
  {
    try {
      threaded_spheres.get(i).join();
    }
    catch (InterruptedException e) {
      System.out.println("Collision error. One of the threads couldn't manage the collision calculations in time.");//Safety counter in the de-clipping function should prevent this from ever happening, though it makes the de-clipping a bit more approximate than actually ideal.
      e.printStackTrace();
    }
  }
  pushMatrix();
  translate(-200, -150);
  stroke(255);
  strokeWeight(1);
  textSize(30);
  drawGUI();//See GUI Tab
  popMatrix();
}
