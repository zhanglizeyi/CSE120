// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * A link-layer packet.
 * 
 * @see nachos.machine.NetworkLink
 */
public class Packet {
	/**
	 * Allocate a new packet to be sent, using the specified parameters.
	 * 
	 * @param dstLink the destination link address.
	 * @param srcLink the source link address.
	 * @param contents the contents of the packet.
	 */
	public Packet(int dstLink, int srcLink, byte[] contents)
			throws MalformedPacketException {
		// make sure the paramters are valid
		if (dstLink < 0 || dstLink >= linkAddressLimit || srcLink < 0
				|| srcLink >= linkAddressLimit
				|| contents.length > maxContentsLength)
			throw new MalformedPacketException();

		this.dstLink = dstLink;
		this.srcLink = srcLink;
		this.contents = contents;

		packetBytes = new byte[headerLength + contents.length];

		packetBytes[0] = NetworkLink.networkID;
		packetBytes[1] = (byte) dstLink;
		packetBytes[2] = (byte) srcLink;
		packetBytes[3] = (byte) contents.length;

		// if java had subarrays, i'd use them. but System.arraycopy is ok...
		System.arraycopy(contents, 0, packetBytes, headerLength,
				contents.length);
	}

	/**
	 * Allocate a new packet using the specified array of bytes received from
	 * the network.
	 * 
	 * @param packetBytes the bytes making up this packet.
	 */
	public Packet(byte[] packetBytes) throws MalformedPacketException {
		this.packetBytes = packetBytes;

		// make sure we have a valid header
		if (packetBytes.length < headerLength
				|| packetBytes[0] != NetworkLink.networkID
				|| packetBytes[1] < 0 || packetBytes[1] >= linkAddressLimit
				|| packetBytes[2] < 0 || packetBytes[2] >= linkAddressLimit
				|| packetBytes[3] < 0
				|| packetBytes[3] > packetBytes.length - 4)
			throw new MalformedPacketException();

		dstLink = packetBytes[1];
		srcLink = packetBytes[2];

		contents = new byte[packetBytes[3]];
		System.arraycopy(packetBytes, headerLength, contents, 0,
				contents.length);
	}

	/** This packet, as an array of bytes that can be sent on a network. */
	public byte[] packetBytes;

	/** The address of the destination link of this packet. */
	public int dstLink;

	/** The address of the source link of this packet. */
	public int srcLink;

	/** The contents of this packet, excluding the link-layer header. */
	public byte[] contents;

	/**
	 * The number of bytes in a link-layer packet header. The header is
	 * formatted as follows:
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
	 * <td>network ID (collision detecting)</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>destination link address</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>1</td>
	 * <td>source link address</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>1</td>
	 * <td>length of contents</td>
	 * </tr>
	 * </table>
	 */
	public static final int headerLength = 4;

	/**
	 * The maximum length, in bytes, of a packet that can be sent or received on
	 * the network.
	 */
	public static final int maxPacketLength = 32;

	/**
	 * The maximum number of content bytes (not including the header). Note that
	 * this is just <tt>maxPacketLength - headerLength</tt>.
	 */
	public static final int maxContentsLength = maxPacketLength - headerLength;

	/**
	 * The upper limit on Nachos link addresses. All link addresses fall between
	 * <tt>0</tt> and <tt>linkAddressLimit - 1</tt>.
	 */
	public static final int linkAddressLimit = 128;
}
