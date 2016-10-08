// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.threads.KThread;
import nachos.threads.Semaphore;

/**
 * Tests the <tt>ElevatorBank</tt> module, using a single elevator and a single
 * rider.
 */
public final class ElevatorTest {
	/**
	 * Allocate a new <tt>ElevatorTest</tt> object.
	 */
	public ElevatorTest() {
	}

	/**
	 * Run a test on <tt>Machine.bank()</tt>.
	 */
	public void run() {
		Machine.bank().init(1, 2, new ElevatorController());

		int[] stops = { 1 };

		Machine.bank().addRider(new Rider(), 0, stops);

		Machine.bank().run();
	}

	private class ElevatorController implements ElevatorControllerInterface {
		public void initialize(ElevatorControls controls) {
			this.controls = controls;

			eventWait = new Semaphore(0);

			controls.setInterruptHandler(new Runnable() {
				public void run() {
					interrupt();
				}
			});
		}

		public void run() {
			ElevatorEvent e;

			Lib.assertTrue(controls.getFloor(0) == 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventUpButtonPressed
					&& e.floor == 0);

			controls.setDirectionDisplay(0, dirUp);
			controls.openDoors(0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventFloorButtonPressed
					&& e.floor == 1);

			controls.closeDoors(0);
			controls.moveTo(1, 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventElevatorArrived
					&& e.floor == 1 && e.elevator == 0);

			controls.openDoors(0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventRidersDone);

			controls.finish();
			Lib.assertNotReached();
		}

		private void interrupt() {
			eventWait.V();
		}

		private ElevatorEvent getNextEvent() {
			ElevatorEvent event;
			while (true) {
				if ((event = controls.getNextEvent()) != null)
					break;

				eventWait.P();
			}
			return event;
		}

		private ElevatorControls controls;

		private Semaphore eventWait;
	}

	private class Rider implements RiderInterface {
		public void initialize(RiderControls controls, int[] stops) {
			this.controls = controls;
			Lib.assertTrue(stops.length == 1 && stops[0] == 1);

			eventWait = new Semaphore(0);

			controls.setInterruptHandler(new Runnable() {
				public void run() {
					interrupt();
				}
			});
		}

		public void run() {
			RiderEvent e;

			Lib.assertTrue(controls.getFloor() == 0);

			controls.pressUpButton();

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsOpened
					&& e.floor == 0 && e.elevator == 0);
			Lib.assertTrue(controls.getDirectionDisplay(0) == dirUp);

			Lib.assertTrue(controls.enterElevator(0));
			controls.pressFloorButton(1);

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsClosed
					&& e.floor == 0 && e.elevator == 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsOpened
					&& e.floor == 1 && e.elevator == 0);

			Lib.assertTrue(controls.exitElevator(1));

			controls.finish();
			Lib.assertNotReached();
		}

		private void interrupt() {
			eventWait.V();
		}

		private RiderEvent getNextEvent() {
			RiderEvent event;
			while (true) {
				if ((event = controls.getNextEvent()) != null)
					break;

				eventWait.P();
			}
			return event;
		}

		private RiderControls controls;

		private Semaphore eventWait;
	}
}
