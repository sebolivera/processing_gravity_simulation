package events.physics;

import events.core.Event;

/** Simulation pause event. <i>Now hold on a second...</i> */
public record SimulationPausedEvent(boolean paused) implements Event {}
