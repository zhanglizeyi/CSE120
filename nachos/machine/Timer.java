// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * A hardware timer generates a CPU timer interrupt approximately every 500
 * clock ticks. This means that it can be used for implementing time-slicing, or
 * for having a thread go to sleep for a specific period of time.
 * 
 * The <tt>Timer</tt> class emulates a hardware timer by scheduling a timer
 * interrupt to occur every time approximately 500 clock ticks pass. There is a
 * small degree of randomness here, so interrupts do not occur exactly every 500
 * ticks.
 */
public final class Timer {
	/**
	 * Allocate a new timer.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 */
	public Timer(Privilege privilege) {
		System.out.print(" timer");

		this.privilege = privilege;

		timerInterrupt = new Runnable() {
			public void run() {
				timerInterrupt();
			}
		};

		autoGraderInterrupt = new Runnable() {
			public void run() {
				Machine.autoGrader().timerInterrupt(Timer.this.privilege,
						lastTimerInterrupt);
			}
		};

		scheduleInterrupt();
	}

	/**
	 * Set the callback to use as a timer interrupt handler. The timer interrupt
	 * handler will be called approximately every 500 clock ticks.
	 * 
	 * @param handler the timer interrupt handler.
	 */
	public void setInterruptHandler(Runnable handler) {
		this.handler = handler;
	}

	/**
	 * Get the current time.
	 * 
	 * @return the number of clock ticks since Nachos started.
	 */
	public long getTime() {
		return privilege.stats.totalTicks;
	}

	private void timerInterrupt() {
		scheduleInterrupt();
		scheduleAutoGraderInterrupt();

		lastTimerInterrupt = getTime();

		if (handler != null)
			handler.run();
	}

	private void scheduleInterrupt() {
		int delay = Stats.TimerTicks;
		delay += Lib.random(delay / 10) - (delay / 20);

		privilege.interrupt.schedule(delay, "timer", timerInterrupt);
	}

	private void scheduleAutoGraderInterrupt() {
		privilege.interrupt.schedule(1, "timerAG", autoGraderInterrupt);
	}

	private long lastTimerInterrupt;

	private Runnable timerInterrupt;

	private Runnable autoGraderInterrupt;

	private Privilege privilege;

	private Runnable handler = null;
}
