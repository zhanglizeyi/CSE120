// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.security;

import nachos.machine.*;
import nachos.threads.KThread;

import java.util.LinkedList;
import java.util.Iterator;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * A capability that allows privileged access to the Nachos machine.
 * 
 * <p>
 * Some privileged operations are guarded by the Nachos security manager:
 * <ol>
 * <li>creating threads
 * <li>writing/deleting files in the test directory
 * <li>exit with specific status code
 * </ol>
 * These operations can only be performed through <tt>doPrivileged()</tt>.
 * 
 * <p>
 * Some privileged operations require a capability:
 * <ol>
 * <li>scheduling interrupts
 * <li>advancing the simulated time
 * <li>accessing machine statistics
 * <li>installing a console
 * <li>flushing the simulated processor's pipeline
 * <li>approving TCB operations
 * </ol>
 * These operations can be directly performed using a <tt>Privilege</tt> object.
 * 
 * <p>
 * The Nachos kernel should <i>never</i> be able to directly perform any of
 * these privileged operations. If you have discovered a loophole somewhere,
 * notify someone.
 */
public abstract class Privilege {
	/**
	 * Allocate a new <tt>Privilege</tt> object. Note that this object in itself
	 * does not encapsulate privileged access until the machine devices fill it
	 * in.
	 */
	public Privilege() {
	}

	/**
	 * Perform the specified action with privilege.
	 * 
	 * @param action the action to perform.
	 */
	public abstract void doPrivileged(Runnable action);

	/**
	 * Perform the specified <tt>PrivilegedAction</tt> with privilege.
	 * 
	 * @param action the action to perform.
	 * @return the return value of the action.
	 */
	public abstract Object doPrivileged(PrivilegedAction action);

	/**
	 * Perform the specified <tt>PrivilegedExceptionAction</tt> with privilege.
	 * 
	 * @param action the action to perform.
	 * @return the return value of the action.
	 */
	public abstract Object doPrivileged(PrivilegedExceptionAction action)
			throws PrivilegedActionException;

	/**
	 * Exit Nachos with the specified status.
	 * 
	 * @param exitStatus the exit status of the Nachos process.
	 */
	public abstract void exit(int exitStatus);

	/**
	 * Add an <tt>exit()</tt> notification handler. The handler will be invoked
	 * by exit().
	 * 
	 * @param handler the notification handler.
	 */
	public void addExitNotificationHandler(Runnable handler) {
		exitNotificationHandlers.add(handler);
	}

	/**
	 * Invoke each <tt>exit()</tt> notification handler added by
	 * <tt>addExitNotificationHandler()</tt>. Called by <tt>exit()</tt>.
	 */
	protected void invokeExitNotificationHandlers() {
		for (Iterator i = exitNotificationHandlers.iterator(); i.hasNext();) {
			try {
				((Runnable) i.next()).run();
			}
			catch (Throwable e) {
				System.out.println("exit() notification handler failed");
			}
		}
	}

	private LinkedList<Runnable> exitNotificationHandlers = new LinkedList<Runnable>();

	/** Nachos runtime statistics. */
	public Stats stats = null;

	/** Provides access to some private <tt>Machine</tt> methods. */
	public MachinePrivilege machine = null;

	/** Provides access to some private <tt>Interrupt</tt> methods. */
	public InterruptPrivilege interrupt = null;

	/** Provides access to some private <tt>Processor</tt> methods. */
	public ProcessorPrivilege processor = null;

	/** Provides access to some private <tt>TCB</tt> methods. */
	public TCBPrivilege tcb = null;

	/**
	 * An interface that provides access to some private <tt>Machine</tt>
	 * methods.
	 */
	public interface MachinePrivilege {
		/**
		 * Install a hardware console.
		 * 
		 * @param console the new hardware console.
		 */
		public void setConsole(SerialConsole console);
	}

	/**
	 * An interface that provides access to some private <tt>Interrupt</tt>
	 * methods.
	 */
	public interface InterruptPrivilege {
		/**
		 * Schedule an interrupt to occur at some time in the future.
		 * 
		 * @param when the number of ticks until the interrupt should occur.
		 * @param type a name for the type of interrupt being scheduled.
		 * @param handler the interrupt handler to call.
		 */
		public void schedule(long when, String type, Runnable handler);

		/**
		 * Advance the simulated time.
		 * 
		 * @param inKernelMode <tt>true</tt> if the current thread is running
		 * kernel code, <tt>false</tt> if the current thread is running MIPS
		 * user code.
		 */
		public void tick(boolean inKernelMode);
	}

	/**
	 * An interface that provides access to some private <tt>Processor</tt>
	 * methods.
	 */
	public interface ProcessorPrivilege {
		/**
		 * Flush the processor pipeline in preparation for switching to kernel
		 * mode.
		 */
		public void flushPipe();
	}

	/**
	 * An interface that provides access to some private <tt>TCB</tt> methods.
	 */
	public interface TCBPrivilege {
		/**
		 * Associate the current TCB with the specified <tt>KThread</tt>.
		 * <tt>AutoGrader.runningThread()</tt> <i>must</i> call this method
		 * before returning.
		 * 
		 * @param thread the current thread.
		 */
		public void associateThread(KThread thread);

		/**
		 * Authorize the TCB associated with the specified thread to be
		 * destroyed.
		 * 
		 * @param thread the thread whose TCB is about to be destroyed.
		 */
		public void authorizeDestroy(KThread thread);
	}
}
