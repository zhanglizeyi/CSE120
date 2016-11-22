// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.security;

import nachos.machine.*;

import java.io.File;
import java.security.Permission;
import java.io.FilePermission;
import java.util.PropertyPermission;
import java.net.NetPermission;
import java.awt.AWTPermission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * Protects the environment from malicious Nachos code.
 */
public class NachosSecurityManager extends SecurityManager {
	/**
	 * Allocate a new Nachos security manager.
	 * 
	 * @param testDirectory the directory usable by the stub file system.
	 */
	public NachosSecurityManager(File testDirectory) {
		this.testDirectory = testDirectory;

		fullySecure = Config.getBoolean("NachosSecurityManager.fullySecure");
	}

	/**
	 * Return a privilege object for this security manager. This security
	 * manager must not be the active security manager.
	 * 
	 * @return a privilege object for this security manager.
	 */
	public Privilege getPrivilege() {
		Lib.assertTrue(this != System.getSecurityManager());

		return new PrivilegeProvider();
	}

	/**
	 * Install this security manager.
	 */
	public void enable() {
		Lib.assertTrue(this != System.getSecurityManager());

		doPrivileged(new Runnable() {
			public void run() {
				System.setSecurityManager(NachosSecurityManager.this);
			}
		});
	}

	private class PrivilegeProvider extends Privilege {
		public void doPrivileged(Runnable action) {
			NachosSecurityManager.this.doPrivileged(action);
		}

		public Object doPrivileged(PrivilegedAction action) {
			return NachosSecurityManager.this.doPrivileged(action);
		}

		public Object doPrivileged(PrivilegedExceptionAction action)
				throws PrivilegedActionException {
			return NachosSecurityManager.this.doPrivileged(action);
		}

		public void exit(int exitStatus) {
			invokeExitNotificationHandlers();
			NachosSecurityManager.this.exit(exitStatus);
		}
	}

	private void enablePrivilege() {
		if (privilegeCount == 0) {
			Lib.assertTrue(privileged == null);
			privileged = Thread.currentThread();
			privilegeCount++;
		}
		else {
			Lib.assertTrue(privileged == Thread.currentThread());
			privilegeCount++;
		}
	}

	private void rethrow(Throwable e) {
		disablePrivilege();

		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		else if (e instanceof Error)
			throw (Error) e;
		else
			Lib.assertNotReached();
	}

	private void disablePrivilege() {
		Lib.assertTrue(privileged != null && privilegeCount > 0);
		privilegeCount--;
		if (privilegeCount == 0)
			privileged = null;
	}

	private void forcePrivilege() {
		privileged = Thread.currentThread();
		privilegeCount = 1;
	}

	private void exit(int exitStatus) {
		forcePrivilege();
		System.exit(exitStatus);
	}

	private boolean isPrivileged() {
		// the autograder does not allow non-Nachos threads to be created, so..
		if (!TCB.isNachosThread())
			return true;

		return (privileged == Thread.currentThread());
	}

	private void doPrivileged(final Runnable action) {
		doPrivileged(new PrivilegedAction() {
			public Object run() {
				action.run();
				return null;
			}
		});
	}

	private Object doPrivileged(PrivilegedAction action) {
		Object result = null;
		enablePrivilege();
		try {
			result = action.run();
		}
		catch (Throwable e) {
			rethrow(e);
		}
		disablePrivilege();
		return result;
	}

	private Object doPrivileged(PrivilegedExceptionAction action)
			throws PrivilegedActionException {
		Object result = null;
		enablePrivilege();
		try {
			result = action.run();
		}
		catch (Exception e) {
			throw new PrivilegedActionException(e);
		}
		catch (Throwable e) {
			rethrow(e);
		}
		disablePrivilege();
		return result;
	}

	private void no() {
		throw new SecurityException();
	}

	private void no(Permission perm) {
		System.err.println("\n\nLacked permission: " + perm);
		throw new SecurityException();
	}

	/**
	 * Check the specified permission. Some operations are permissible while not
	 * grading. These operations are regulated here.
	 * 
	 * @param perm the permission to check.
	 */
	public void checkPermission(Permission perm) {
		String name = perm.getName();

		// some permissions are strictly forbidden
		if (perm instanceof RuntimePermission) {
			// no creating class loaders
			if (name.equals("createClassLoader"))
				no(perm);
		}

		// allow the AWT mess when not grading
		if (!fullySecure) {
			if (perm instanceof NetPermission) {
				// might be needed to load awt stuff
				if (name.equals("specifyStreamHandler"))
					return;
			}

			if (perm instanceof RuntimePermission) {
				// might need to load libawt
				if (name.startsWith("loadLibrary.")) {
					String lib = name.substring("loadLibrary.".length());
					if (lib.equals("awt")) {
						Lib.debug(dbgSecurity, "\tdynamically linking " + lib);
						return;
					}
				}
			}

			if (perm instanceof AWTPermission) {
				// permit AWT stuff
				if (name.equals("accessEventQueue"))
					return;
			}
		}

		// some are always allowed
		if (perm instanceof PropertyPermission) {
			// allowed to read properties
			if (perm.getActions().equals("read"))
				return;
		}

		// some require some more checking
		if (perm instanceof FilePermission) {
			if (perm.getActions().equals("read")) {
				// the test directory can only be read with privilege
				if (isPrivileged())
					return;

				enablePrivilege();

				// not allowed to read test directory directly w/out privilege
				try {
					File f = new File(name);
					if (f.isFile()) {
						File p = f.getParentFile();
						if (p != null) {
							if (p.equals(testDirectory))
								no(perm);
						}
					}
				}
				catch (Throwable e) {
					rethrow(e);
				}

				disablePrivilege();
				return;
			}
			else if (perm.getActions().equals("write")
					|| perm.getActions().equals("delete")) {
				// only allowed to write test diretory, and only with privilege
				verifyPrivilege();

				try {
					File f = new File(name);
					if (f.isFile()) {
						File p = f.getParentFile();
						if (p != null && p.equals(testDirectory))
							return;
					}
				}
				catch (Throwable e) {
					no(perm);
				}
			}
			else if (perm.getActions().equals("execute")) {
				// only allowed to execute with privilege, and if there's a net
				verifyPrivilege();

				if (Machine.networkLink() == null)
					no(perm);
			}
			else {
				no(perm);
			}
		}

		// default to requiring privilege
		verifyPrivilege(perm);
	}

	/**
	 * Called by the <tt>java.lang.Thread</tt> constructor to determine a thread
	 * group for a child thread of the current thread. The caller must be
	 * privileged in order to successfully create the thread.
	 * 
	 * @return a thread group for the new thread, or <tt>null</tt> to use the
	 * current thread's thread group.
	 */
	public ThreadGroup getThreadGroup() {
		verifyPrivilege();
		return null;
	}

	/**
	 * Verify that the caller is privileged.
	 */
	public void verifyPrivilege() {
		if (!isPrivileged())
			no();
	}

	/**
	 * Verify that the caller is privileged, so as to check the specified
	 * permission.
	 * 
	 * @param perm the permission being checked.
	 */
	public void verifyPrivilege(Permission perm) {
		if (!isPrivileged())
			no(perm);
	}

	private File testDirectory;

	private boolean fullySecure;

	private Thread privileged = null;

	private int privilegeCount = 0;

	private static final char dbgSecurity = 'S';
}
