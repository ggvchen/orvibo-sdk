package com.github.tavalin.s20.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tavalin.s20.S20Client;
import com.github.tavalin.s20.entities.internal.Message;
import com.github.tavalin.s20.entities.internal.MessageType;
import com.github.tavalin.s20.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class SocketReader.
 */
public class SocketReader implements Runnable {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SocketReader.class);
	
	/** The socket. */
	private DatagramSocket socket;
	
	/** The network context. */
	private S20Client networkContext;
	
	/** The running. */
	private boolean running = false;

	/**
	 * Instantiates a new socket reader.
	 *
	 * @param udpSocket the udp socket
	 * @param s20Client the s20 client
	 */
	public SocketReader(DatagramSocket udpSocket, S20Client s20Client) {
		if (udpSocket == null) {
			throw new IllegalArgumentException("Socket cannot be null");
		}
		if (s20Client == null) {
			throw new IllegalArgumentException("Client cannot be null");
		}

		this.socket = udpSocket;
		this.networkContext = s20Client;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		while (running && !Thread.currentThread().isInterrupted()) {
			byte[] buffer = new byte[256];
			DatagramPacket p = new DatagramPacket(buffer, buffer.length);
			try {
				socket.receive(p);
				if (!TransportManager.isLocalAddress(p.getAddress())) {
					byte[] data = Arrays.copyOf(p.getData(), p.getLength());
					String message = Utils.toHexString(data);

					// order of message config is important
					Message m = new Message(message);
					MessageType type = Message.getMessageTypeFromMessage(m);
					m.setMessageType(type);
					String deviceId = Message.getDeviceIdFromMessage(m);
					m.setDeviceId(deviceId);
					m.setAddress(new InetSocketAddress(p.getAddress(), TransportManager.REMOTE_PORT));
					logger.debug(String.format("%s %s %s %s",p.getSocketAddress(), type, deviceId, message));
					networkContext.handleMessage(m);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}
		}
	}
	



}
