package events.physics;

import events.core.Event;

/**
 * Simulation pause event.
 * <i>Now hold on a second...</i>
 */
public class SimulationPausedEvent implements Event {
    private final boolean paused;

    public SimulationPausedEvent(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() { return paused; }
}
