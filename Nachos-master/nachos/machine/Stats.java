// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.machine.*;

/**
 * An object that maintains Nachos runtime statistics.
 */
public final class Stats {
	/**
	 * Allocate a new statistics object.
	 */
	public Stats() {
	}

	/**
	 * Print out the statistics in this object.
	 */
	public void print() {
		System.out.println("Ticks: total " + totalTicks + ", kernel "
				+ kernelTicks + ", user " + userTicks);
		System.out.println("Disk I/O: reads " + numDiskReads + ", writes "
				+ numDiskWrites);
		System.out.println("Console I/O: reads " + numConsoleReads
				+ ", writes " + numConsoleWrites);
		System.out.println("Paging: page faults " + numPageFaults
				+ ", TLB misses " + numTLBMisses);
		System.out.println("Network I/O: received " + numPacketsReceived
				+ ", sent " + numPacketsSent);
	}

	/**
	 * The total amount of simulated time that has passed since Nachos started.
	 */
	public long totalTicks = 0;

	/**
	 * The total amount of simulated time that Nachos has spent in kernel mode.
	 */
	public long kernelTicks = 0;

	/**
	 * The total amount of simulated time that Nachos has spent in user mode.
	 */
	public long userTicks = 0;

	/** The total number of sectors Nachos has read from the simulated disk. */
	public int numDiskReads = 0;

	/** The total number of sectors Nachos has written to the simulated disk. */
	public int numDiskWrites = 0;

	/** The total number of characters Nachos has read from the console. */
	public int numConsoleReads = 0;

	/** The total number of characters Nachos has written to the console. */
	public int numConsoleWrites = 0;

	/** The total number of page faults that have occurred. */
	public int numPageFaults = 0;

	/** The total number of TLB misses that have occurred. */
	public int numTLBMisses = 0;

	/** The total number of packets Nachos has sent to the network. */
	public int numPacketsSent = 0;

	/** The total number of packets Nachos has received from the network. */
	public int numPacketsReceived = 0;

	/**
	 * The amount to advance simulated time after each user instructions is
	 * executed.
	 */
	public static final int UserTick = 1;

	/**
	 * The amount to advance simulated time after each interrupt enable.
	 */
	public static final int KernelTick = 10;

	/**
	 * The amount of simulated time required to rotate the disk 360 degrees.
	 */
	public static final int RotationTime = 500;

	/**
	 * The amount of simulated time required for the disk to seek.
	 */
	public static final int SeekTime = 500;

	/**
	 * The amount of simulated time required for the console to handle a
	 * character.
	 */
	public static final int ConsoleTime = 100;

	/**
	 * The amount of simulated time required for the network to handle a packet.
	 */
	public static final int NetworkTime = 100;

	/**
	 * The mean amount of simulated time between timer interrupts.
	 */
	public static final int TimerTicks = 500;

	/**
	 * The amount of simulated time required for an elevator to move a floor.
	 */
	public static final int ElevatorTicks = 2000;
}
