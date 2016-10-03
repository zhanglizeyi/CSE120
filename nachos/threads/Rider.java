package nachos.threads;

import nachos.machine.*;

/**
 * A single rider. Each rider accesses the elevator bank through an instance of
 * <tt>RiderControls</tt>.
 */
public class Rider implements RiderInterface {
	/**
	 * Allocate a new rider.
	 */
	public Rider() {
	}

	/**
	 * Initialize this rider. The rider will access the elevator bank through
	 * <i>controls</i>, and the rider will make stops at different floors as
	 * specified in <i>stops</i>. This method should return immediately after
	 * this rider is initialized, but not until the interrupt handler is set.
	 * The rider will start receiving events after this method returns,
	 * potentially before <tt>run()</tt> is called.
	 * 
	 * @param controls the rider's interface to the elevator bank. The rider
	 * must not attempt to access the elevator bank in <i>any</i> other way.
	 * @param stops an array of stops the rider should make; see below.
	 */
	public void initialize(RiderControls controls, int[] stops) {
	}

	/**
	 * Cause the rider to use the provided controls to make the stops specified
	 * in the constructor. The rider should stop at each of the floors in
	 * <i>stops</i>, an array of floor numbers. The rider should <i>only</i>
	 * make the specified stops.
	 * 
	 * <p>
	 * For example, suppose the rider uses <i>controls</i> to determine that it
	 * is initially on floor 1, and suppose the stops array contains two
	 * elements: { 0, 2 }. Then the rider should get on an elevator, get off on
	 * floor 0, get on an elevator, and get off on floor 2, pushing buttons as
	 * necessary.
	 * 
	 * <p>
	 * This method should not return, but instead should call
	 * <tt>controls.finish()</tt> when the rider is finished.
	 */
	public void run() {
	}
}
