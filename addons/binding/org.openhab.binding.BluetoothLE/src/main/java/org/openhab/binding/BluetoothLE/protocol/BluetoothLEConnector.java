/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.BluetoothLE.protocol;

/**
 * @author Patrick Ammann - Initial contribution
 */
public abstract class BluetoothLEConnector {

	/**
	 * @throws Exception
	 */
	public abstract void connect() throws Exception;

	/**
	 * @throws Exception
	 */
	public abstract void disconnect() throws Exception;

	/**
	 * @throws Exception
	 */
	public abstract Object getData() throws Exception;
	
	protected Object getData(byte[] bytes, int length) {
		if (length < 10) {
			return null;
		}
		
		//logger.debug("Received frame len={} data={}", packet.getLength(), Arrays.toString(bytes));
		
		if (bytes[0] == 3) // ADV_NONCONN_IND - Non connectable undirected advertising
		{
			String device = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
					bytes[2 + 5], bytes[2 + 4], bytes[2 + 3], bytes[2 + 2], bytes[2 + 1], bytes[2 + 0]);
			int rssi = bytes[length - 1];
			return new ScanResult(device, new ScanRecord(bytes, 9, length - 1), rssi, System.nanoTime());
		}
		
		return null;
	}
}
