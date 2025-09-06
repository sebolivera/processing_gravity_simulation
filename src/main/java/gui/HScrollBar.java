package gui;

import events.core.EventManager;
import events.gui.GUIHoverEvent;
import events.gui.GUIManager;
import misc.MathUtils;
import processing.core.PApplet;

/**
 * Custom horizontal scroll bar GUI element that I probably stole from stackoverflow at some point, but I can't be sure.
 * <i>The 'H' stands for 'vertical'.</i>
 */
public class HScrollBar {
    private final int sliderWidth;
    private final int sliderHeight;
    private final float xPosition;
    private final float yPosition;
    private float sliderPosition;
    private final float sliderPositionMin;
    private final float sliderPositionMax;
    private boolean isHovered;
    private boolean isLocked;
    private final float lerpedMinValue;
    private final float lerpedMaxValue;
    private final String label;
    private MathUtils.FloatFunction lambdaController;
    private boolean valueShown;
    private final String minLabelValue;
    private final String maxLabelValue;
    private final PApplet parent;
    private final EventManager eventManager;
    private final GUIManager guiEventManager;
    private final String scrollBarId;
    private float lastValue = Float.NaN;


    public HScrollBar(
            float xPosition,
            float yPosition,
            int sliderWidth,
            int sliderHeight,
            float lerpedMinValue,
            float lerpedMaxValue,
            String label,
            float defaultValue,
            MathUtils.FloatFunction lambdaController,
            boolean valueShown,
            String minLabelValue,
            String maxLabelValue,
            PApplet parent,
            EventManager eventManager,
            GUIManager guiEventManager,
            String scrollBarId
    ) {
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
        this.xPosition = xPosition;
        this.yPosition = yPosition - this.sliderHeight / 2.0f;
        this.sliderPosition = defaultValue * (this.sliderWidth - this.sliderHeight) + this.xPosition;
        this.sliderPositionMin = this.xPosition;
        this.sliderPositionMax = this.xPosition + this.sliderWidth - this.sliderHeight;
        this.lerpedMinValue = lerpedMinValue;
        this.lerpedMaxValue = lerpedMaxValue;
        this.label = label;
        this.lambdaController = lambdaController;
        this.valueShown = valueShown;
        this.minLabelValue = minLabelValue;
        this.maxLabelValue = maxLabelValue;
        this.parent = parent;
        this.eventManager = eventManager;
        this.guiEventManager = guiEventManager;
        this.scrollBarId = scrollBarId;
    }

    public void update() {
        boolean wasHovered = isHovered;
        isHovered = overEvent();

        if (isHovered != wasHovered) {
            eventManager.publish(
                    new GUIHoverEvent(
                            scrollBarId,
                            isHovered,
                            parent.mouseX,
                            parent.mouseY
                    )
            );
        }

        if (parent.mousePressed && isHovered && !isLocked) {
            isLocked = true;
        }
        if (!parent.mousePressed) {
            isLocked = false;
        }
        if (isLocked) {
            sliderPosition = constrain(parent.mouseX - sliderHeight / 2.0f, sliderPositionMin, sliderPositionMax);
        }

        float currentValue = getValue();
        if (Float.isNaN(lastValue) || Math.abs(currentValue - lastValue) > 0.001f) {
            lambdaController.update(currentValue);
            lastValue = currentValue;
        }

    }

    /**
     * Displays the element on the GUI.
     * <i>Well, there it is.</i>
     */
    public void display() {
        parent.noStroke();

        if (guiEventManager.isFreeCamEnabled()) {
            parent.fill(50);
        } else if (isHovered || isLocked) {
            parent.fill(150);
        } else {
            parent.fill(100);
        }
        parent.rect(xPosition, yPosition, sliderWidth, sliderHeight);
        parent.textSize(10);
        parent.fill(255);
        parent.text(minLabelValue, xPosition, yPosition + sliderHeight + 10);
        parent.text(maxLabelValue, xPosition + sliderWidth - (minLabelValue.length() * 5), yPosition + sliderHeight + 10);

        if (isHovered || isLocked) {
            parent.fill(255);
        } else {
            parent.fill(150);
        }
        parent.rect(sliderPosition, yPosition, sliderHeight, sliderHeight);
        parent.textSize(30);

        if (guiEventManager.isFreeCamEnabled()) {
            parent.fill(128);
        } else {
            parent.fill(255);
        }
        parent.text(label, xPosition, yPosition - sliderHeight + 10);

        if (valueShown) {
            parent.textSize(10);
            parent.fill(255);
            parent.text(getValue(), sliderPosition - 5, yPosition + sliderHeight + 15);
        }
    }

    /**
     * Returns the slider's value.
     * <i>Show me what you're worth.</i>
     * @return The slider value.
     */
    public float getValue() {
        return PApplet.lerp(
                lerpedMinValue,
                lerpedMaxValue,
                (float) (Math.round(
                        (sliderPosition - xPosition) / (sliderPositionMax - sliderPositionMin) * 100.0) / 100.0)
        );
    }

    private float constrain(float value, float minValue, float maxValue) {
        return Math.min(Math.max(value, minValue), maxValue);
    }


    public boolean overEvent() {
        return parent.mouseX > xPosition && parent.mouseX < xPosition + sliderWidth &&
                parent.mouseY > yPosition && parent.mouseY < yPosition + sliderHeight;
    }

    public String getId() {
        return scrollBarId;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public void setValueShown(boolean value) {
        this.valueShown = value;
    }

    public boolean getvalueShown() {
        return valueShown;
    }
}
