package nachos.threads;

import nachos.machine.*;

/**
 * A <tt>Semaphore</tt> is a synchronization primitive with an unsigned value. A
 * semaphore has only two operations:
 * 
 * <ul>
 * <li><tt>P()</tt>: waits until the semaphore's value is greater than zero,
 * then decrements it.
 * <li><tt>V()</tt>: increments the semaphore's value, and wakes up one thread
 * waiting in <tt>P()</tt> if possible.
 * </ul>
 * 
 * <p>
 * Note that this API does not allow a thread to read the value of the semaphore
 * directly. Even if you did read the value, the only thing you would know is
 * what the value used to be. You don't know what the value is now, because by
 * the time you get the value, a context switch might have occurred, and some
 * other thread might have called <tt>P()</tt> or <tt>V()</tt>, so the true
 * value might now be different.
 */
public class Semaphore {
	/**
	 * Allocate a new semaphore.
	 * 
	 * @param initialValue the initial value of this semaphore.
	 */
	public Semaphore(int initialValue) {
		value = initialValue;
	}

	/**
	 * Atomically wait for this semaphore to become non-zero and decrement it.
	 */
	public void P() {
		boolean intStatus = Machine.interrupt().disable();

		if (value == 0) {
			waitQueue.waitForAccess(KThread.currentThread());
			KThread.sleep();
		}
		else {
			value--;
		}

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Atomically increment this semaphore and wake up at most one other thread
	 * sleeping on this semaphore.
	 */
	public void V() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = waitQueue.nextThread();
		if (thread != null) {
			thread.ready();
		}
		else {
			value++;
		}

		Machine.interrupt().restore(intStatus);
	}

	private static class PingTest implements Runnable {
		PingTest(Semaphore ping, Semaphore pong) {
			this.ping = ping;
			this.pong = pong;
		}

		public void run() {
			for (int i = 0; i < 10; i++) {
				ping.P();
				pong.V();
			}
		}

		private Semaphore ping;

		private Semaphore pong;
	}

	/**
	 * Test if this module is working.
	 */
	public static void selfTest() {
		Semaphore ping = new Semaphore(0);
		Semaphore pong = new Semaphore(0);

		new KThread(new PingTest(ping, pong)).setName("ping").fork();

		for (int i = 0; i < 10; i++) {
			ping.V();
			pong.P();
		}
	}

	private int value;

	private ThreadQueue waitQueue = ThreadedKernel.scheduler
			.newThreadQueue(false);
}
