Set<String> keysDown = new HashSet<>();
Set<String> MOVE_KEYS = Set.of("w", "a", "s", "d", "q", "z");
float speedMult;
boolean isShiftDown = false;
float yaw, pitch;

void mousePressed() {//handles the clicks on the checkboxes
  if (!toggledFreeCam){
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
    if (arrowEnableOverBounds) {
      ENABLE_BOUNDS = !ENABLE_BOUNDS;
    }
    if (!firstMousePress) {//for the scrollbar
      firstMousePress = true;
    }
  }
}

void move(){
  
  yaw = radians(mouseX - pmouseX)*0.125;
  pitch = radians(mouseY - pmouseY)*0.125;
  
  

  if (toggledFreeCam && mouseButton == RIGHT) {
    cam.truck(-(mouseX - pmouseX));
    cam.boom((mouseY - pmouseY)); // No, this unfortunately does not make the camera go "boom" :'(
  }
  else if (toggledFreeCam) {
    cam.pan(yaw);
    cam.tilt(pitch);
    cam.roll(0);
    for (String currentKey: keysDown){
      speedMult = isShiftDown ? 4 : 1;
      switch (currentKey) {
          case "z":
          case "w":
              cam.dolly(-CAM_DOLLY_STEP * speedMult);
              break;
      
          case "s":
              cam.dolly( CAM_DOLLY_STEP * speedMult);
              break;
      
          case "q":
          case "a":
              cam.truck(-CAM_PAN_STEP * speedMult);
              break;
      
          case "d":
              cam.truck( CAM_PAN_STEP * speedMult);
              break;
      
          default:
              // no-op
      }
    }
  }
}

void keyPressed(KeyEvent e) {
  if (key == 'l' || key == 'L') {             // manual toggle
    isAzerty = !isAzerty;
  }
  
  if (MOVE_KEYS.contains("" + key)){
    keysDown.add("" + key);
  }
  
  if (e.isShiftDown()){
    isShiftDown = true;
  }
}

void keyReleased(KeyEvent e) {//Handle of keypresses
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
  
  if (MOVE_KEYS.contains("" + key)){
    keysDown.remove("" + key);
  }
  
  if (!e.isShiftDown()){
    isShiftDown = false;
  }
  if (key == 'c')
  {
    resetCam();
  }
  if (key == 'f')
  {
    toggledFreeCam = !toggledFreeCam;
  }
}

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  cam.dolly(e * 20);
}
