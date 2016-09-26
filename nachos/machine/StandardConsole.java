// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.io.IOException;

/**
 * A text-based console that uses System.in and System.out.
 */
public class StandardConsole implements SerialConsole {
	/**
	 * Allocate a new standard console.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 */
	public StandardConsole(Privilege privilege) {
		System.out.print(" console");

		this.privilege = privilege;

		receiveInterrupt = new Runnable() {
			public void run() {
				receiveInterrupt();
			}
		};

		sendInterrupt = new Runnable() {
			public void run() {
				sendInterrupt();
			}
		};

		scheduleReceiveInterrupt();
	}

	public final void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler) {
		this.receiveInterruptHandler = receiveInterruptHandler;
		this.sendInterruptHandler = sendInterruptHandler;
	}

	private void scheduleReceiveInterrupt() {
		privilege.interrupt.schedule(Stats.ConsoleTime, "console read",
				receiveInterrupt);
	}

	/**
	 * Attempt to read a byte from the object backing this console.
	 * 
	 * @return the byte read, or -1 of no data is available.
	 */
	protected int in() {
		try {
			if (System.in.available() <= 0)
				return -1;

			return System.in.read();
		}
		catch (IOException e) {
			return -1;
		}
	}

	private int translateCharacter(int c) {
		// translate win32 0x0D 0x0A sequence to single newline
		if (c == 0x0A && prevCarriageReturn) {
			prevCarriageReturn = false;
			return -1;
		}
		prevCarriageReturn = (c == 0x0D);

		// invalid if non-ASCII
		if (c >= 0x80)
			return -1;
		// backspace characters
		else if (c == 0x04 || c == 0x08 || c == 0x19 || c == 0x1B || c == 0x7F)
			return '\b';
		// if normal ASCII range, nothing to do
		else if (c >= 0x20)
			return c;
		// newline characters
		else if (c == 0x0A || c == 0x0D)
			return '\n';
		// everything else is invalid
		else
			return -1;
	}

	private void receiveInterrupt() {
		Lib.assertTrue(incomingKey == -1);

		incomingKey = translateCharacter(in());
		if (incomingKey == -1) {
			scheduleReceiveInterrupt();
		}
		else {
			privilege.stats.numConsoleReads++;

			if (receiveInterruptHandler != null)
				receiveInterruptHandler.run();
		}
	}

	public final int readByte() {
		int key = incomingKey;

		if (incomingKey != -1) {
			incomingKey = -1;
			scheduleReceiveInterrupt();
		}

		return key;
	}

	private void scheduleSendInterrupt() {
		privilege.interrupt.schedule(Stats.ConsoleTime, "console write",
				sendInterrupt);
	}

	/**
	 * Write a byte to the object backing this console.
	 * 
	 * @param value the byte to write.
	 */
	protected void out(int value) {
		System.out.write(value);
		System.out.flush();
	}

	private void sendInterrupt() {
		Lib.assertTrue(outgoingKey != -1);

		out(outgoingKey);
		outgoingKey = -1;

		privilege.stats.numConsoleWrites++;

		if (sendInterruptHandler != null)
			sendInterruptHandler.run();
	}

	public final void writeByte(int value) {
		if (outgoingKey == -1)
			scheduleSendInterrupt();

		outgoingKey = value & 0xFF;
	}

	private Privilege privilege = null;

	private Runnable receiveInterrupt;

	private Runnable sendInterrupt;

	private Runnable receiveInterruptHandler = null;

	private Runnable sendInterruptHandler = null;

	private int incomingKey = -1;

	private int outgoingKey = -1;

	private boolean prevCarriageReturn = false;
}
