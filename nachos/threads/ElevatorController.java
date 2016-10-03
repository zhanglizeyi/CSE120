package nachos.threads;

import nachos.machine.*;

/**
 * A controller for all the elevators in an elevator bank. The controller
 * accesses the elevator bank through an instance of <tt>ElevatorControls</tt>.
 */
public class ElevatorController implements ElevatorControllerInterface {
	/**
	 * Allocate a new elevator controller.
	 */
	public ElevatorController() {
	}

	/**
	 * Initialize this elevator controller. The controller will access the
	 * elevator bank through <i>controls</i>. This constructor should return
	 * immediately after this controller is initialized, but not until the
	 * interupt handler is set. The controller will start receiving events after
	 * this method returns, but potentially before <tt>run()</tt> is called.
	 * 
	 * @param controls the controller's interface to the elevator bank. The
	 * controler must not attempt to access the elevator bank in <i>any</i>
	 * other way.
	 */
	public void initialize(ElevatorControls controls) {
	}

	/**
	 * Cause the controller to use the provided controls to receive and process
	 * requests from riders. This method should not return, but instead should
	 * call <tt>controls.finish()</tt> when the controller is finished.
	 */
	public void run() {
	}
}
