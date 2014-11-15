/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;

import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatic UDP discovery of a MAX!Cube Lan Gateway on the local network. 
 * 
 * @author Marcel Verpaalen, based on UDP client code of Michiel De Mey 
 * @author Marcel Verpaalen, major revision for OH2 allowing discovery of a specific Max!Cube based on serial 
 * @since 1.4.0
 */
public final class MaxCubeDiscover {

	static final String MAXCUBE_DISCOVER_STRING ="eQ3Max*\0**********I";
	static final String RFADDRESS ="rfAddress";
	static Logger logger = LoggerFactory.getLogger(MaxCubeDiscover.class);
	static boolean discoveryRunning = false;

	/**
	 * Automatic UDP discovery of a MAX!Cube
	 * @param
	 * @return if the cube is found, returns the IP address as a string. Otherwise returns null
	 */
	public final static String discoverIp () {
		HashMap<String, String > discoverResults = new HashMap<String, String>(DiscoverCube(null));
		if (discoverResults.containsKey(MaxCubeBridgeConfiguration.IP_ADDRESS)){
			return discoverResults.get(MaxCubeBridgeConfiguration.IP_ADDRESS);
		} else {
			logger.debug( "No Max!Cube Lan Gateway discovery on the network.");
			return null;
		}
	}

	/**
	 * Automatic UDP discovery of a MAX!Cube
	 * @return if the cube is found, returns the HashMap containing the details.
	 */
	public synchronized final static HashMap<String, String > DiscoverCube(final String cubeSerialNumber) {
		discoveryRunning = true;
		HashMap<String, String > discoverResults = new HashMap<String, String>();
		String maxCubeIP = null;
		String maxCubeName = null;
		String serialNumber = null;
		String rfAddress = null;

		Thread thread = new Thread("Sendbroadcast"){
			 public void run(){
				 if (cubeSerialNumber !=null){
				 sendBroadcastforDevice(cubeSerialNumber);
				 } else {
					 sendBroadcast();			
				 }
			try {
				sleep(5000);
			} catch (Exception e) {
			}
			discoveryRunning = false;
			logger.trace( "Done sending broadcast discovery messages.");
			 }
		};
		thread.start();
		DatagramSocket bcReceipt = null;

		try{
			bcReceipt = new DatagramSocket(23272);
			bcReceipt.setReuseAddress(true);
			bcReceipt.setSoTimeout(10000);
		
			while (discoveryRunning){
			//Wait for a response
			byte[] recvBuf = new byte[1500];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			bcReceipt.receive(receivePacket);

			//We have a response
			String message = new String(receivePacket.getData()).trim();
			logger.trace( "Broadcast response from {} : {} '{}'", receivePacket.getAddress(),message.length(),message);

			//Check if the message is correct
			if (message.startsWith("eQ3Max") &&  !message.equals(MAXCUBE_DISCOVER_STRING)) {
				maxCubeIP=receivePacket.getAddress().getHostAddress();
				maxCubeName=message.substring(0, 8);
				serialNumber=message.substring(8, 18);
				byte[] unknownData=message.substring(18,21).getBytes();
				rfAddress=Utils.getHex(message.substring(21).getBytes()).replace(" ", "").toLowerCase();
				logger.info("Max!Cube found on network");
				logger.debug("Found at  : {}", maxCubeIP);
				logger.debug("Name      : {}", maxCubeName);
				logger.debug("Serial    : {}", serialNumber);
				logger.debug("RF Address: {}", rfAddress);
				logger.trace("Unknown   : {}", Utils.getHex(unknownData));
			}
			}
		} catch (SocketTimeoutException ex) {
			logger.debug("No further response");
		} catch (IOException ex) {
			logger.debug(ex.toString());
		} finally {
			//Close the port!		
			try {
				if (bcReceipt !=null) 
					bcReceipt.close();
			}	catch (Exception e) {
				logger.debug(e.toString());
			}
		}
		//TODO add this to a discoverResults array to deal with possible multiple Lan gateways on the network
		discoverResults.put(MaxCubeBridgeConfiguration.IP_ADDRESS, maxCubeIP);
		discoverResults.put(MaxCubeConfiguration.FRIENDLY_NAME, maxCubeName);
		discoverResults.put(MaxCubeConfiguration.SERIAL_NUMBER, serialNumber);
		discoverResults.put(RFADDRESS, serialNumber);
		return discoverResults;
	}

	/**
	 * Send broadcast message over all active interfaces for a specific device
	 * @param serialnumber of the Max!Cube lan gateway searched.
	 */
	private static void sendBroadcastforDevice(String serialnumber) { 
		sendBroadcastMessage ("eQ3Max*\0" + serialnumber + "I");
	}

	/**
	 * Send a generic broadcast message over all active interfaces to find all devices
	 */
	private static void sendBroadcast() { 
		sendBroadcastMessage (MAXCUBE_DISCOVER_STRING);
	}

	/**
	 * Send broadcast message over all active interfaces
	 * @param discoverString String to be used for the discovery
	 */
	private static void sendBroadcastMessage(String discoverString) {
		DatagramSocket bcSend = null;
		//Find the MaxCube using UDP broadcast
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
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					//ugly hack to workaround Java issue of wrong broadcast address for Wlan devices 
					//http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7158636
							byte[] networkIpAddress = interfaceAddress.getAddress().getAddress();
							byte[] broadcastIpAddress = broadcast.getAddress();
						
							if (networkIpAddress[0] != broadcastIpAddress[0]){
								broadcastIpAddress = networkIpAddress;
								broadcastIpAddress[3]= (byte) 0xFF;
								InetAddress newbroadcast =  InetAddress.getByAddress(broadcastIpAddress);
								logger.debug( "Strange broadcast address '{}' for IP {}, replaced with '{}' Interface: '{}' '{}'", broadcast.getHostAddress(), interfaceAddress.getAddress(),newbroadcast.getHostAddress(),  networkInterface.getDisplayName(),  networkInterface.getName());
								broadcast = newbroadcast;
							}
							// Send the broadcast package!
							try {
								DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 23272);
								logger.debug( "Sending request packet sent to: {} Interface: '{}' '{}'", broadcast.getHostAddress(),  networkInterface.getDisplayName(),  networkInterface.getName());
						bcSend.send(sendPacket);
					} catch (Exception e) {
						logger.debug( "Error while sending request packet sent to: {} Interface: '{}' '{}'", broadcast.getHostAddress(),  networkInterface.getDisplayName(),  networkInterface.getName());

						logger.debug(e.getMessage());
						logger.debug(Utils.getStackTrace(e));
					}

					logger.debug( "Request packet sent to: {} Interface: {}", broadcast.getHostAddress(),  networkInterface.getDisplayName());
				}
			}

		} catch (IOException ex) {
			logger.debug(ex.toString());
		}finally {
			try {
				if (bcSend !=null) 
					bcSend.close();
			}	catch (Exception e) {
				logger.debug(e.toString());
			}}
		 
	}
}

