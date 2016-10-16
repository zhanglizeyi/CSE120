package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;


		//queue for holding threads
		threadQueue = ThreadedKernel.scheduler.newThreadQueue(false);
		
		//initialized counter 
		threadCounter = 0;
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		/*TO-DO
			1. need to disable the thread (interrupt)
			2. queue need add current  thread
			3. put it in sleep to wait
			4. restore the back the interrupt
		*/

		if( counter !=  MAX_int )
		{
		
			boolean status = Machine.interrupt().disable();

			conditionLock.release();

			threadQueue.waitForAccess(KThread.currentThread());
			KThread.sleep();
			counter++;
		
			conditionLock.acquire();
   
			Machine.interrupt().restore(status);
		}
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		/*TO-DO
			1. interrupt again for next action 
			2. check if the queue still have queue left
			3. call ready to call it out 
			4. interrupt restore 
		*/

		if( counter != 0 )
		{
			boolean status = Machine.interrupt().disable();

				threadQueue.ready();	
				counter--;

			Machine.interrupt().restore(status);
		}
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		/*TO-DO
			1. interrupt disable 
			2. check if queue not empty 
			3. call wake, want them out 
			4. restore
		*/
		boolean status = Machine.interrupt().disable();

		while(counter != 0)
		{
			wake();
		}

		Machine.interrupt().restore(status);
	}

	private Lock conditionLock;

	//create queue container to hold threads
	private ThreadedKernel threadQueue;

	//counter 
	private int threadCounter;

	//TODO
	//SELFTEST AREA
	    // Place Condition2 testing code in the Condition2 class.

    // Example of the "interlock" pattern where two threads strictly
    // alternate their execution with each other using a condition
    // variable.  (Also see the slide showing this pattern at the end
    // of Lecture 6.)

    private static class InterlockTest {
        private static Lock lock;
        private static Condition2 cv;

        private static class Interlocker implements Runnable {
            public void run () {
                lock.acquire();
                for (int i = 0; i < 10; i++) {
                    System.out.println(KThread.currentThread().getName());
                    System.out.println(cv.wake());
                    cv.wake();    // signal
                    System.out.println(cv.sleep();)
                    cv.sleep();   // wait
                }
                lock.release();
            }
        }

        public InterlockTest () {
            lock = new Lock();
            cv = new Condition2(lock);

            KThread ping = new KThread(new Interlocker());
            ping.setName("ping");
            KThread pong = new KThread(new Interlocker());
            pong.setName("pong");

            ping.fork();
            pong.fork();

            // We need to wait for ping to finish, and the proper way
            // to do so is to join on ping.  (Note that, when ping is
            // done, pong is sleeping on the condition variable; if we
            // were also to join on pong, we would block forever.)
            // For this to work, join must be implemented.  If you
            // have not implemented join yet, then comment out the
            // call to join and instead uncomment the loop with
            // yields; the loop has the same effect, but is a kludgy
            // way to do it.
            ping.join();
            // for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
        }
    }

    // Invoke Condition2.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
        new InterlockTest();
    }

}
