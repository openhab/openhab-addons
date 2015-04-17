/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.BluetoothLE.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for UDP communication.
 * 
 * @author Patrick Ammann - Initial contribution
 */
public class BluetoothLEUDPConnector extends BluetoothLEConnector {

	private static final Logger logger = LoggerFactory.getLogger(BluetoothLEUDPConnector.class);

	static final int MAX_PACKET_SIZE = 260;

	int port = 9998;
	DatagramSocket socket = null;

	public BluetoothLEUDPConnector(int port) {
		this.port = port;
	}

	@Override
	public void connect() throws Exception {
		if (socket == null) {
			socket = new DatagramSocket(port);
			logger.debug("UDP message listener started");

		}
	}

	@Override
	public void disconnect() throws Exception {
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	@Override
	public Object getData() throws Exception {
		if (socket == null) {
			socket = new DatagramSocket(port);
		}

		// Create a packet
		DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
		// Receive a packet (blocking)
		socket.receive(packet);

		return getData(packet.getData(), packet.getLength());
	}
}
