boolean arrowEnableOverVel = false;
boolean arrowEnableOverName = false;
boolean arrowEnableOverWeight = false;
boolean arrowEnableOverTrail = false;
boolean arrowEnableOverGravity = false;
boolean arrowEnableOverBounds = false;

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
    //had issues with Box() function so I resorted to simply drawing the lines.
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
    fill(255, 255, 0);
    text("X", x, y+20);
  }
  if (hovered) {
    fill(255);
  } else {
    fill(200);
  }
  text(text, x+30, y+20);
}

void drawHints() {
  fill(255);
  textFont(fontLight);
  text("Use left-click to move around", width-475, height-330);
  text("Use right-click to rotate the camera", width-475, height-280);
  text("Use mouse wheel to zoom", width-475, height-230);
  text("Double-click to reset camera", width-475, height-180);
  text("Press 'H' to hide the interface", width-475, height-130);
  text("Press 'R' to restart the simulation", width-475, height-30);
  if (PAUSED) {
    textFont(fontLight);
    text("Press 'P' to unpause the simulation", width-475, height-80);
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
    text("Press 'P' to pause the simulation", width-475, height-80);
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

void drawMouse(){
  strokeWeight(2);
  stroke(0, 0, 255);
  line(mouseX-25, mouseY, mouseX+25, mouseY);
  line(mouseX, mouseY-25, mouseX, mouseY+25);
  strokeWeight(1);
}
void drawGUI() {//Handles the display for the Graphical User Intefrace. Is on by default.
  if (SHOW_INTERFACE) {
    cam.beginHUD();
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
    cam.endHUD();
  }
}
