void mousePressed() {//handles the clicks on the checkboxes
  if (arrowEnableOverVel) {
    DRAW_ARROWS = !DRAW_ARROWS;
  }
  if (arrowEnableOverName) {
    DRAW_NAMES = !DRAW_NAMES;
  }
  if (arrowEnableOverWeight) {
    DRAW_WEIGHTS = !DRAW_WEIGHTS;
  }
  if (arrowEnableOverTrail) {
    DRAW_TRAILS = !DRAW_TRAILS;
  }
  if (arrowEnableOverGravity) {
    ENABLE_GRAVITY = !ENABLE_GRAVITY;
  }
}

void keyReleased() {//Handle of keypresses
  if (keyCode == 82) {// 'r' for 'restart'
    seed(SPHERE_COUNT);
  }
  if (keyCode == 80)// 'p' for 'pause'
  {
    PAUSED = !PAUSED;
    if (!PAUSED) {
      UNPAUSED_TIMER = millis();
    }
  }
  if (keyCode == 72)// 'h' for 'hide'
  {
    SHOW_INTERFACE = !SHOW_INTERFACE;
  }
}
