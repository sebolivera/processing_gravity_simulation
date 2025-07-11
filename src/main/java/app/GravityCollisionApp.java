package app;

import processing.core.*;

import java.util.ArrayList;
import java.lang.Object;

import damkjer.ocd.*;

import java.util.function.Consumer;
import java.awt.im.InputContext;
import java.util.Locale;

import processing.event.MouseEvent;

import java.util.HashSet;
import java.util.Set;

public final class GravityCollisionApp extends PApplet {


    public static float CAM_PAN_STEP = 20;
    public static int FRAMES = 0;
    public static float CAM_DOLLY_STEP = 20;
    public static int GLOBAL_SPEED = 0;

    public static float G = 6.6743f;
    public static boolean DRAW_TRAILS = false;
    public static boolean DRAW_ARROWS = false;
    public static boolean DRAW_NAMES = false;
    public static boolean DRAW_WEIGHTS = false;
    public static boolean ENABLE_GRAVITY = true;
    public static boolean ENABLE_BOUNDS = true;
    public static boolean PAUSED = false;

    /* …the rest follow the same pattern … */

    private ArrayList<PhysicSphere> spheres = new ArrayList<>();
    private ArrayList<SphereBatchThread> threads = new ArrayList<>();

    private HScrollbar gravityScroll;
    private HScrollbar speedScroll;

    /* === life-cycle methods required by Processing ===================== */

    @Override
    public void settings() {
        size(1000, 1000, P3D);
    }

    @Override
    public void setup() {
        cam = new Camera(this, width / 2f, height / 2f, height + 1000, width / 2f, height / 2f, 0);
        /* …everything that was in the old setup() stays here … */
        textFont(createFont("Roboto-Black.ttf", 128));
        seed(SPHERE_COUNT);
        initGUI();
        noCursor();
    }

    @Override
    public void draw() {
        background(0);
        lights();
        cam.feed();

        hover();
        drawBounds();

        if (!PAUSED) {
            threads.forEach(Thread::run);
        }
        spheres.forEach(PhysicSphere::display);

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        drawGUI();
        move();
        FRAMES++;
    }

    /* === callbacks still belong here =================================== */

    @Override
    public void mousePressed() { /* same code as before */ }

    @Override
    public void mouseWheel(MouseEvent evt) { /* … */ }

    @Override
    public void keyPressed() { /* … */ }

    @Override
    public void keyReleased() { /* … */ }

    /* === tiny setters that other classes can call ====================== */

    void setG(float g) {
        this.localG = g;
    }

    void setGlobalSpeed(int s) {
        this.GLOBAL_SPEED = s;
    }
    /* add whatever you actually need, nothing more */

    /* === main() so the sketch can be launched from the IDE ============= */

    public static void main(String[] args) {
        PApplet.main(GravityCollisionApp.class);
    }
}
