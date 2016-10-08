package nachos.threads;

/**
 * Schedules access to some sort of resource with limited access constraints. A
 * thread queue can be used to share this limited access among multiple threads.
 * 
 * <p>
 * Examples of limited access in Nachos include:
 * 
 * <ol>
 * <li>the right for a thread to use the processor. Only one thread may run on
 * the processor at a time.
 * 
 * <li>the right for a thread to acquire a specific lock. A lock may be held by
 * only one thread at a time.
 * 
 * <li>the right for a thread to return from <tt>Semaphore.P()</tt> when the
 * semaphore is 0. When another thread calls <tt>Semaphore.V()</tt>, only one
 * thread waiting in <tt>Semaphore.P()</tt> can be awakened.
 * 
 * <li>the right for a thread to be woken while sleeping on a condition
 * variable. When another thread calls <tt>Condition.wake()</tt>, only one
 * thread sleeping on the condition variable can be awakened.
 * 
 * <li>the right for a thread to return from <tt>KThread.join()</tt>. Threads
 * are not allowed to return from <tt>join()</tt> until the target thread has
 * finished.
 * </ol>
 * 
 * All these cases involve limited access because, for each of them, it is not
 * necessarily possible (or correct) for all the threads to have simultaneous
 * access. Some of these cases involve concrete resources (e.g. the processor,
 * or a lock); others are more abstract (e.g. waiting on semaphores, condition
 * variables, or join).
 * 
 * <p>
 * All thread queue methods must be invoked with <b>interrupts disabled</b>.
 */
public abstract class ThreadQueue {
	/**
	 * Notify this thread queue that the specified thread is waiting for access.
	 * This method should only be called if the thread cannot immediately obtain
	 * access (e.g. if the thread wants to acquire a lock but another thread
	 * already holds the lock).
	 * 
	 * <p>
	 * A thread must not simultaneously wait for access to multiple resources.
	 * For example, a thread waiting for a lock must not also be waiting to run
	 * on the processor; if a thread is waiting for a lock it should be
	 * sleeping.
	 * 
	 * <p>
	 * However, depending on the specific objects, it may be acceptable for a
	 * thread to wait for access to one object while having access to another.
	 * For example, a thread may attempt to acquire a lock while holding another
	 * lock. Note, though, that the processor cannot be held while waiting for
	 * access to anything else.
	 * 
	 * @param thread the thread waiting for access.
	 */
	public abstract void waitForAccess(KThread thread);

	/**
	 * Notify this thread queue that another thread can receive access. Choose
	 * and return the next thread to receive access, or <tt>null</tt> if there
	 * are no threads waiting.
	 * 
	 * <p>
	 * If the limited access object transfers priority, and if there are other
	 * threads waiting for access, then they will donate priority to the
	 * returned thread.
	 * 
	 * @return the next thread to receive access, or <tt>null</tt> if there are
	 * no threads waiting.
	 */
	public abstract KThread nextThread();

	/**
	 * Notify this thread queue that a thread has received access, without going
	 * through <tt>request()</tt> and <tt>nextThread()</tt>. For example, if a
	 * thread acquires a lock that no other threads are waiting for, it should
	 * call this method.
	 * 
	 * <p>
	 * This method should not be called for a thread returned from
	 * <tt>nextThread()</tt>.
	 * 
	 * @param thread the thread that has received access, but was not returned
	 * from <tt>nextThread()</tt>.
	 */
	public abstract void acquire(KThread thread);

	/**
	 * Print out all the threads waiting for access, in no particular order.
	 */
	public abstract void print();
}
