package events.physics;

import events.core.Event;

/** Event fired when global speed changes. <i>WE HAV REAHCED MXAIMUN VLELOCIPY</i> */
public record SpeedChangedEvent(int newSpeed) implements Event {}
