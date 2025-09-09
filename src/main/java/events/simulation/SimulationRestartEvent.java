
package events.simulation;

import events.core.Event;

public record SimulationRestartEvent(int sphereCount) implements Event {
}
