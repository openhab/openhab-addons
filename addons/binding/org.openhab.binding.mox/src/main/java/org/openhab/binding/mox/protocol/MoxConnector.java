/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import static org.openhab.binding.mox.protocol.MoxMessageBuilder.messageBuilder;

/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 2.0.0
 */
public class MoxConnector extends Thread {

	private static final int RECEIVE_BUFFER_LENGTH = 32;

	private static final int SOCKET_TIMEOUT = 20000;

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
	
	/**
	 * The hostname is used to send UDP datagrams to - not the listening interface address!
	 * The Port will be used for listening for incoming datagrams.
	 * @param hostname IP or hostname of the MOX Gateway
	 * @param port Port to listen
	 */
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
		socket.setReuseAddress(true);
		socket.setSoTimeout(SOCKET_TIMEOUT);
		logger.debug("Connection to MOX Gateway established on {}:{}", hostname, port);
		return true;
	}
	
	@Override
	public void run() {
		byte[] buffer;
		DatagramPacket packet = null;
		MoxMessage moxMessage;
		if (!interrupted) {
			do {
				try {
					buffer = new byte[RECEIVE_BUFFER_LENGTH];
					packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					moxMessage = messageBuilder().parseFrom(packet.getData()).build();

					if (logger.isTraceEnabled()) {
						logger.trace("Received MOX Message [{}]", moxMessage.toStringForTrace());
					}

					messageListener.onMessage(moxMessage);
				} catch (SocketTimeoutException e) {
					logger.trace("Socket listening timeout of {}ms reached, will retry? {}!", SOCKET_TIMEOUT, !interrupted);
				} catch (IllegalArgumentException iae) {
					int[] convertedData = packet != null ? MoxMessageBuilder.getUnsignedIntArray(packet.getData()) : null;
					logger.warn("Parsing message '{}' failed because: {}", convertedData, iae.getLocalizedMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!interrupted);
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
