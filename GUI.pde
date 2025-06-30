boolean arrowEnableOverVel = false;
boolean arrowEnableOverName = false;
boolean arrowEnableOverWeight = false;
boolean arrowEnableOverTrail = false;
boolean arrowEnableOverGravity = false;
boolean arrowEnableOverBounds = false;
boolean toggledFreeCam = false;

boolean overRect(int x, int y, int width, int height) {
  if (mouseX >= x && mouseX <= x+width &&
          mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

interface FloatFunction {
  void update(Float f);
}

void initGUI() {
  BOTTOM_INIT_X = 50;
  BOTTOM_INIT_Y = height-50;
  TICKBOX_COLOR = color(0);
  TICKBOX_HIGHLIGHT_COLOR = color(51);
  FloatFunction editGLambda = (n) -> {
    edit_G(n);
  };
  FloatFunction editGLOBAL_SPEEDLambda = (n) -> {
    edit_GLOBAL_SPEED(n);
  };
  gravity_scroll = new HScrollbar(BOTTOM_INIT_X, BOTTOM_INIT_Y-280, width/3, 16, 0, 2, "Global gravity scale", 0.5, editGLambda, true, "0", "2");
  speed_scroll = new HScrollbar(BOTTOM_INIT_X, BOTTOM_INIT_Y-350, width/3, 16, 0, 100, "Simulation speed scale", 1.0, editGLOBAL_SPEEDLambda, false, "Slow", "Fast");
}

void drawBounds() {
  if (ENABLE_BOUNDS) {
    noFill();
    stroke(255);
    //Box() had issue in processing3, and so is drawn using lines only.
    line(0, 0, 0, width, 0, 0);
    line(0, 0, 0, 0, height, 0);
    line(0, 0, 0, 0, 0, height);

    line(width, 0, 0, width, height, 0);
    line(width, 0, 0, width, 0, height);

    line(width, 0, height, 0, 0, height);
    line(width, 0, height, width, height, height);

    line(0, 0, height, width, 0, height);
    line(0, 0, height, 0, height, height);


    line(0, height, height, width, height, height);
    line(0, height, height, 0, height, 0);

    line(width, height, 0, width, height, height);
    line(width, height, 0, 0, height, 0);
  }
}

void hover() {//handles hovers on titles and tickboxes for options
  if ( overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y, 320, 20) ) {//Arrow tickbox
    arrowEnableOverVel = true;
  } else {
    arrowEnableOverVel = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-50, 295, 20)) {//Name tickbox
    arrowEnableOverName = true;
  } else {
    arrowEnableOverName = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-100, 270, 20)) {//Weight tickbox
    arrowEnableOverWeight = true;
  } else {
    arrowEnableOverWeight = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-150, 220, 20)) {//Trail tickbox
    arrowEnableOverTrail = true;
  } else {
    arrowEnableOverTrail = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-200, 220, 20)) {//Gravity tickbox
    arrowEnableOverGravity = true;
  } else {
    arrowEnableOverGravity = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-250, 240, 20)) {//Visible bounds tickbox
    arrowEnableOverBounds = true;
  } else {
    arrowEnableOverBounds = false;
  }
}

void drawMenuElementTickBox(int x, int y, String text, boolean active, boolean hovered) {//draws a checkbox, a tickmark and a label for each element
  if (hovered) {
    fill(TICKBOX_HIGHLIGHT_COLOR);
  } else {
    fill(TICKBOX_COLOR);
  }
  rect(x, y, 20, 20);
  if (active) {
    if(toggledFreeCam) {
      fill(128, 128, 128);
    } else {
      fill(255, 255, 0);
    }
    text("X", x, y+20);
  }
  if(toggledFreeCam) {
    fill(128, 128, 128);
  } else if (hovered) {
    fill(255);
  } else {
    fill(200);
  }
  text(text, x+30, y+20);
}

void drawHints() {
  textFont(fontLight);
  fill(255, 255, 0);
  if (toggledFreeCam) {
    text("Use wasd/zqsd to move around.", width-575, height-330);
    text("Use right-click to move the camera laterally.", width-575, height-280);
  }
  text("Press 'f' to toggle freecam", width-575, height-230);
  text("Press 'c' to reset camera position.", width-575, height-180);
  text("Press 'h' to hide the interface.", width-575, height-130);
  text("Press 'r' to restart the simulation.", width-575, height-30);
  if (PAUSED) {
    textFont(fontLight);
    text("Press 'p' to unpause the simulation.", width-575, height-80);
    textFont(fontBold);
    noStroke();
    fill(255, 0, 0);
    rect( 35, 68, 10, 30);
    rect( 50, 68, 10, 30);
    textSize(50);
    text("PAUSED", 75, 100);
  } else
  {
    textFont(fontLight);
    text("Press 'p' to pause the simulation.", width-575, height-80);
    textFont(fontBold);
    noStroke();
    if (millis()-UNPAUSED_TIMER<2000) {
      fill(0, lerp(255, 0, (float)(millis()-UNPAUSED_TIMER)/2000), 0);
      triangle(35, 70, 35, 96, 65, 83);
      textSize(50);
      text("RUNNING", 75, 100);
    }
  }
}

float lastMouseX, lastMouseY;

void drawMouse(){
  strokeWeight(2);
  if (toggledFreeCam) {
    stroke(128);
  }
  else {
    stroke(0, 0, 255);
  }
  if (!toggledFreeCam)
  {
    lastMouseX = mouseX;
    lastMouseY = mouseY;
  }
  line(lastMouseX-25, lastMouseY, lastMouseX+25, lastMouseY);
  line(lastMouseX, lastMouseY-25, lastMouseX, lastMouseY+25);
  strokeWeight(1);
}

void drawGUI() {//Handles the display for the Graphical User Intefrace. Is on by default.
  if (SHOW_INTERFACE) {
    //enable velocity arrows display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y, "Show velocity arrows", DRAW_ARROWS, arrowEnableOverVel);

    //enable name display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-50, "Show sphere names", DRAW_NAMES, arrowEnableOverName);

    //enable sphere weights display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-100, "Show sphere weights", DRAW_WEIGHTS, arrowEnableOverWeight);

    //enable trails display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-150, "Show sphere trails", DRAW_TRAILS, arrowEnableOverTrail);

    //enable gravity
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-200, "Enable gravity", ENABLE_GRAVITY, arrowEnableOverGravity);

    //enable bounds
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-250, "Enable boundaries", ENABLE_BOUNDS, arrowEnableOverBounds);
    drawHints();
    gravity_scroll.update();
    gravity_scroll.display();
    speed_scroll.update();
    speed_scroll.display();

    drawMouse();
  }
}
