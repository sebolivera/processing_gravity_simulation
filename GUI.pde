boolean arrowEnableOverVel = false;
boolean arrowEnableOverName = false;
boolean arrowEnableOverWeight = false;
boolean arrowEnableOverTrail = false;
boolean arrowEnableOverGravity = false;

boolean overRect(int x, int y, int width, int height) {
  if (mouseX >= x && mouseX <= x+width &&
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

void hover() {//handles hovers on titles and checkboxes for options
  if ( overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y, 320, 20) ) {
    arrowEnableOverVel = true;
  } else {
    arrowEnableOverVel = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-50, 295, 20)) {
    arrowEnableOverName = true;
  } else {
    arrowEnableOverName = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-100, 270, 20)) {
    arrowEnableOverWeight = true;
  } else {
    arrowEnableOverWeight = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-150, 220, 20)) {
    arrowEnableOverTrail = true;
  } else {
    arrowEnableOverTrail = false;
  }
  if (overRect(BOTTOM_INIT_X, BOTTOM_INIT_Y-200, 220, 20)) {
    arrowEnableOverGravity = true;
  } else {
    arrowEnableOverGravity = false;
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
  text("Press 'R' to restart the simulation", width-475, height-30);
  text("Press 'H' to hide the interface", width-475, height-130);
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

void drawGUI() {//Handles the display for the Graphical User Intefrace. Is on by default.
  if (SHOW_INTERFACE) {
    //enable velocity arrows display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y, "Show velocity arrows", DRAW_ARROWS, arrowEnableOverVel);

    //enable name display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-50, "Show sphere names", DRAW_NAMES, arrowEnableOverName);

    //enable sphere weights display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-100, "Show sphere weights", DRAW_WEIGHTS, arrowEnableOverWeight);

    //enable trails display
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-150, "Show sphere weights", DRAW_TRAILS, arrowEnableOverTrail);

    //enable gravity
    drawMenuElementTickBox(BOTTOM_INIT_X, BOTTOM_INIT_Y-200, "Enable gravity", ENABLE_GRAVITY, arrowEnableOverGravity);
    drawHints();
  }
}
