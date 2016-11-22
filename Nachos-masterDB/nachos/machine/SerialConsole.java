// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * A serial console can be used to send and receive characters. Only one
 * character may be sent at a time, and only one character may be received at a
 * time.
 */

public interface SerialConsole {
	/**
	 * Set this console's receive and send interrupt handlers.
	 * 
	 * <p>
	 * The receive interrupt handler is called every time another byte arrives
	 * and can be read using <tt>readByte()</tt>.
	 * 
	 * <p>
	 * The send interrupt handler is called every time a byte sent with
	 * <tt>writeByte()</tt> is finished being sent. This means that another byte
	 * can be sent.
	 * 
	 * @param receiveInterruptHandler the callback to call when a byte arrives.
	 * @param sendInterruptHandler the callback to call when another byte can be
	 * sent.
	 */
	public void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler);

	/**
	 * Return the next unsigned byte received (in the range <tt>0</tt> through
	 * <tt>255</tt>).
	 * 
	 * @return the next byte read, or -1 if no byte is available.
	 */
	public int readByte();

	/**
	 * Send another byte. If a byte is already being sent, the result is not
	 * defined.
	 * 
	 * @param value the byte to be sent (the upper 24 bits are ignored).
	 */
	public void writeByte(int value);
}
