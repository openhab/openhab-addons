/**
* Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/

package org.openhab.binding.BluetoothLE.protocol;


/**
 * The {@link ScanResult} inspired by Android API
 * 
 * @author Patrick Ammann - Initial contribution
 */
public final class ScanResult {
	private String deviceAddress;
	private int rssi;
	private ScanRecord scanRecord;
	private long timestampNanos;
	
	
	public ScanResult(String deviceAddress, ScanRecord scanRecord, int rssi, long timestampNanos) {
		this.deviceAddress = deviceAddress;
		this.rssi = rssi;
		this.scanRecord = scanRecord;
		this.timestampNanos = timestampNanos;
	}

	/**
	 * @return the remote bluetooth device identified by the bluetooth device address.
	 * 
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * @return the received signal strength in dBm.
	 */
	public int getRssi() {
		return rssi;
	}

	/**
	 * 
	 * @return the scan record, which is a combination of advertisement and scan response.
	 */
	public ScanRecord getScanRecord() {
		return scanRecord;
	}
	
	/**
	 * 
	 * @return timestamp since boot when the scan record was observed. 
	 */
	public long getTimestampNanos() {
		return timestampNanos;
	}
	
	public String toString() {
		return String.format("Device:%s RSSI:%s record:%s", deviceAddress, rssi, scanRecord.toString());
	}
}
