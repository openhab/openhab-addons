/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.eclipse.smarthome.io.net.actions.Ping;

/**
 * Runs a Ping in its own Thread
 * @author Marc Mettke - Initial contribution
 */
class DiscoveryThread extends Thread {
	private final static int PING_TIMEOUT_IN_MS = 1000;
	
	private DiscoveryCallback discoveryCallback;
	private String ip;

	public DiscoveryThread(String ip, DiscoveryCallback discoveryCallback) {
		this.ip = ip;
		this.discoveryCallback = discoveryCallback;
	}

	@Override
	public void run() {
		try {
			if( Ping.checkVitality(this.ip, 0, PING_TIMEOUT_IN_MS) ) {
				this.discoveryCallback.newDevice(this.ip);
			}
		} 
		catch (SocketTimeoutException se) {
		}
		catch (IOException ioe) {
		}
	}
}
