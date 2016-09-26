// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.threads.KThread;

import java.util.Vector;
import java.security.PrivilegedAction;

/**
 * A TCB simulates the low-level details necessary to create, context-switch,
 * and destroy Nachos threads. Each TCB controls an underlying JVM Thread
 * object.
 * 
 * <p>
 * Do not use any methods in <tt>java.lang.Thread</tt>, as they are not
 * compatible with the TCB API. Most <tt>Thread</tt> methods will either crash
 * Nachos or have no useful effect.
 * 
 * <p>
 * Do not use the <i>synchronized</i> keyword <b>anywhere</b> in your code. It's
 * against the rules, <i>and</i> it can easily deadlock nachos.
 */
public final class TCB {
	/**
	 * Allocate a new TCB.
	 */
	public TCB() {
	}

	/**
	 * Give the TCB class the necessary privilege to create threads. This is
	 * necessary, because unlike other machine classes that need privilege, we
	 * want the kernel to be able to create TCB objects on its own.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 */
	public static void givePrivilege(Privilege privilege) {
		TCB.privilege = privilege;
		privilege.tcb = new TCBPrivilege();
	}

	/**
	 * Causes the thread represented by this TCB to begin execution. The
	 * specified target is run in the thread.
	 */
	public void start(Runnable target) {
		/*
		 * We will not use synchronization here, because we're assuming that
		 * either this is the first call to start(), or we're being called in
		 * the context of another TCB. Since we only allow one TCB to run at a
		 * time, no synchronization is necessary.
		 * 
		 * The only way this assumption could be broken is if one of our
		 * non-Nachos threads used the TCB code.
		 */

		/*
		 * Make sure this TCB has not already been started. If done is false,
		 * then destroy() has not yet set javaThread back to null, so we can use
		 * javaThread as a reliable indicator of whether or not start() has
		 * already been invoked.
		 */
		Lib.assertTrue(javaThread == null && !done);

		/*
		 * Make sure there aren't too many running TCBs already. This limitation
		 * exists in an effort to prevent wild thread usage.
		 */
		Lib.assertTrue(runningThreads.size() < maxThreads);

		isFirstTCB = (currentTCB == null);

		/*
		 * Probably unnecessary sanity check: if this is not the first TCB, we
		 * make sure that the current thread is bound to the current TCB. This
		 * check can only fail if non-Nachos threads invoke start().
		 */
		if (!isFirstTCB)
			Lib.assertTrue(currentTCB.javaThread == Thread.currentThread());

		/*
		 * At this point all checks are complete, so we go ahead and start the
		 * TCB. Whether or not this is the first TCB, it gets added to
		 * runningThreads, and we save the target closure.
		 */
		runningThreads.add(this);

		this.target = target;

		if (!isFirstTCB) {
			/*
			 * If this is not the first TCB, we have to make a new Java thread
			 * to run it. Creating Java threads is a privileged operation.
			 */
			tcbTarget = new Runnable() {
				public void run() {
					threadroot();
				}
			};

			privilege.doPrivileged(new Runnable() {
				public void run() {
					javaThread = new Thread(tcbTarget);
				}
			});

			/*
			 * The Java thread hasn't yet started, but we need to get it
			 * blocking in yield(). We do this by temporarily turning off the
			 * current TCB, starting the new Java thread, and waiting for it to
			 * wake us up from threadroot(). Once the new TCB wakes us up, it's
			 * safe to context switch to the new TCB.
			 */
			currentTCB.running = false;

			this.javaThread.start();
			currentTCB.waitForInterrupt();
		}
		else {
			/*
			 * This is the first TCB, so we don't need to make a new Java thread
			 * to run it; we just steal the current Java thread.
			 */
			javaThread = Thread.currentThread();

			/* All we have to do now is invoke threadroot() directly. */
			threadroot();
		}
	}

	/**
	 * Return the TCB of the currently running thread.
	 */
	public static TCB currentTCB() {
		return currentTCB;
	}

	/**
	 * Context switch between the current TCB and this TCB. This TCB will become
	 * the new current TCB. It is acceptable for this TCB to be the current TCB.
	 */
	public void contextSwitch() {
		/*
		 * Probably unnecessary sanity check: we make sure that the current
		 * thread is bound to the current TCB. This check can only fail if
		 * non-Nachos threads invoke start().
		 */
		Lib.assertTrue(currentTCB.javaThread == Thread.currentThread());

		// make sure AutoGrader.runningThread() called associateThread()
		Lib.assertTrue(currentTCB.associated);
		currentTCB.associated = false;

		// can't switch from a TCB to itself
		if (this == currentTCB)
			return;

		/*
		 * There are some synchronization concerns here. As soon as we wake up
		 * the next thread, we cannot assume anything about static variables, or
		 * about any TCB's state. Therefore, before waking up the next thread,
		 * we must latch the value of currentTCB, and set its running flag to
		 * false (so that, in case we get interrupted before we call yield(),
		 * the interrupt will set the running flag and yield() won't block).
		 */

		TCB previous = currentTCB;
		previous.running = false;

		this.interrupt();
		previous.yield();
	}

	/**
	 * Destroy this TCB. This TCB must not be in use by the current thread. This
	 * TCB must also have been authorized to be destroyed by the autograder.
	 */
	public void destroy() {
		// make sure the current TCB is correct
		Lib.assertTrue(currentTCB != null
				&& currentTCB.javaThread == Thread.currentThread());
		// can't destroy current thread
		Lib.assertTrue(this != currentTCB);
		// thread must have started but not be destroyed yet
		Lib.assertTrue(javaThread != null && !done);

		// ensure AutoGrader.finishingCurrentThread() called authorizeDestroy()
		Lib.assertTrue(nachosThread == toBeDestroyed);
		toBeDestroyed = null;

		this.done = true;
		currentTCB.running = false;

		this.interrupt();
		currentTCB.waitForInterrupt();

		this.javaThread = null;
	}

	/**
	 * Destroy all TCBs and exit Nachos. Same as <tt>Machine.terminate()</tt>.
	 */
	public static void die() {
		privilege.exit(0);
	}

	/**
	 * Test if the current JVM thread belongs to a Nachos TCB. The AWT event
	 * dispatcher is an example of a non-Nachos thread.
	 * 
	 * @return <tt>true</tt> if the current JVM thread is a Nachos thread.
	 */
	public static boolean isNachosThread() {
		return (currentTCB != null && Thread.currentThread() == currentTCB.javaThread);
	}

	private void threadroot() {
		// this should be running the current thread
		Lib.assertTrue(javaThread == Thread.currentThread());

		if (!isFirstTCB) {
			/*
			 * start() is waiting for us to wake it up, signalling that it's OK
			 * to context switch to us. We leave the running flag false so that
			 * we'll still run if a context switch happens before we go to
			 * sleep. All we have to do is wake up the current TCB and then wait
			 * to get woken up by contextSwitch() or destroy().
			 */

			currentTCB.interrupt();
			this.yield();
		}
		else {
			/*
			 * start() called us directly, so we just need to initialize a
			 * couple things.
			 */

			currentTCB = this;
			running = true;
		}

		try {
			target.run();

			// no way out of here without going throw one of the catch blocks
			Lib.assertNotReached();
		}
		catch (ThreadDeath e) {
			// make sure this TCB is being destroyed properly
			if (!done) {
				System.out.print("\nTCB terminated improperly!\n");
				privilege.exit(1);
			}

			runningThreads.removeElement(this);
			if (runningThreads.isEmpty())
				privilege.exit(0);
		}
		catch (Throwable e) {
			System.out.print("\n");
			e.printStackTrace();

			runningThreads.removeElement(this);
			if (runningThreads.isEmpty())
				privilege.exit(1);
			else
				die();
		}
	}

	/**
	 * Invoked by threadroot() and by contextSwitch() when it is necessary to
	 * wait for another TCB to context switch to this TCB. Since this TCB might
	 * get destroyed instead, we check the <tt>done</tt> flag after waking up.
	 * If it is set, the TCB that woke us up is waiting for an acknowledgement
	 * in destroy(). Otherwise, we just set the current TCB to this TCB and
	 * return.
	 */
	private void yield() {
		waitForInterrupt();

		if (done) {
			currentTCB.interrupt();
			throw new ThreadDeath();
		}

		currentTCB = this;
	}

	/**
	 * Waits on the monitor bound to this TCB until its <tt>running</tt> flag is
	 * set to <tt>true</tt>. <tt>waitForInterrupt()</tt> is used whenever a TCB
	 * needs to go to wait for its turn to run. This includes the ping-pong
	 * process of starting and destroying TCBs, as well as in context switching
	 * from this TCB to another. We don't rely on <tt>currentTCB</tt>, since it
	 * is updated by <tt>contextSwitch()</tt> before we get called.
	 */
	private synchronized void waitForInterrupt() {
		while (!running) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Wake up this TCB by setting its <tt>running</tt> flag to <tt>true</tt>
	 * and signalling the monitor bound to it. Used in the ping-pong process of
	 * starting and destroying TCBs, as well as in context switching to this
	 * TCB.
	 */
	private synchronized void interrupt() {
		running = true;
		notify();
	}

	private void associateThread(KThread thread) {
		// make sure AutoGrader.runningThread() gets called only once per
		// context switch
		Lib.assertTrue(!associated);
		associated = true;

		Lib.assertTrue(thread != null);

		if (nachosThread != null)
			Lib.assertTrue(thread == nachosThread);
		else
			nachosThread = thread;
	}

	private static void authorizeDestroy(KThread thread) {
		// make sure AutoGrader.finishingThread() gets called only once per
		// destroy
		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = thread;
	}

	/**
	 * The maximum number of started, non-destroyed TCB's that can be in
	 * existence.
	 */
	public static final int maxThreads = 250;

	/**
	 * A reference to the currently running TCB. It is initialized to
	 * <tt>null</tt> when the <tt>TCB</tt> class is loaded, and then the first
	 * invocation of <tt>start(Runnable)</tt> assigns <tt>currentTCB</tt> a
	 * reference to the first TCB. After that, only <tt>yield()</tt> can change
	 * <tt>currentTCB</tt> to the current TCB, and only after
	 * <tt>waitForInterrupt()</tt> returns.
	 * 
	 * <p>
	 * Note that <tt>currentTCB.javaThread</tt> will not be the current thread
	 * if the current thread is not bound to a TCB (this includes the threads
	 * created for the hardware simulation).
	 */
	private static TCB currentTCB = null;

	/**
	 * A vector containing all <i>running</i> TCB objects. It is initialized to
	 * an empty vector when the <tt>TCB</tt> class is loaded. TCB objects are
	 * added only in <tt>start(Runnable)</tt>, which can only be invoked once on
	 * each TCB object. TCB objects are removed only in each of the
	 * <tt>catch</tt> clauses of <tt>threadroot()</tt>, one of which is always
	 * invoked on thread termination. The maximum number of threads in
	 * <tt>runningThreads</tt> is limited to <tt>maxThreads</tt> by
	 * <tt>start(Runnable)</tt>. If <tt>threadroot()</tt> drops the number of
	 * TCB objects in <tt>runningThreads</tt> to zero, Nachos exits, so once the
	 * first TCB is created, this vector is basically never empty.
	 */
	private static Vector<TCB> runningThreads = new Vector<TCB>();

	private static Privilege privilege;

	private static KThread toBeDestroyed = null;

	/**
	 * <tt>true</tt> if and only if this TCB is the first TCB to start, the one
	 * started in <tt>Machine.main(String[])</tt>. Initialized by
	 * <tt>start(Runnable)</tt>, on the basis of whether <tt>currentTCB</tt> has
	 * been initialized.
	 */
	private boolean isFirstTCB;

	/**
	 * A reference to the Java thread bound to this TCB. It is initially
	 * <tt>null</tt>, assigned to a Java thread in <tt>start(Runnable)</tt>, and
	 * set to <tt>null</tt> again in <tt>destroy()</tt>.
	 */
	private Thread javaThread = null;

	/**
	 * <tt>true</tt> if and only if the Java thread bound to this TCB ought to
	 * be running. This is an entirely different condition from membership in
	 * <tt>runningThreads</tt>, which contains all TCB objects that have started
	 * and have not terminated. <tt>running</tt> is only <tt>true</tt> when the
	 * associated Java thread ought to run ASAP. When starting or destroying a
	 * TCB, this is temporarily true for a thread other than that of the current
	 * TCB.
	 */
	private boolean running = false;

	/**
	 * Set to <tt>true</tt> by <tt>destroy()</tt>, so that when
	 * <tt>waitForInterrupt()</tt> returns in the doomed TCB, <tt>yield()</tt>
	 * will know that the current TCB is doomed.
	 */
	private boolean done = false;

	private KThread nachosThread = null;

	private boolean associated = false;

	private Runnable target;

	private Runnable tcbTarget;

	private static class TCBPrivilege implements Privilege.TCBPrivilege {
		public void associateThread(KThread thread) {
			Lib.assertTrue(currentTCB != null);
			currentTCB.associateThread(thread);
		}

		public void authorizeDestroy(KThread thread) {
			TCB.authorizeDestroy(thread);
		}
	}
}
