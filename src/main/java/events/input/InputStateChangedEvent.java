package events.input;

import events.core.Event;

/** State changes events in the inputs. <i>Lucki there's a famili GUI.</i> */
public record InputStateChangedEvent(InputElement element, boolean newState) implements Event {
    public enum InputElement {
        SHIFT_KEY_DOWN,
    }
}
