// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * An event that affects rider software. If a rider is outside the elevators, it
 * will only receive events on the same floor as the rider. If a rider is inside
 * an elevator, it will only receive events pertaining to that elevator.
 */
public final class RiderEvent {
	public RiderEvent(int event, int floor, int elevator, int direction) {
		this.event = event;
		this.floor = floor;
		this.elevator = elevator;
		this.direction = direction;
	}

	/** The event identifier. Refer to the <i>event*</i> constants. */
	public final int event;

	/** The floor pertaining to the event, or -1 if not applicable. */
	public final int floor;

	/** The elevator pertaining to the event, or -1 if not applicable. */
	public final int elevator;

	/** The direction display of the elevator (neither if not applicable). */
	public final int direction;

	/** An elevator's doors have opened. */
	public static final int eventDoorsOpened = 0;

	/** An elevator's doors were open and its direction display changed. */
	public static final int eventDirectionChanged = 1;

	/** An elevator's doors have closed. */
	public static final int eventDoorsClosed = 2;
}
