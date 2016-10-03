// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * A set of controls that can be used by an elevator controller.
 */
public interface ElevatorControls {
	/**
	 * Return the number of floors in the elevator bank. If <i>n</i> is the
	 * number of floors in the bank, then the floors are numbered <i>0</i> (the
	 * ground floor) through <i>n - 1</i> (the top floor).
	 * 
	 * @return the number of floors in the bank.
	 */
	public int getNumFloors();

	/**
	 * Return the number of elevators in the elevator bank. If <i>n</i> is the
	 * number of elevators in the bank, then the elevators are numbered <i>0</i>
	 * through <i>n - 1</i>.
	 * 
	 * @return the numbe rof elevators in the bank.
	 */
	public int getNumElevators();

	/**
	 * Set the elevator interrupt handler. This handler will be called when an
	 * elevator event occurs, and when all the riders have reaced their
	 * destinations.
	 * 
	 * @param handler the elevator interrupt handler.
	 */
	public void setInterruptHandler(Runnable handler);

	/**
	 * Open an elevator's doors.
	 * 
	 * @param elevator which elevator's doors to open.
	 */
	public void openDoors(int elevator);

	/**
	 * Close an elevator's doors.
	 * 
	 * @param elevator which elevator's doors to close.
	 */
	public void closeDoors(int elevator);

	/**
	 * Move an elevator to another floor. The elevator's doors must be closed.
	 * If the elevator is already moving and cannot safely stop at the specified
	 * floor because it has already passed or is about to pass the floor, fails
	 * and returns <tt>false</tt>. If the elevator is already stopped at the
	 * specified floor, returns <tt>false</tt>.
	 * 
	 * @param floor the floor to move to.
	 * @param elevator the elevator to move.
	 * @return <tt>true</tt> if the elevator's destination was changed.
	 */
	public boolean moveTo(int floor, int elevator);

	/**
	 * Return the current location of the elevator. If the elevator is in
	 * motion, the returned value will be within one of the exact location.
	 * 
	 * @param elevator the elevator to locate.
	 * @return the floor the elevator is on.
	 */
	public int getFloor(int elevator);

	/**
	 * Set which direction the elevator bank will show for this elevator's
	 * display. The <i>direction</i> argument should be one of the <i>dir*</i>
	 * constants in the <tt>ElevatorBank</tt> class.
	 * 
	 * @param elevator the elevator whose direction display to set.
	 * @param direction the direction to show (up, down, or neither).
	 */
	public void setDirectionDisplay(int elevator, int direction);

	/**
	 * Call when the elevator controller is finished.
	 */
	public void finish();

	/**
	 * Return the next event in the event queue. Note that there may be multiple
	 * events pending when an elevator interrupt occurs, so this method should
	 * be called repeatedly until it returns <tt>null</tt>.
	 * 
	 * @return the next event, or <tt>null</tt> if no further events are
	 * currently pending.
	 */
	public ElevatorEvent getNextEvent();
}
