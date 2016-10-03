package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * An implementation of condition variables built upon semaphores.
 * 
 * <p>
 * A condition variable is a synchronization primitive that does not have a
 * value (unlike a semaphore or a lock), but threads may still be queued.
 * 
 * <p>
 * <ul>
 * 
 * <li><tt>sleep()</tt>: atomically release the lock and relinkquish the CPU
 * until woken; then reacquire the lock.
 * 
 * <li><tt>wake()</tt>: wake up a single thread sleeping in this condition
 * variable, if possible.
 * 
 * <li><tt>wakeAll()</tt>: wake up all threads sleeping inn this condition
 * variable.
 * 
 * </ul>
 * 
 * <p>
 * Every condition variable is associated with some lock. Multiple condition
 * variables may be associated with the same lock. All three condition variable
 * operations can only be used while holding the associated lock.
 * 
 * <p>
 * In Nachos, condition variables are summed to obey <i>Mesa-style</i>
 * semantics. When a <tt>wake()</tt> or <tt>wakeAll()</tt> wakes up another
 * thread, the woken thread is simply put on the ready list, and it is the
 * responsibility of the woken thread to reacquire the lock (this reacquire is
 * taken core of in <tt>sleep()</tt>).
 * 
 * <p>
 * By contrast, some implementations of condition variables obey
 * <i>Hoare-style</i> semantics, where the thread that calls <tt>wake()</tt>
 * gives up the lock and the CPU to the woken thread, which runs immediately and
 * gives the lock and CPU back to the waker when the woken thread exits the
 * critical section.
 * 
 * <p>
 * The consequence of using Mesa-style semantics is that some other thread can
 * acquire the lock and change data structures, before the woken thread gets a
 * chance to run. The advance to Mesa-style semantics is that it is a lot easier
 * to implement.
 */
public class Condition {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition(Lock conditionLock) {
		this.conditionLock = conditionLock;

		waitQueue = new LinkedList<Semaphore>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 * 
	 * <p>
	 * This implementation uses semaphores to implement this, by allocating a
	 * semaphore for each waiting thread. The waker will <tt>V()</tt> this
	 * semaphore, so thre is no chance the sleeper will miss the wake-up, even
	 * though the lock is released before caling <tt>P()</tt>.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		Semaphore waiter = new Semaphore(0);
		waitQueue.add(waiter);

		conditionLock.release();
		waiter.P();
		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		if (!waitQueue.isEmpty())
			((Semaphore) waitQueue.removeFirst()).V();
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		while (!waitQueue.isEmpty())
			wake();
	}

	private Lock conditionLock;

	private LinkedList<Semaphore> waitQueue;
}
