void mousePressed() {
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

void keyReleased() {
  if (keyCode == 82) {
    seed(SPHERE_COUNT);
  }
  if (keyCode == 80)
  {
    PAUSED = !PAUSED;
    if (!PAUSED) {
      UNPAUSED_TIMER = millis();
    }
  }
  if (keyCode == 72)
  {
    SHOW_INTERFACE = !SHOW_INTERFACE;
  }
}
