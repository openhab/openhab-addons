/**
* Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/

package org.openhab.binding.BluetoothLE.protocol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;


/*
02 # Number of bytes that follow in the first AD structure
01 # AD type: Flags
04 # Flags value 0x04 = 000000100  
   bit 0 (OFF) LE Limited Discoverable Mode
   bit 1 (OFF) LE General Discoverable Mode
   bit 2 (ON) BR/EDR Not Supported
   bit 3 (OFF) Simultaneous LE and BR/EDR to Same Device Capable (controller)
   bit 4 (OFF) Simultaneous LE and BR/EDR to Same Device Capable (Host)

09 # Number of bytes that follow in the second AD Structure
09 # AD Type: Complete Local Name
43 39 35 37 30 31 35 46 # "C957015F"

07 # Number of bytes that follow in the third AD Structure
16 # ​AD Type: "Service Data AD Type - 16-bit UUID"	​Core Specification Supplement, Part A, section 1.11
09 18 # 16-bit Service UUID 0x1809 = Health thermometer (org.bluetooth.service.health_thermometer)
6c 07 00 fe # Additional Service Data 6c0700  (Temperature = 0x00076c x 10^-2) = 19.0 degrees

04 # Number of bytes that follow in the forth AD Structure
16 # ​AD Type: "Service Data AD Type - 16-bit UUID"	​Core Specification Supplement, Part A, section 1.11
0f 18 # 16-bit Service UUID 0x180F  = Battery Service (org.bluetooth.service.battery_service)
57 # Additional Service Data (battery level)

cc # rssi
*/

/**
 * The {@link ScanRecord} inspired by Android API
 * 
 * @author Patrick Ammann - Initial contribution
 */
public final class ScanRecord {
	private byte[] raw;
	
	private int advertiseFlags = 0;
	private String localName = null;
	private Map<UUID, byte[]> serviceData = new Hashtable<UUID, byte[]>();
	private Map<Integer, byte[]> manufacturerData = new Hashtable<Integer, byte[]>();
	private int txPowerLevel = 0;
	
	private byte[] getBytes(byte[] raw, int offset, int length) {
		ByteBuffer bytebuf = ByteBuffer.allocate(length);
		for (int i = 0; i < length; i++) {
			bytebuf.put(raw[offset + i]);
		}
		return bytebuf.array();
	}

	public ScanRecord(byte[] raw, int offset, int length) {
		this.raw = raw;
		for (int i = offset; i < length; ) {
			int num = raw[i];
			int type = raw[i+1];
			switch (type) {
				case 0x01:
					advertiseFlags = raw[i + 2];
					break;
				case 0x08:
				case 0x09:
					try {
						localName = new String(getBytes(raw, i + 2, num - 1), "UTF-8");
					} catch (UnsupportedEncodingException e) {
					}
					break;
				case 0x0a:
					txPowerLevel = raw[i + 2];
					break;
				case 0x16: // 16bit UUID
					UUID uuid = new UUID(0, raw[i+2] + (raw[i+3] << 8));
					byte[] b16 = getBytes(raw, i + 4, num - 3);
					serviceData.put(uuid, b16);
					break;
				case 0xFF: // manufacturer specific data
					Integer mid = new Integer(raw[i+2] + (raw[i+3] << 8));
					byte[] bmid = getBytes(raw, i + 4, num - 3);
					manufacturerData.put(mid, bmid);
					break;
				default:
					// not handled yet
					break;
					
			}
			i += num + 1;
		}
	}
	
	/**
	 * 
	 * @return the advertising flags indicating the discoverable mode and capability of the device.
	 */
	public int getAdvertiseFlags() {
		return advertiseFlags;
	}

	/**
	 * 
	 * @return raw bytes of scan record.
	 */
	public byte[] getBytes() {
		return raw;
	}

	/**
	 * 
	 * @return the local name of the BLE device.
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * 
	 * @return a map of manufacturer identifier and its corresponding manufacturer specific data.
	 */
	public Map<Integer, byte[]> getManufacturerSpecificData() {
		return manufacturerData;
	}
	
	/**
	 * 
	 * @param manufacturerId
	 * @return the manufacturer specific data associated with the manufacturer id.
	 */
	byte[] getManufacturerSpecificData(int manufacturerId) {
		return manufacturerData.get(manufacturerId);
	}

	/**
	 * 
	 * @return a map of service UUID and its corresponding service data.
	 */
	public Map<UUID, byte[]> getServiceData() {
		return serviceData;
	}

	/**
	 * 
	 * @return a list of service UUIDs within the advertisement that are used to identify the bluetooth GATT services.
	 */
	public List<UUID> getServiceUuids() {
		return new ArrayList<UUID>(serviceData.keySet());
	}

	/**
	 * 
	 * @param serviceDataUuid
	 * @return the service data byte array associated with the serviceUuid.
	 */
	public byte[] getServiceData(UUID serviceDataUuid) {
		return serviceData.get(serviceDataUuid);
	}

	/**
	 * 
	 * @return the transmission power level of the packet in dBm.
	 */
	public int getTxPowerLevel() {
		return txPowerLevel;
	}
	
	public String toString() {
		return DatatypeConverter.printHexBinary(raw);
	}
}
