package org.openhab.binding.mox.handler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.mox.config.MoxGatewayConfig;
import org.openhab.binding.mox.protocol.MoxMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

public class MoxGatewaySendHandler {
	private Logger logger = LoggerFactory
			.getLogger(MoxGatewaySendHandler.class);

	public MoxGatewayConfig config;
	public Bridge bridge;
	final RateLimiter rateLimiter = RateLimiter.create(2.0); // "2 permits per second"

	private static MoxGatewaySendHandler instance = null;

	private MoxGatewaySendHandler(Bridge bridge) {
		this.bridge = bridge;
		this.config = bridge.getConfiguration().as(MoxGatewayConfig.class);
		logger.trace(
				"Created new instance of MoxGatewaySendHandler which sends to {}:{}",
				config.udpHost, config.udpPort);
	}

	public static MoxGatewaySendHandler getInstance(Bridge bridge) {
		assert (bridge != null);
		if (instance == null || !instance.bridge.equals(bridge)) {
			instance = new MoxGatewaySendHandler(bridge);
		}
		return instance;
	}

	public boolean sendMoxMessage(byte[] messageBytes) {
		if (messageBytes == null)
			throw new IllegalArgumentException("Bytes to send were null.");
		rateLimiter.acquire();
		return sendMoxMessageInternal(messageBytes);
	}

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
			logger.trace("Message that could not be sent: {}", messageBytes);
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