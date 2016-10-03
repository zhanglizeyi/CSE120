package nachos.network;

import nachos.machine.*;

/**
 * A mail message. Includes a packet header, a mail header, and the actual
 * payload.
 * 
 * @see nachos.machine.Packet
 */
public class MailMessage {
	/**
	 * Allocate a new mail message to be sent, using the specified parameters.
	 * 
	 * @param dstLink the destination link address.
	 * @param dstPort the destination port.
	 * @param srcLink the source link address.
	 * @param srcPort the source port.
	 * @param contents the contents of the packet.
	 */
	public MailMessage(int dstLink, int dstPort, int srcLink, int srcPort,
			byte[] contents) throws MalformedPacketException {
		// make sure the paramters are valid
		if (dstPort < 0 || dstPort >= portLimit || srcPort < 0
				|| srcPort >= portLimit || contents.length > maxContentsLength)
			throw new MalformedPacketException();

		this.dstPort = (byte) dstPort;
		this.srcPort = (byte) srcPort;
		this.contents = contents;

		byte[] packetContents = new byte[headerLength + contents.length];

		packetContents[0] = (byte) dstPort;
		packetContents[1] = (byte) srcPort;

		System.arraycopy(contents, 0, packetContents, headerLength,
				contents.length);

		packet = new Packet(dstLink, srcLink, packetContents);
	}

	/**
	 * Allocate a new mail message using the specified packet from the network.
	 * 
	 * @param packet the packet containg the mail message.
	 */
	public MailMessage(Packet packet) throws MalformedPacketException {
		this.packet = packet;

		// make sure we have a valid header
		if (packet.contents.length < headerLength || packet.contents[0] < 0
				|| packet.contents[0] >= portLimit || packet.contents[1] < 0
				|| packet.contents[1] >= portLimit)
			throw new MalformedPacketException();

		dstPort = packet.contents[0];
		srcPort = packet.contents[1];

		contents = new byte[packet.contents.length - headerLength];
		System.arraycopy(packet.contents, headerLength, contents, 0,
				contents.length);
	}

	/**
	 * Return a string representation of the message headers.
	 */
	public String toString() {
		return "from (" + packet.srcLink + ":" + srcPort + ") to ("
				+ packet.dstLink + ":" + dstPort + "), " + contents.length
				+ " bytes";
	}

	/** This message, as a packet that can be sent through a network link. */
	public Packet packet;

	/** The port used by this message on the destination machine. */
	public int dstPort;

	/** The port used by this message on the source machine. */
	public int srcPort;

	/** The contents of this message, excluding the mail message header. */
	public byte[] contents;

	/**
	 * The number of bytes in a mail header. The header is formatted as follows:
	 * 
	 * <table>
	 * <tr>
	 * <td>offset</td>
	 * <td>size</td>
	 * <td>value</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>destination port</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>source port</td>
	 * </tr>
	 * </table>
	 */
	public static final int headerLength = 2;

	/** Maximum payload (real data) that can be included in a single mesage. */
	public static final int maxContentsLength = Packet.maxContentsLength
			- headerLength;

	/**
	 * The upper limit on mail ports. All ports fall between <tt>0</tt> and
	 * <tt>portLimit - 1</tt>.
	 */
	public static final int portLimit = 128;
}
