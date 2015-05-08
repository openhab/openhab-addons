/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new MAX!
 * Cube LAN gateway devices on the network
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeDiscovery extends AbstractDiscoveryService {

	static final String MAXCUBE_DISCOVER_STRING = "eQ3Max*\0**********I";

	private final static Logger logger = LoggerFactory.getLogger(MaxCubeBridgeDiscovery.class);

	static boolean discoveryRunning = false;
	
	/** The refresh interval for discovery of MAX! Cubes */
	private long refreshInterval = 600;
	private ScheduledFuture<?> cubeDiscoveryJob;
	private Runnable cubeDiscoveryRunnable = new Runnable() {
		@Override
		public void run() {
			discoverCube();		}
	};
	
	public MaxCubeBridgeDiscovery() {
		super(MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, true);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}

	@Override
	public void startScan() {
		logger.debug("Start MAX! Cube discovery");
		scheduler.scheduleAtFixedRate(cubeDiscoveryRunnable, 0, refreshInterval, TimeUnit.SECONDS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#stopBackgroundDiscovery()
	 */
	@Override
	protected void stopBackgroundDiscovery() {
		logger.debug("Stop MAX! Cube background discovery");
		if (cubeDiscoveryJob != null && !cubeDiscoveryJob.isCancelled()) {
			cubeDiscoveryJob.cancel(true);
			cubeDiscoveryJob = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startBackgroundDiscovery()
	 */
	@Override
	protected void startBackgroundDiscovery() {
		logger.debug("Start MAX! Cube background discovery");
		if (cubeDiscoveryJob == null || cubeDiscoveryJob.isCancelled()) {
			cubeDiscoveryJob = scheduler.scheduleAtFixedRate(cubeDiscoveryRunnable, 10, refreshInterval, TimeUnit.SECONDS);
		}
	}

	private synchronized void discoverCube() {
		logger.debug("Run MAX! Cube discovery");
		sendDiscoveryMessage(MAXCUBE_DISCOVER_STRING);
		logger.trace("Done sending broadcast discovery messages.");
		receiveDiscoveryMessage();
		logger.debug("Done receiving discovery messages.");
	}

	private void receiveDiscoveryMessage() {

		DatagramSocket bcReceipt = null;

		try {
			discoveryRunning = true;
			bcReceipt = new DatagramSocket(23272);
			bcReceipt.setReuseAddress(true);
			bcReceipt.setSoTimeout(5000);

			while (discoveryRunning) {
				// Wait for a response
				byte[] recvBuf = new byte[1500];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				bcReceipt.receive(receivePacket);

				// We have a response
				String message = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());
				logger.trace("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
						message);

				// Check if the message is correct
				if (message.startsWith("eQ3Max") && !message.equals(MAXCUBE_DISCOVER_STRING)) {
					String maxCubeIP = receivePacket.getAddress().getHostAddress();
					String maxCubeState = message.substring(0, 8);
					String serialNumber = message.substring(8, 18);
					String msgValidid = message.substring(18, 19);
					String requestType = message.substring(19, 20);
					String rfAddress ="";
					logger.debug("MAX! Cube found on network");
					logger.debug("Found at  : {}", maxCubeIP);
					logger.debug("Cube State: {}", maxCubeState);
					logger.debug("Serial    : {}", serialNumber);
					logger.trace("Msg Valid : {}", msgValidid);
					logger.trace("Msg Type  : {}", requestType);

					if (requestType.equals( "I")) {
						rfAddress = Utils.getHex(message.substring(21, 24).getBytes()).replace(" ", "")
								.toLowerCase();
						String firmwareVersion = Utils.getHex(message.substring(24, 26).getBytes()).replace(" ", ".");
						logger.debug("RF Address: {}", rfAddress);
						logger.debug("Firmware  : {}", firmwareVersion);
					}
					discoveryResultSubmission(maxCubeIP, serialNumber, rfAddress);
				}
			}
		} catch (SocketTimeoutException e) {
			logger.trace("No further response");
			discoveryRunning = false;
		} catch (IOException e) {
			logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
			discoveryRunning = false;
		} finally {
			// Close the port!
			try {
				if (bcReceipt != null)
					bcReceipt.close();
			} catch (Exception e) {
				logger.debug(e.toString());
			}
		}
	}

	private void discoveryResultSubmission(String IpAddress, String cubeSerialNumber, String rfAddress) {
		if (cubeSerialNumber != null) {
			logger.trace("Adding new MAX! Cube Lan Gateway on {} with id '{}' to Smarthome inbox", IpAddress,
					cubeSerialNumber);
			Map<String, Object> properties = new HashMap<>(2);
			properties.put(MaxBinding.PROPERTY_IP_ADDRESS, IpAddress);
			properties.put(MaxBinding.PROPERTY_SERIAL_NUMBER, cubeSerialNumber);
			properties.put(MaxBinding.PROPERTY_RFADDRESS,rfAddress);	
			ThingUID uid = new ThingUID(MaxBinding.CUBEBRIDGE_THING_TYPE, cubeSerialNumber);
			if (uid != null) {
				DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
						.withLabel("MAX! Cube LAN Gateway").build();
				thingDiscovered(result);
			}
		}
	}

	/**
	 * Send broadcast message over all active interfaces
	 * 
	 * @param discoverString
	 *            String to be used for the discovery
	 */
	private void sendDiscoveryMessage(String discoverString) {
		DatagramSocket bcSend = null;
		// Find the MaxCube using UDP broadcast
		try {
			bcSend = new DatagramSocket();
			bcSend.setBroadcast(true);

			byte[] sendData = discoverString.getBytes();

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress[] broadcast = new InetAddress[3];
					broadcast[0] = InetAddress.getByName("224.0.0.1");
					broadcast[0] = InetAddress.getByName("255.255.255.255");
					broadcast[1] = interfaceAddress.getBroadcast();
					for (InetAddress bc : broadcast) {
						// Send the broadcast package!
						if (bc != null) {
							try {
								DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bc, 23272);
								bcSend.send(sendPacket);
							} catch (IOException e) {
								logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
							} catch (Exception e) {
								logger.info(e.getMessage(), e);
							}
							logger.trace("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
									networkInterface.getDisplayName());
						}
					}
				}
			}
			logger.trace("Done looping over all network interfaces. Now waiting for a reply!");

		} catch (IOException e) {
			logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
		} finally {
			try {
				if (bcSend != null)
					bcSend.close();
			} catch (Exception e) {
				// Ignore
			}
		}

	}

}