// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * An OS kernel.
 */
public abstract class Kernel {
	/** Globally accessible reference to the kernel. */
	public static Kernel kernel = null;

	/**
	 * Allocate a new kernel.
	 */
	public Kernel() {
		// make sure only one kernel is created
		Lib.assertTrue(kernel == null);
		kernel = this;
	}

	/**
	 * Initialize this kernel.
	 */
	public abstract void initialize(String[] args);

	/**
	 * Test that this module works.
	 * 
	 * <b>Warning:</b> this method will not be invoked by the autograder when we
	 * grade your projects. You should perform all initialization in
	 * <tt>initialize()</tt>.
	 */
	public abstract void selfTest();

	/**
	 * Begin executing user programs, if applicable.
	 */
	public abstract void run();

	/**
	 * Terminate this kernel. Never returns.
	 */
	public abstract void terminate();
}
