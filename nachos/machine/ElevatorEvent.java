// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * An event that affects elevator software.
 */
public final class ElevatorEvent {
	public ElevatorEvent(int event, int floor, int elevator) {
		this.event = event;
		this.floor = floor;
		this.elevator = elevator;
	}

	/** The event identifier. Refer to the <i>event*</i> constants. */
	public final int event;

	/** The floor pertaining to the event, or -1 if not applicable. */
	public final int floor;

	/** The elevator pertaining to the event, or -1 if not applicable. */
	public final int elevator;

	/** An up button was pressed. */
	public static final int eventUpButtonPressed = 0;

	/** A down button was pressed. */
	public static final int eventDownButtonPressed = 1;

	/** A floor button was pressed inside an elevator. */
	public static final int eventFloorButtonPressed = 2;

	/** An elevator has arrived and stopped at its destination floor. */
	public static final int eventElevatorArrived = 3;

	/** All riders have finished; the elevator controller should terminate. */
	public static final int eventRidersDone = 4;
}
