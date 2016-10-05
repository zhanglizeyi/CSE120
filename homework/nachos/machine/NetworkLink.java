// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

/**
 * A full-duplex network link. Provides ordered, unreliable delivery of
 * limited-size packets to other machines on the network. Packets are guaranteed
 * to be uncorrupted as well.
 * 
 * <p>
 * Recall the general layering of network protocols:
 * <ul>
 * <li>Session/Transport
 * <li>Network
 * <li>Link
 * <li>Physical
 * </ul>
 * 
 * <p>
 * The physical layer provides a bit stream interface to the link layer. This
 * layer is very hardware-dependent.
 * 
 * <p>
 * The link layer uses the physical layer to provide a packet interface to the
 * network layer. The link layer generally provides unreliable delivery of
 * limited-size packets, but guarantees that packets will not arrive out of
 * order. Some links protect against packet corruption as well. The ethernet
 * protocol is an example of a link layer.
 * 
 * <p>
 * The network layer exists to connect multiple networks together into an
 * internet. The network layer provides globally unique addresses. Routers
 * (a.k.a. gateways) move packets across networks at this layer. The network
 * layer provides unordered, unreliable delivery of limited-size uncorrupted
 * packets to any machine on the same internet. The most commonly used network
 * layer protocol is IP (Internet Protocol), which is used to connect the
 * Internet.
 * 
 * <p>
 * The session/transport layer provides a byte-stream interface to the
 * application. This means that the transport layer must deliver uncorrupted
 * bytes to the application, in the same order they were sent. Byte-streams must
 * be connected and disconnected, and exist between ports, not machines.
 * 
 * <p>
 * This class provides a link layer abstraction. Since we do not allow different
 * Nachos networks to communicate with one another, there is no need for a
 * network layer in Nachos. This should simplify your design for the
 * session/transport layer, since you can assume packets never arrive out of
 * order.
 */
public class NetworkLink {
	/**
	 * Allocate a new network link.
	 * 
	 * <p>
	 * <tt>nachos.conf</tt> specifies the reliability of the network. The
	 * reliability, between 0 and 1, is the probability that any particular
	 * packet will not get dropped by the network.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 */
	public NetworkLink(Privilege privilege) {
		System.out.print(" network");

		this.privilege = privilege;

		try {
			localHost = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			localHost = null;
		}

		Lib.assertTrue(localHost != null);

		reliability = Config.getDouble("NetworkLink.reliability");
		Lib.assertTrue(reliability > 0 && reliability <= 1.0);

		socket = null;

		for (linkAddress = 0; linkAddress < Packet.linkAddressLimit; linkAddress++) {
			try {
				socket = new DatagramSocket(portBase + linkAddress, localHost);
				break;
			}
			catch (SocketException e) {
			}
		}

		if (socket == null) {
			System.out.println("");
			System.out.println("Unable to acquire a link address!");
			Lib.assertNotReached();
		}

		System.out.print("(" + linkAddress + ")");

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

		Thread receiveThread = new Thread(new Runnable() {
			public void run() {
				receiveLoop();
			}
		});

		receiveThread.start();
	}

	/**
	 * Returns the address of this network link.
	 * 
	 * @return the address of this network link.
	 */
	public int getLinkAddress() {
		return linkAddress;
	}

	/**
	 * Set this link's receive and send interrupt handlers.
	 * 
	 * <p>
	 * The receive interrupt handler is called every time a packet arrives and
	 * can be read using <tt>receive()</tt>.
	 * 
	 * <p>
	 * The send interrupt handler is called every time a packet sent with
	 * <tt>send()</tt> is finished being sent. This means that another packet
	 * can be sent.
	 * 
	 * @param receiveInterruptHandler the callback to call when a packet
	 * arrives.
	 * @param sendInterruptHandler the callback to call when another packet can
	 * be sent.
	 */
	public void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler) {
		this.receiveInterruptHandler = receiveInterruptHandler;
		this.sendInterruptHandler = sendInterruptHandler;
	}

	private void scheduleReceiveInterrupt() {
		privilege.interrupt.schedule(Stats.NetworkTime, "network recv",
				receiveInterrupt);
	}

	private synchronized void receiveInterrupt() {
		Lib.assertTrue(incomingPacket == null);

		if (incomingBytes != null) {
			if (Machine.autoGrader().canReceivePacket(privilege)) {
				try {
					incomingPacket = new Packet(incomingBytes);

					privilege.stats.numPacketsReceived++;
				}
				catch (MalformedPacketException e) {
				}
			}

			incomingBytes = null;
			notify();

			if (incomingPacket == null)
				scheduleReceiveInterrupt();
			else if (receiveInterruptHandler != null)
				receiveInterruptHandler.run();
		}
		else {
			scheduleReceiveInterrupt();
		}
	}

	/**
	 * Return the next packet received.
	 * 
	 * @return the next packet received, or <tt>null</tt> if no packet is
	 * available.
	 */
	public Packet receive() {
		Packet p = incomingPacket;

		if (incomingPacket != null) {
			incomingPacket = null;
			scheduleReceiveInterrupt();
		}

		return p;
	}

	private void receiveLoop() {
		while (true) {
			synchronized (this) {
				while (incomingBytes != null) {
					try {
						wait();
					}
					catch (InterruptedException e) {
					}
				}
			}

			byte[] packetBytes;

			try {
				byte[] buffer = new byte[Packet.maxPacketLength];

				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

				socket.receive(dp);

				packetBytes = new byte[dp.getLength()];

				System.arraycopy(buffer, 0, packetBytes, 0, packetBytes.length);
			}
			catch (IOException e) {
				return;
			}

			synchronized (this) {
				incomingBytes = packetBytes;
			}
		}
	}

	private void scheduleSendInterrupt() {
		privilege.interrupt.schedule(Stats.NetworkTime, "network send",
				sendInterrupt);
	}

	private void sendInterrupt() {
		Lib.assertTrue(outgoingPacket != null);

		// randomly drop packets, according to its reliability
		if (Machine.autoGrader().canSendPacket(privilege)
				&& Lib.random() <= reliability) {
			// ok, no drop
			privilege.doPrivileged(new Runnable() {
				public void run() {
					sendPacket();
				}
			});
		}
		else {
			outgoingPacket = null;
		}

		if (sendInterruptHandler != null)
			sendInterruptHandler.run();
	}

	private void sendPacket() {
		Packet p = outgoingPacket;
		outgoingPacket = null;

		try {
			socket.send(new DatagramPacket(p.packetBytes, p.packetBytes.length,
					localHost, portBase + p.dstLink));

			privilege.stats.numPacketsSent++;
		}
		catch (IOException e) {
		}
	}

	/**
	 * Send another packet. If a packet is already being sent, the result is not
	 * defined.
	 * 
	 * @param pkt the packet to send.
	 */
	public void send(Packet pkt) {
		if (outgoingPacket == null)
			scheduleSendInterrupt();

		outgoingPacket = pkt;
	}

	private static final int hash;

	private static final int portBase;

	/**
	 * The address of the network to which are attached all network links in
	 * this JVM. This is a hash on the account name of the JVM running this
	 * Nachos instance. It is used to help prevent packets from other users from
	 * accidentally interfering with this network.
	 */
	public static final byte networkID;

	static {
		hash = System.getProperty("user.name").hashCode();
		portBase = 0x4E41 + Math.abs(hash % 0x4E41);
		networkID = (byte) (hash / 0x4E41);
	}

	private Privilege privilege;

	private Runnable receiveInterrupt;

	private Runnable sendInterrupt;

	private Runnable receiveInterruptHandler = null;

	private Runnable sendInterruptHandler = null;

	private InetAddress localHost;

	private DatagramSocket socket;

	private byte linkAddress;

	private double reliability;

	private byte[] incomingBytes = null;

	private Packet incomingPacket = null;

	private Packet outgoingPacket = null;

	private boolean sendBusy = false;
}
