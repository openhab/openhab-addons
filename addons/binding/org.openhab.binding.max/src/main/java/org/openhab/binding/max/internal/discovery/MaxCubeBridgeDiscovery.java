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

	public MaxCubeBridgeDiscovery() {
		super(MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return MaxBinding.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}

	@Override
	public void startScan() {
		discoverCube();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#
	 * startBackgroundDiscovery()
	 */

	@Override
	protected void startBackgroundDiscovery() {
		discoverCube();
	}

	@Override
	public boolean isBackgroundDiscoveryEnabled() {
		return true;
	}

	private synchronized void discoverCube() {
		discoveryRunning = true;
		Thread thread = new Thread("Sendbroadcast") {
			public void run() {
				sendDiscoveryMessage(MAXCUBE_DISCOVER_STRING);
				try {
					sleep(5000);
				} catch (Exception e) {
				}
				discoveryRunning = false;
				logger.trace("Done sending broadcast discovery messages.");
			}
		};
		thread.start();
		receiveDiscoveryMessage();
	}

	private void receiveDiscoveryMessage() {
		String maxCubeIP = null;
		String maxCubeName = null;
		String serialNumber = null;
		String rfAddress = null;
		DatagramSocket bcReceipt = null;

		try {
			bcReceipt = new DatagramSocket(23272);
			bcReceipt.setReuseAddress(true);
			bcReceipt.setSoTimeout(10000);

			while (discoveryRunning) {
				// Wait for a response
				byte[] recvBuf = new byte[1500];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				bcReceipt.receive(receivePacket);

				// We have a response
				String message = new String(receivePacket.getData()).trim();
				logger.trace("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
						message);

				// Check if the message is correct
				if (message.startsWith("eQ3Max") && !message.equals(MAXCUBE_DISCOVER_STRING)) {
					maxCubeIP = receivePacket.getAddress().getHostAddress();
					maxCubeName = message.substring(0, 8);
					serialNumber = message.substring(8, 18);
					byte[] unknownData = message.substring(18, 21).getBytes();
					rfAddress = Utils.getHex(message.substring(21).getBytes()).replace(" ", "").toLowerCase();
					logger.debug("MAX! Cube found on network");
					logger.debug("Found at  : {}", maxCubeIP);
					logger.debug("Name      : {}", maxCubeName);
					logger.debug("Serial    : {}", serialNumber);
					logger.debug("RF Address: {}", rfAddress);
					logger.trace("Unknown   : {}", Utils.getHex(unknownData));
					discoveryResultSubmission(maxCubeIP, serialNumber);
				}
			}
		} catch (SocketTimeoutException e) {
			logger.trace("No further response");
		} catch (IOException e) {
			logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
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

	private void discoveryResultSubmission(String IpAddress, String cubeSerialNumber) {
		if (cubeSerialNumber != null) {
			logger.trace("Adding new MAX! Cube Lan Gateway on {} with id '{}' to Smarthome inbox", IpAddress,
					cubeSerialNumber);
			Map<String, Object> properties = new HashMap<>(2);
			properties.put(MaxBinding.IP_ADDRESS, IpAddress);
			properties.put(MaxBinding.SERIAL_NUMBER, cubeSerialNumber);
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
