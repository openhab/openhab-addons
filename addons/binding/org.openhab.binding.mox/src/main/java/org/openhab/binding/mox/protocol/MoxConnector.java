/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 0.8.0
 */
public class MoxConnector extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(MoxConnector.class);
	
	private static boolean interrupted = false;
	
	/** The UDP socket */
	private DatagramSocket socket;

	/** The MOX Gateway hostname */
	private String hostname;

	/** The MOX Gateway port */
	private int port;

	/** Buffered Inputstream for MOX communication*/
	protected BufferedReader in;

	/** Buffered Outputstream for MOX communication*/
	protected BufferedWriter out;
	
	private MoxMessageListener messageListener;
	
	private static Map<Integer, String> subOids = new HashMap<Integer, String>();

	static {
		subOids.put(17, "Channel 1"); // 0x11
		subOids.put(18, "Channel 2"); // 0x12
		subOids.put(19, "Channel 3"); // 0x13
		subOids.put(20, "Channel 4"); // 0x14
		subOids.put(21, "Channel 5"); // 0,15
		subOids.put(22, "Channel 6"); // 0x16
		subOids.put(23, "Channel 7"); // 0x17
		subOids.put(24, "Channel 8"); // 0x18
		subOids.put(49, "REM Data");  // 0x31
	}

	public MoxConnector(String hostname, int port) {
		super("MOX Communication Thread");
		this.setDaemon(true);
		this.hostname = hostname;
		this.port = port;
	}

	
	public void setMessageHandler(MoxMessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public void removeMessageHandler() {
		this.messageListener = null;
	}

	/**
	 * Connects the connector to it's backend system. It's important
	 * to connect before start the thread.
	 * @return
	 * @throws IOException
	 */
	public boolean connect() throws IOException {
		socket = new DatagramSocket(port);
		socket.setSoTimeout(10000);
		logger.debug("Connection to MOX Gateway established on {}:{}", hostname, port);
		return true;
	}
	
	@Override
	public void run() {
		try {
			byte[] buffer = new byte[14];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (!interrupted) {
				socket.receive(packet);
				MoxMessage moxMessage = new MoxMessage(packet.getData());
				logger.trace("Received MOX Message [{}]", moxMessage);
				messageListener.onMessage(moxMessage);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disconnects the connector from it's backend system.
	 * @return
	 * @throws IOException
	 */
	public boolean disconnect() throws IOException {
		interrupted = true;
		if(socket != null) {
			socket.close();
			socket = null;
		}
		return true;
	}
	
	
	
	
	
}
