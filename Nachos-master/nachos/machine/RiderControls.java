// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * A set of controls that can be used by a rider controller. Each rider uses a
 * distinct <tt>RiderControls</tt> object.
 */
public interface RiderControls {
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
	 * Set the rider's interrupt handler. This handler will be called when the
	 * rider observes an event.
	 * 
	 * @param handler the rider's interrupt handler.
	 */
	public void setInterruptHandler(Runnable handler);

	/**
	 * Return the current location of the rider. If the rider is in motion, the
	 * returned value will be within one of the exact location.
	 * 
	 * @return the floor the rider is on.
	 */
	public int getFloor();

	/**
	 * Return an array specifying the sequence of floors at which this rider has
	 * successfully exited an elevator. This array naturally does not count the
	 * floor the rider started on, nor does it count floors where the rider is
	 * in the elevator and does not exit.
	 * 
	 * @return an array specifying the floors this rider has visited.
	 */
	public int[] getFloors();

	/**
	 * Return the indicated direction of the specified elevator, set by
	 * <tt>ElevatorControls.setDirectionDisplay()</tt>.
	 * 
	 * @param elevator the elevator to check the direction of.
	 * @return the displayed direction for the elevator.
	 * 
	 * @see nachos.machine.ElevatorControls#setDirectionDisplay
	 */
	public int getDirectionDisplay(int elevator);

	/**
	 * Press a direction button. If <tt>up</tt> is <tt>true</tt>, invoke
	 * <tt>pressUpButton()</tt>; otherwise, invoke <tt>pressDownButton()</tt>.
	 * 
	 * @param up <tt>true</tt> to press the up button, <tt>false</tt> to press
	 * the down button.
	 * @return the return value of <tt>pressUpButton()</tt> or of
	 * <tt>pressDownButton()</tt>.
	 */
	public boolean pressDirectionButton(boolean up);

	/**
	 * Press the up button. The rider must not be on the top floor and must not
	 * be inside an elevator. If an elevator is on the same floor as this rider,
	 * has the doors open, and says it is going up, does nothing and returns
	 * <tt>false</tt>.
	 * 
	 * @return <tt>true</tt> if the button event was sent to the elevator
	 * controller.
	 */
	public boolean pressUpButton();

	/**
	 * Press the down button. The rider must not be on the bottom floor and must
	 * not be inside an elevator. If an elevator is on the same floor as as this
	 * rider, has the doors open, and says it is going down, does nothing and
	 * returns <tt>false</tt>.
	 * 
	 * @return <tt>true</tt> if the button event was sent to the elevator
	 * controller.
	 */
	public boolean pressDownButton();

	/**
	 * Enter an elevator. A rider cannot enter an elevator if its doors are not
	 * open at the same floor, or if the elevator is full. The rider must not
	 * already be in an elevator.
	 * 
	 * @param elevator the elevator to enter.
	 * @return <tt>true</tt> if the rider successfully entered the elevator.
	 */
	public boolean enterElevator(int elevator);

	/**
	 * Press a floor button. The rider must be inside an elevator. If the
	 * elevator already has its doors open on <tt>floor</tt>, does nothing and
	 * returns <tt>false</tt>.
	 * 
	 * @param floor the button to press.
	 * @return <tt>true</tt> if the button event was sent to the elevator
	 * controller.
	 */
	public boolean pressFloorButton(int floor);

	/**
	 * Exit the elevator. A rider cannot exit the elevator if its doors are not
	 * open on the requested floor. The rider must already be in an elevator.
	 * 
	 * @param floor the floor to exit on.
	 * @return <tt>true</tt> if the rider successfully got off the elevator.
	 */
	public boolean exitElevator(int floor);

	/**
	 * Call when the rider is finished.
	 */
	public void finish();

	/**
	 * Return the next event in the event queue. Note that there may be multiple
	 * events pending when a rider interrupt occurs, so this method should be
	 * called repeatedly until it returns <tt>null</tt>.
	 * 
	 * @return the next event, or <tt>null</tt> if no further events are
	 * currently pending.
	 */
	public RiderEvent getNextEvent();
}
