// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.ag.*;

import java.io.File;

/**
 * The master class of the simulated machine. Processes command line arguments,
 * constructs all simulated hardware devices, and starts the grader.
 */
public final class Machine {
	/**
	 * Nachos main entry point.
	 * 
	 * @param args the command line arguments.
	 */
	public static void main(final String[] args) {
		System.out.print("nachos 5.0j initializing...");

		Lib.assertTrue(Machine.args == null);
		Machine.args = args;

		processArgs();

		Config.load(configFileName);

		// get the current directory (.)
		baseDirectory = new File(new File("").getAbsolutePath());
		// get the nachos directory (./nachos)
		nachosDirectory = new File(baseDirectory, "nachos");

		String testDirectoryName = Config.getString("FileSystem.testDirectory");

		// get the test directory
		if (testDirectoryName != null) {
			testDirectory = new File(testDirectoryName);
		}
		else {
			// use ../test
			testDirectory = new File(baseDirectory.getParentFile(), "test");
		}

		securityManager = new NachosSecurityManager(testDirectory);
		privilege = securityManager.getPrivilege();

		privilege.machine = new MachinePrivilege();

		TCB.givePrivilege(privilege);
		privilege.stats = stats;

		securityManager.enable();
		createDevices();
		checkUserClasses();

		autoGrader = (AutoGrader) Lib.constructObject(autoGraderClassName);

		new TCB().start(new Runnable() {
			public void run() {
				autoGrader.start(privilege);
			}
		});
	}

	/**
	 * Yield to non-Nachos threads. Use in non-preemptive JVM's to give
	 * non-Nachos threads a chance to run.
	 */
	public static void yield() {
		Thread.yield();
	}

	/**
	 * Terminate Nachos. Same as <tt>TCB.die()</tt>.
	 */
	public static void terminate() {
		TCB.die();
	}

	/**
	 * Terminate Nachos as the result of an unhandled exception or error.
	 * 
	 * @param e the exception or error.
	 */
	public static void terminate(Throwable e) {
		if (e instanceof ThreadDeath)
			throw (ThreadDeath) e;

		e.printStackTrace();
		terminate();
	}

	/**
	 * Print stats, and terminate Nachos.
	 */
	public static void halt() {
		System.out.print("Machine halting!\n\n");
		stats.print();
		terminate();
	}

	/**
	 * Return an array containing all command line arguments.
	 * 
	 * @return the command line arguments passed to Nachos.
	 */
	public static String[] getCommandLineArguments() {
		String[] result = new String[args.length];

		System.arraycopy(args, 0, result, 0, args.length);

		return result;
	}

	private static void processArgs() {
		for (int i = 0; i < args.length;) {
			String arg = args[i++];
			if (arg.length() > 0 && arg.charAt(0) == '-') {
				if (arg.equals("-d")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					Lib.enableDebugFlags(args[i++]);
				}
				else if (arg.equals("-h")) {
					System.out.print(help);
					System.exit(1);
				}
				else if (arg.equals("-m")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					try {
						numPhysPages = Integer.parseInt(args[i++]);
					}
					catch (NumberFormatException e) {
						Lib.assertNotReached("bad value for -m switch");
					}
				}
				else if (arg.equals("-s")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					try {
						randomSeed = Long.parseLong(args[i++]);
					}
					catch (NumberFormatException e) {
						Lib.assertNotReached("bad value for -s switch");
					}
				}
				else if (arg.equals("-x")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					shellProgramName = args[i++];
				}
				else if (arg.equals("-z")) {
					System.out.print(copyright);
					System.exit(1);
				}
				// these switches are reserved for the autograder
				else if (arg.equals("-[]")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					configFileName = args[i++];
				}
				else if (arg.equals("--")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					autoGraderClassName = args[i++];
				}
			}
		}

		Lib.seedRandom(randomSeed);
	}

	private static void createDevices() {
		interrupt = new Interrupt(privilege);
		timer = new Timer(privilege);

		if (Config.getBoolean("Machine.bank"))
			bank = new ElevatorBank(privilege);

		if (Config.getBoolean("Machine.processor")) {
			if (numPhysPages == -1)
				numPhysPages = Config.getInteger("Processor.numPhysPages");
			processor = new Processor(privilege, numPhysPages);
		}

		if (Config.getBoolean("Machine.console"))
			console = new StandardConsole(privilege);

		if (Config.getBoolean("Machine.stubFileSystem"))
			stubFileSystem = new StubFileSystem(privilege, testDirectory);

		if (Config.getBoolean("Machine.networkLink"))
			networkLink = new NetworkLink(privilege);
	}

	private static void checkUserClasses() {
		System.out.print(" user-check");

		Class aclsInt = (new int[0]).getClass();
		Class clsObject = Lib.loadClass("java.lang.Object");
		Class clsRunnable = Lib.loadClass("java.lang.Runnable");
		Class clsString = Lib.loadClass("java.lang.String");

		Class clsKernel = Lib.loadClass("nachos.machine.Kernel");
		Class clsFileSystem = Lib.loadClass("nachos.machine.FileSystem");
		Class clsRiderControls = Lib.loadClass("nachos.machine.RiderControls");
		Class clsElevatorControls = Lib
				.loadClass("nachos.machine.ElevatorControls");
		Class clsRiderInterface = Lib
				.loadClass("nachos.machine.RiderInterface");
		Class clsElevatorControllerInterface = Lib
				.loadClass("nachos.machine.ElevatorControllerInterface");

		Class clsAlarm = Lib.loadClass("nachos.threads.Alarm");
		Class clsThreadedKernel = Lib
				.loadClass("nachos.threads.ThreadedKernel");
		Class clsKThread = Lib.loadClass("nachos.threads.KThread");
		Class clsCommunicator = Lib.loadClass("nachos.threads.Communicator");
		Class clsSemaphore = Lib.loadClass("nachos.threads.Semaphore");
		Class clsLock = Lib.loadClass("nachos.threads.Lock");
		Class clsCondition = Lib.loadClass("nachos.threads.Condition");
		Class clsCondition2 = Lib.loadClass("nachos.threads.Condition2");
		Class clsRider = Lib.loadClass("nachos.threads.Rider");
		Class clsElevatorController = Lib
				.loadClass("nachos.threads.ElevatorController");

		Lib.checkDerivation(clsThreadedKernel, clsKernel);

		Lib.checkStaticField(clsThreadedKernel, "alarm", clsAlarm);
		Lib.checkStaticField(clsThreadedKernel, "fileSystem", clsFileSystem);

		Lib.checkMethod(clsAlarm, "waitUntil", new Class[] { long.class },
				void.class);

		Lib.checkConstructor(clsKThread, new Class[] {});
		Lib.checkConstructor(clsKThread, new Class[] { clsRunnable });

		Lib.checkStaticMethod(clsKThread, "currentThread", new Class[] {},
				clsKThread);
		Lib.checkStaticMethod(clsKThread, "finish", new Class[] {}, void.class);
		Lib.checkStaticMethod(clsKThread, "yield", new Class[] {}, void.class);
		Lib.checkStaticMethod(clsKThread, "sleep", new Class[] {}, void.class);

		Lib.checkMethod(clsKThread, "setTarget", new Class[] { clsRunnable },
				clsKThread);
		Lib.checkMethod(clsKThread, "setName", new Class[] { clsString },
				clsKThread);
		Lib.checkMethod(clsKThread, "getName", new Class[] {}, clsString);
		Lib.checkMethod(clsKThread, "fork", new Class[] {}, void.class);
		Lib.checkMethod(clsKThread, "ready", new Class[] {}, void.class);
		Lib.checkMethod(clsKThread, "join", new Class[] {}, void.class);

		Lib.checkField(clsKThread, "schedulingState", clsObject);

		Lib.checkConstructor(clsCommunicator, new Class[] {});
		Lib.checkMethod(clsCommunicator, "speak", new Class[] { int.class },
				void.class);
		Lib.checkMethod(clsCommunicator, "listen", new Class[] {}, int.class);

		Lib.checkConstructor(clsSemaphore, new Class[] { int.class });
		Lib.checkMethod(clsSemaphore, "P", new Class[] {}, void.class);
		Lib.checkMethod(clsSemaphore, "V", new Class[] {}, void.class);

		Lib.checkConstructor(clsLock, new Class[] {});
		Lib.checkMethod(clsLock, "acquire", new Class[] {}, void.class);
		Lib.checkMethod(clsLock, "release", new Class[] {}, void.class);
		Lib.checkMethod(clsLock, "isHeldByCurrentThread", new Class[] {},
				boolean.class);

		Lib.checkConstructor(clsCondition, new Class[] { clsLock });
		Lib.checkConstructor(clsCondition2, new Class[] { clsLock });

		Lib.checkMethod(clsCondition, "sleep", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition, "wake", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition, "wakeAll", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "sleep", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "wake", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "wakeAll", new Class[] {}, void.class);

		Lib.checkDerivation(clsRider, clsRiderInterface);

		Lib.checkConstructor(clsRider, new Class[] {});
		Lib.checkMethod(clsRider, "initialize", new Class[] { clsRiderControls,
				aclsInt }, void.class);

		Lib.checkDerivation(clsElevatorController,
				clsElevatorControllerInterface);

		Lib.checkConstructor(clsElevatorController, new Class[] {});
		Lib.checkMethod(clsElevatorController, "initialize",
				new Class[] { clsElevatorControls }, void.class);
	}

	/**
	 * Prevent instantiation.
	 */
	private Machine() {
	}

	/**
	 * Return the hardware interrupt manager.
	 * 
	 * @return the hardware interrupt manager.
	 */
	public static Interrupt interrupt() {
		return interrupt;
	}

	/**
	 * Return the hardware timer.
	 * 
	 * @return the hardware timer.
	 */
	public static Timer timer() {
		return timer;
	}

	/**
	 * Return the hardware elevator bank.
	 * 
	 * @return the hardware elevator bank, or <tt>null</tt> if it is not
	 * present.
	 */
	public static ElevatorBank bank() {
		return bank;
	}

	/**
	 * Return the MIPS processor.
	 * 
	 * @return the MIPS processor, or <tt>null</tt> if it is not present.
	 */
	public static Processor processor() {
		return processor;
	}

	/**
	 * Return the hardware console.
	 * 
	 * @return the hardware console, or <tt>null</tt> if it is not present.
	 */
	public static SerialConsole console() {
		return console;
	}

	/**
	 * Return the stub filesystem.
	 * 
	 * @return the stub file system, or <tt>null</tt> if it is not present.
	 */
	public static FileSystem stubFileSystem() {
		return stubFileSystem;
	}

	/**
	 * Return the network link.
	 * 
	 * @return the network link, or <tt>null</tt> if it is not present.
	 */
	public static NetworkLink networkLink() {
		return networkLink;
	}

	/**
	 * Return the autograder.
	 * 
	 * @return the autograder.
	 */
	public static AutoGrader autoGrader() {
		return autoGrader;
	}

	private static Interrupt interrupt = null;

	private static Timer timer = null;

	private static ElevatorBank bank = null;

	private static Processor processor = null;

	private static SerialConsole console = null;

	private static FileSystem stubFileSystem = null;

	private static NetworkLink networkLink = null;

	private static AutoGrader autoGrader = null;

	private static String autoGraderClassName = "nachos.ag.AutoGrader";

	/**
	 * Return the name of the shell program that a user-programming kernel must
	 * run. Make sure <tt>UserKernel.run()</tt> <i>always</i> uses this method
	 * to decide which program to run.
	 * 
	 * @return the name of the shell program to run.
	 */
	public static String getShellProgramName() {
		if (shellProgramName == null)
			shellProgramName = Config.getString("Kernel.shellProgram");

		Lib.assertTrue(shellProgramName != null);
		return shellProgramName;
	}

	private static String shellProgramName = null;

	/**
	 * Return the name of the process class that the kernel should use. In the
	 * multi-programming project, returns <tt>nachos.userprog.UserProcess</tt>.
	 * In the VM project, returns <tt>nachos.vm.VMProcess</tt>. In the
	 * networking project, returns <tt>nachos.network.NetProcess</tt>.
	 * 
	 * @return the name of the process class that the kernel should use.
	 * 
	 * @see nachos.userprog.UserKernel#run
	 * @see nachos.userprog.UserProcess
	 * @see nachos.vm.VMProcess
	 * @see nachos.network.NetProcess
	 */
	public static String getProcessClassName() {
		if (processClassName == null)
			processClassName = Config.getString("Kernel.processClassName");

		Lib.assertTrue(processClassName != null);
		return processClassName;
	}

	private static String processClassName = null;

	private static NachosSecurityManager securityManager;

	private static Privilege privilege;

	private static String[] args = null;

	private static Stats stats = new Stats();

	private static int numPhysPages = -1;

	private static long randomSeed = 0;

	private static File baseDirectory, nachosDirectory, testDirectory;

	private static String configFileName = "nachos.conf";

	private static final String help = "\n"
			+ "Options:\n"
			+ "\n"
			+ "\t-d <debug flags>\n"
			+ "\t\tEnable some debug flags, e.g. -d ti\n"
			+ "\n"
			+ "\t-h\n"
			+ "\t\tPrint this help message.\n"
			+ "\n"
			+ "\t-m <pages>\n"
			+ "\t\tSpecify how many physical pages of memory to simulate.\n"
			+ "\n"
			+ "\t-s <seed>\n"
			+ "\t\tSpecify the seed for the random number generator (seed is a\n"
			+ "\t\tlong).\n" + "\n" + "\t-x <program>\n"
			+ "\t\tSpecify a program that UserKernel.run() should execute,\n"
			+ "\t\tinstead of the value of the configuration variable\n"
			+ "\t\tKernel.shellProgram\n" + "\n" + "\t-z\n"
			+ "\t\tprint the copyright message\n" + "\n"
			+ "\t-- <grader class>\n"
			+ "\t\tSpecify an autograder class to use, instead of\n"
			+ "\t\tnachos.ag.AutoGrader\n" + "\n" + "\t-# <grader arguments>\n"
			+ "\t\tSpecify the argument string to pass to the autograder.\n"
			+ "\n" + "\t-[] <config file>\n"
			+ "\t\tSpecifiy a config file to use, instead of nachos.conf\n"
			+ "";

	private static final String copyright = "\n"
			+ "Copyright 1992-2001 The Regents of the University of California.\n"
			+ "All rights reserved.\n"
			+ "\n"
			+ "Permission to use, copy, modify, and distribute this software and\n"
			+ "its documentation for any purpose, without fee, and without\n"
			+ "written agreement is hereby granted, provided that the above\n"
			+ "copyright notice and the following two paragraphs appear in all\n"
			+ "copies of this software.\n"
			+ "\n"
			+ "IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY\n"
			+ "PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL\n"
			+ "DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS\n"
			+ "DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN\n"
			+ "ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n"
			+ "\n"
			+ "THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY\n"
			+ "WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"
			+ "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE\n"
			+ "SOFTWARE PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE\n"
			+ "UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE\n"
			+ "MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.\n";

	private static class MachinePrivilege implements Privilege.MachinePrivilege {
		public void setConsole(SerialConsole console) {
			Machine.console = console;
		}
	}

	// dummy variables to make javac smarter
	private static Coff dummy1 = null;
}
