package org.openhab.binding.mox.handler;

import com.google.common.util.concurrent.RateLimiter;
import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.mox.config.MoxGatewayConfig;
import org.openhab.binding.mox.protocol.MoxMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Singleton of a MoxGatewaySendHandler. This is used to limit the send rate as packets per second to not
 * kill the gateway. This is useful on startup, because all device states are fetched then, which requires
 * from 1 to 4 packages per device. The gateway vendor suggests a max send rate of 2 per second.
 */
public class MoxGatewaySendHandler {
	private static final long SEND_FAIL_TIMEOUT = 1000 * 60 * 3; // 3 Minutes
	private Logger logger = LoggerFactory
			.getLogger(MoxGatewaySendHandler.class);

	private MoxGatewayConfig config;
	private Bridge bridge;
	private final RateLimiter rateLimiter = RateLimiter.create(2.0); // 2 permits per second

	private static MoxGatewaySendHandler instance = null;

	private MoxGatewaySendHandler(Bridge bridge) {
		this.bridge = bridge;
		this.config = bridge.getConfiguration().as(MoxGatewayConfig.class);
		logger.debug("Created new instance of MoxGatewaySendHandler which sends to {}:{}", config.udpHost,
					 config.targetUdpPort);
	}

	/**
	 * Returns or created an instance of the {MoxGatewaySendHandler}. It will create a new
	 * instance if the given bridge does not equal that one before.
	 * @param bridge
	 * @return
	 */
	public static MoxGatewaySendHandler getInstance(Bridge bridge) {
		assert (bridge != null);
		if (instance == null || !instance.bridge.equals(bridge)) {
			instance = new MoxGatewaySendHandler(bridge);
		}
		return instance;
	}

	/**
	 * Send a MoxMessage as byte stream to the configured target of the bridge.
	 * The send rate (messages per second) is capped with a 3 minutes timeout.
	 * If it's not possible to send the message after that amount of time, false is returned.
	 * @see {SEND_FAIL_TIMEOUT}
	 * @param messageBytes MoxMessage
	 * @return
	 */
	public boolean sendMoxMessage(byte[] messageBytes) {
		if (messageBytes == null)
			throw new IllegalArgumentException("Bytes to send were null.");
		return sendMoxMessage(messageBytes, SEND_FAIL_TIMEOUT);
	}

	/**
	 * Like {sendMoxMessage(byte[])} this method send the bytes of a MoxMessage,
	 * but with an own timout value. You can set {timeoutMillis} to 0 to get immediately
	 * false, if instant sending is not possible.
	 * @param messageBytes MoxMessage
	 * @param timeoutMillis Timeout for send
	 * @return True on successful send, false on timeout or sending problems.
	 */
	public boolean sendMoxMessage(byte[] messageBytes, long timeoutMillis) {
		if (messageBytes == null)
			throw new IllegalArgumentException("Bytes to send were null.");
		if (timeoutMillis < 0) {
			throw new IllegalArgumentException("Timeout can not be < 0, was " + timeoutMillis);
		}

		if (!rateLimiter.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
			logger.error(
					"Could not send message due to rate limitation. Current send rate: {} per second",
					rateLimiter.getRate());
			logger.debug("Message that could not be sent: {}", messageBytes);
			return false;
		}
		return sendMoxMessageInternal(messageBytes);
	}

	private boolean sendMoxMessageInternal(byte[] messageBytes) {
		DatagramSocket datagramSocket = null;
		try {
			InetAddress address = InetAddress.getByName(config.udpHost);

			if (logger.isTraceEnabled()) {
				logger.trace("Sending bytes to host {} : {}", address,
						MoxMessageBuilder.getUnsignedIntArray(messageBytes));
			}

			DatagramPacket packet = new DatagramPacket(messageBytes,
					messageBytes.length, address, config.targetUdpPort);
			datagramSocket = new DatagramSocket();
			datagramSocket.send(packet);
			return true;
		} catch (Exception e) {
			logger.error("Error sending UDP datagram: {}",
					e.getLocalizedMessage(), e);
		} finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}
		}
		return false;
	}

}