/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bluetooth.notification;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BluetoothScanNotification} provides a notification of a received scan packet
 *
 * @author Chris Jackson - Initial contribution
 * @author Peter Rosenberg - Add support for ServiceData
 */
@NonNullByDefault
public class BluetoothScanNotification extends BluetoothNotification {
    /**
     * The receive signal strength for this beacon packet
     */
    private int rssi = Integer.MIN_VALUE;

    /**
     * The raw data
     */
    private byte[] data = new byte[0];

    /**
     * The manufacturer specific data
     */
    private byte[] manufacturerData = new byte[0];

    /**
     * The service data.
     * Key: UUID of the service
     * Value: Data of the characteristic
     */
    private Map<String, byte[]> serviceData = new HashMap<String, byte[]>();

    /**
     * The beacon type
     */
    private BluetoothBeaconType beaconType = BluetoothBeaconType.BEACON_UNKNOWN;

    /**
     * The device name
     */
    private String name = new String();

    /**
     * An enumeration of basic beacon types
     */
    public enum BluetoothBeaconType {
        BEACON_UNKNOWN,
        BEACON_ADVERTISEMENT,
        BEACON_SCANRESPONSE
    }

    /**
     * Sets the receive signal strength RSSI value for the scan
     *
     * param rssi the RSSI value for the scan packet in dBm
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * Gets the receive signal strength RSSI value for the scan
     *
     * @return the RSSI value for the scan packet in dBm or Integer.MIN_VALUE if no RSSI is available.
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Sets the scan packet data
     *
     * @param data a byte array containing the raw packet data;
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the scan packet data
     *
     * @return a byte array containing the data or null if none is set
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the scan packet manufacturer specific data
     *
     * @param manufacturerData a byte array containing the manufacturer specific data
     */
    public void setManufacturerData(byte[] manufacturerData) {
        this.manufacturerData = manufacturerData;
    }

    /**
     * Gets the scan packet manufacturer specific data
     *
     * @return a byte array containing the manufacturer specific data or null if none is set
     */
    public byte[] getManufacturerData() {
        return manufacturerData;
    }

    public void setServiceData(Map<String, byte[]> serviceData) {
        this.serviceData = serviceData;
    }

    public Map<String, byte[]> getServiceData() {
        return serviceData;
    }

    /**
     * Sets the beacon type for this packet
     *
     * @beaconType the {@link BluetoothBeaconType} for this packet
     */
    public void setBeaconType(BluetoothBeaconType beaconType) {
        this.beaconType = beaconType;
    }

    /**
     * Gets the beacon type for this packet
     *
     * @return the {@link BluetoothBeaconType} for this packet
     */
    public BluetoothBeaconType getBeaconType() {
        return beaconType;
    }

    /**
     * Sets the device name
     *
     * @param name {@link String} containing the device name
     */
    public void setDeviceName(String name) {
        this.name = name;
    }

    /**
     * Gets the device name
     *
     * @return {@link String} containing the device name
     */
    public String getDeviceName() {
        return name;
    }
}
