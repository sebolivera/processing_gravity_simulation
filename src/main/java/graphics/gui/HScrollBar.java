package graphics.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import events.core.EventManager;
import events.graphics.gui.GUIHoverEvent;
import misc.MathUtils;
import processing.core.PApplet;

/**
 * Custom horizontal scroll bar GUI element that I probably stole from stackoverflow at some point,
 * but I can't be sure. <i>The 'H' stands for 'vertical'.</i>
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
    private final MathUtils.FloatFunction lambdaController;
    private final boolean valueShown;
    private final String minLabelValue;
    private final String maxLabelValue;
    private final PApplet parent;
    private final EventManager eventManager;
    private final GUIHandler guiEventManager;
    private final String scrollBarId;
    private float lastValue = Float.NaN;
    private final boolean useExponentialScale;
    private final float exponentialBase;

    /** Configuration for scroll bar positioning and dimensions. */
    public record ScrollBarGeometry(float xPosition, float yPosition, int sliderWidth, int sliderHeight) {}

    /** Configuration for scroll bar behavior and values. */
    public record ScrollBarValueRange(
            float lerpedMinValue,
            float lerpedMaxValue,
            float defaultValue,
            boolean useExponentialScale,
            float exponentialBase) {}

    /** Configuration for scroll bar display options. */
    public record ScrollBarDisplayOptions(
            String label, boolean valueShown, String minLabelValue, String maxLabelValue) {}

    /** Dependencies required by the scroll bar. */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "PApplet must be shared in Processing; Renderer never exposes app."
    )
    public record ScrollBarDependencies(
            PApplet parent,
            EventManager eventManager,
            GUIHandler guiEventManager,
            MathUtils.FloatFunction lambdaController,
            String scrollBarId) {}

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "PApplet must be shared in Processing; Renderer never exposes app."
    )
    public HScrollBar(
            final ScrollBarGeometry scrollBarGeometry,
            final ScrollBarValueRange scrollBarValueRange,
            final ScrollBarDisplayOptions scrollBarDisplayOptions,
            final ScrollBarDependencies scrollBarDependencies) {
        this.sliderWidth = scrollBarGeometry.sliderWidth;
        this.sliderHeight = scrollBarGeometry.sliderHeight;
        this.xPosition = scrollBarGeometry.xPosition;
        this.yPosition = scrollBarGeometry.yPosition - this.sliderHeight / 2.0f;
        this.sliderPositionMin = this.xPosition;
        this.sliderPositionMax = this.xPosition + this.sliderWidth - this.sliderHeight;

        this.lerpedMinValue = scrollBarValueRange.lerpedMinValue;
        this.lerpedMaxValue = scrollBarValueRange.lerpedMaxValue;
        this.useExponentialScale = scrollBarValueRange.useExponentialScale;
        this.exponentialBase = scrollBarValueRange.exponentialBase;

        this.label = scrollBarDisplayOptions.label;
        this.valueShown = scrollBarDisplayOptions.valueShown;
        this.minLabelValue = scrollBarDisplayOptions.minLabelValue;
        this.maxLabelValue = scrollBarDisplayOptions.maxLabelValue;

        this.parent = scrollBarDependencies.parent;
        this.eventManager = scrollBarDependencies.eventManager;
        this.guiEventManager = scrollBarDependencies.guiEventManager;
        this.lambdaController = scrollBarDependencies.lambdaController;
        this.scrollBarId = scrollBarDependencies.scrollBarId;

        if (scrollBarValueRange.useExponentialScale) {
            final float normalizedValue =
                    (scrollBarValueRange.defaultValue - scrollBarValueRange.lerpedMinValue)
                            / (scrollBarValueRange.lerpedMaxValue - scrollBarValueRange.lerpedMinValue);
            final float linearPosition =
                    (float)
                            (Math.log(normalizedValue * (scrollBarValueRange.exponentialBase - 1) + 1)
                                    / Math.log(scrollBarValueRange.exponentialBase));
            this.sliderPosition =
                    linearPosition * (this.sliderWidth - this.sliderHeight) + this.xPosition;
        } else {
            this.sliderPosition =
                    scrollBarValueRange.defaultValue * (this.sliderWidth - this.sliderHeight)
                            + this.xPosition;
        }
    }

    /** Updates the position and the value of the slider. */
    public void update() {
        final boolean wasHovered = isHovered;
        isHovered = overEvent();

        if (isHovered != wasHovered) {
            eventManager.publish(
                    new GUIHoverEvent(scrollBarId, isHovered, parent.mouseX, parent.mouseY));
        }

        if (parent.mousePressed && isHovered && !isLocked) {
            isLocked = true;
        }
        if (!parent.mousePressed) {
            isLocked = false;
        }
        if (isLocked) {
            sliderPosition =
                    constrain(
                            parent.mouseX - sliderHeight / 2.0f,
                            sliderPositionMin,
                            sliderPositionMax);
        }

        final float currentValue = getValue();
        if (Float.isNaN(lastValue) || Math.abs(currentValue - lastValue) > 0.001f) {
            lambdaController.update(currentValue);
            lastValue = currentValue;
        }
    }

    /** Displays the element on the GUI. <i>Well, there it is.</i> */
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
        parent.fill(255);
        parent.text(
                minLabelValue, xPosition, yPosition + sliderHeight + GUIHandler.DEFAULT_FONT_SIZE);
        parent.text(
                maxLabelValue,
                xPosition + sliderWidth - (minLabelValue.length() * 5),
                yPosition + sliderHeight + GUIHandler.DEFAULT_FONT_SIZE);

        if (isHovered || isLocked) {
            parent.fill(255);
        } else {
            parent.fill(150);
        }
        parent.pushMatrix();
        parent.translate(0, 0, 1);

        parent.rect(sliderPosition, yPosition, sliderHeight, sliderHeight);
        parent.popMatrix();

        if (guiEventManager.isFreeCamEnabled()) {
            parent.fill(128);
        } else {
            parent.fill(255);
        }
        parent.text(
                label, xPosition, yPosition - (sliderHeight + GUIHandler.DEFAULT_FONT_SIZE) / 2f);

        if (valueShown) {
            parent.fill(255, 200, 200);
            parent.textSize(GUIHandler.DEFAULT_FONT_SIZE * 0.5f);
            parent.text(
                    getValue(),
                    xPosition + sliderWidth,
                    yPosition + GUIHandler.DEFAULT_FONT_SIZE / 2f);
            parent.textSize(GUIHandler.DEFAULT_FONT_SIZE);
        }
    }

    /**
     * Returns the slider's value, either linear or exponential. <i>Show me what you're worth.</i>
     *
     * @return The slider value.
     */
    public float getValue() {
        final float normalizedPosition =
                (float)
                        (Math.round(
                                        (sliderPosition - xPosition)
                                                / (sliderPositionMax - sliderPositionMin)
                                                * 100.0)
                                / 100.0);

        if (useExponentialScale) {
            final float exponentialValue =
                    (float)
                            ((Math.pow(exponentialBase, normalizedPosition) - 1)
                                    / (exponentialBase - 1));
            return PApplet.lerp(lerpedMinValue, lerpedMaxValue, exponentialValue);
        } else {
            return PApplet.lerp(lerpedMinValue, lerpedMaxValue, normalizedPosition);
        }
    }

    /**
     * Clamps a value between a minimum and maximum.
     *
     * @param value The value to constrain.
     * @param minValue The minimum value.
     * @param maxValue The maximum value.
     * @return The constrained value.
     */
    private float constrain(final float value, final float minValue, final float maxValue) {
        return Math.min(Math.max(value, minValue), maxValue);
    }

    /**
     * Checks if the mouse is over the slider.
     *
     * @return Whether the mouse is over the slider.
     */
    public boolean overEvent() {
        final float mx = guiEventManager.getCursorX();
        final float my = guiEventManager.getCursorY();
        return mx > xPosition
                && mx < xPosition + sliderWidth
                && my > yPosition
                && my < yPosition + sliderHeight;
    }
}
