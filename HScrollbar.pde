//Taken from: https://processing.org/examples/scrollbar.html and tweaked
class HScrollbar {
  int swidth, sheight;    // width and height of bar
  float xpos, ypos;       // x and y position of bar
  float spos, newspos;    // x position of slider
  float sposMin, sposMax; // max and min values of slider
  boolean over;           // is the mouse over the slider?
  boolean locked;
  float lerpedMinValue, lerpedMaxValue;
  String label;
  FloatFunction controller;
  boolean showValue;
  String minLabelValue, maxLabelValue;

  HScrollbar (float xp, float yp, int sw, int sh, float finalMin, float finalMax, String l, float defaultValue, FloatFunction lambdaController, boolean show, String minLV, String maxLV) {
    swidth = sw;
    sheight = sh;
    xpos = xp;
    ypos = yp-sheight/2;
    spos = defaultValue*(swidth-sheight)+xpos;
    newspos = spos;
    sposMin = xpos;
    sposMax = xpos + swidth - sheight;
    lerpedMinValue = finalMin;
    lerpedMaxValue = finalMax;
    label = l;
    controller = lambdaController;
    showValue = show;
    minLabelValue = minLV;
    maxLabelValue = maxLV;
  }

  void update() {
    if (overEvent()) {
      over = true;
    } else {
      over = false;
    }
    if (firstMousePress && over) {
      locked = true;
      cam.setLeftDragHandler(null);
    }
    if (!mousePressed) {
      locked = false;
      cam.setLeftDragHandler(PanDragHandler);
    }
    if (locked) {
      spos = constrain(mouseX-sheight/2, sposMin, sposMax);
    }
    controller.update(getValue());
  }


  void display() {
    noStroke();
    if (over || locked) {
      fill(150);
    } else {
      fill(100);
    }
    rect(xpos, ypos, swidth, sheight);
    textSize(10);
    fill(255);
    text(minLabelValue, xpos, ypos+sheight+10);
    text(maxLabelValue, xpos+swidth-(maxLabelValue.length()*5), ypos+sheight+10);
    if (over || locked) {
      fill(255);
    } else {
      fill(150);
    }
    rect(spos, ypos, sheight, sheight);
    textSize(30);
    fill(255);
    text(label, xpos, ypos-sheight+10);
    if (showValue) {
      textSize(10);
      fill(255);
      text(getValue(), spos-5, ypos+sheight+15);
    }
  }

  float getValue()
  {
    return lerp(lerpedMinValue, lerpedMaxValue, Math.round((float)(spos-xpos)/(sposMax-sposMin) * 100.0) / 100.0);
  }
  float constrain(float val, float minv, float maxv) {
    return min(max(val, minv), maxv);
  }

  boolean overEvent() {
    if (mouseX > xpos && mouseX < xpos+swidth &&
      mouseY > ypos && mouseY < ypos+sheight) {
      return true;
    } else {
      return false;
    }
  }
}
