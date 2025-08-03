Set<String> keysDown = new HashSet<>();
Set<String> moveKeys = Set.of("z", "q", "s", "d", "w", "a");
float speedMult;
boolean isShiftDown = false;
float yaw, pitch;

void mousePressed() {//handles the clicks on the checkboxes
  if (!toggledFreeCam){
    if (arrowEnableOverVel) {
      arrowsDisplayed = !arrowsDisplayed;
    }
    if (arrowEnableOverName) {
      namesDisplayed = !namesDisplayed;
    }
    if (arrowEnableOverWeight) {
      weightsDisplayed = !weightsDisplayed;
    }
    if (arrowEnableOverTrail) {
      tailsDisplayed = !tailsDisplayed;
    }
    if (arrowEnableOverGravity) {
      gravityEnabled = !gravityEnabled;
    }
    if (arrowEnableOverBounds) {
      boundsEnabled = !boundsEnabled;
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
              cam.dolly(-camDollyStep * speedMult);
              break;
      
          case "s":
              cam.dolly( camDollyStep * speedMult);
              break;
      
          case "q":
          case "a":
              cam.truck(-camPanStep * speedMult);
              break;
      
          case "d":
              cam.truck( camPanStep * speedMult);
              break;
      
          default:
      }
    }
  }
}

void keyPressed(KeyEvent e) {
  if (key == 'l' || key == 'L') {             // manual toggle
    isAzerty = !isAzerty;
  }
  
  if (moveKeys.contains("" + key)){
    keysDown.add("" + key);
  }
  
  if (e.isShiftDown()){
    isShiftDown = true;
  }
}

void keyReleased(KeyEvent e) {//Handle of keypresses
  if (keyCode == 82) {// 'r' for 'restart'
    seed(sphereCount);
  }
  if (keyCode == 80)// 'p' for 'pause'
  {
    isPaused = !isPaused;
    if (!isPaused) {
      unpausedTimer = millis();
    }
  }
  if (keyCode == 72)// 'h' for 'hide'
  {
    showInterface = !showInterface;
  }
  
  if (moveKeys.contains("" + key)){
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
