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
		
		//creates a queue to hold all the sleeping threads
		this.sleepQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		//stores initial status
		boolean initStatus = Machine.interrupt().disable();
		
		//adds the current thread to the queue of sleeping threads
		sleepQueue.waitForAccess(KThread.currentThread());
	
		conditionLock.release();
		KThread.sleep();
		conditionLock.acquire();
	
		//restores initial status
		Machine.interrupt().restore(initStatus);
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		//stores initial status
		boolean initStatus = Machine.interrupt().disable();
		
		//remove the first thread from the set of sleep threads
		KThread nextThread = sleepQueue.nextThread();
		
		//if it exists, disable interrupt and "wake" it by calling ready()
		if(nextThread!=null)
		{
			nextThread.ready();
		}
		
		//restores initial status
		Machine.interrupt().restore(initStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean initStatus = Machine.interrupt().disable();
		
		KThread nextThread = sleepQueue.nextThread();
		
		while(nextThread!=null)
		{
			nextThread.ready();
			nextThread = sleepQueue.nextThread();
		}
		
		Machine.interrupt().restore(initStatus);
	}

	private Lock conditionLock;
	private ThreadQueue sleepQueue;


	    private static class InterlockTest {
        private static Lock lock;
        private static Condition2 cv;

        private static class Interlocker implements Runnable {
            public void run () {
                lock.acquire();
                for (int i = 0; i < 10; i++) {
                	System.out.println("running\n");
                    System.out.println(KThread.currentThread().getName());
                    cv.wake();   // signal
                    cv.sleep();  // wait
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
            KThread pingpong = new KThread(new Interlocker());
            //pingpong.setName("pingpong");

            ping.fork();
            pong.fork();
            //pingpong.fork();

            // We need to wait for ping to finish, and the proper way
            // to do so is to join on ping.  (Note that, when ping is
            // done, pong is sleeping on the condition variable; if we
            // were also to join on pong, we would block forever.)
            // For this to work, join must be implemented.  If you
            // have not implemented join yet, then comment out the
            // call to join and instead uncomment the loop with
            // yields; the loop has the same effect, but is a kludgy
            // way to do it.

            //ping need to wait until pong, and ping will finish before main
            ping.join();
            // for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
        }
    }

    // Invoke Condition2.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
        new InterlockTest();
    }
}
