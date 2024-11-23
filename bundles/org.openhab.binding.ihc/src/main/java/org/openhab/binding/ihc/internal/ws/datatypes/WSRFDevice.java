/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.ws.datatypes;

/**
 * Class for WSRFDevice complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSRFDevice {

    private int batteryLevel;
    private int deviceType;
    private long serialNumber;
    private int signalStrength;
    private int version;
    private boolean detected;

    public WSRFDevice() {
    }

    public WSRFDevice(int batteryLevel, int deviceType, long serialNumber, int signalStrength, int version,
            boolean detected) {
        this.batteryLevel = batteryLevel;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.signalStrength = signalStrength;
        this.version = version;
        this.detected = detected;
    }

    /**
     * Gets the battery level value for this WSRFDevice.
     *
     * @return Battery Level
     */
    public int getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Sets the battery level value for this WSRFDevice.
     *
     * @param batteryLevel battery level
     */
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    /**
     * Gets the device type value for this WSRFDevice.
     *
     * @return device type
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the device type value for this WSRFDevice.
     *
     * @param deviceType device type
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Gets the serial number value for this WSRFDevice.
     *
     * @return Serial number
     */
    public long getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number value for this WSRFDevice.
     *
     * @param serialNumber Serial number
     */
    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Gets the signal strength value for this WSRFDevice.
     *
     * @return Signal strength
     */
    public int getSignalStrength() {
        return signalStrength;
    }

    /**
     * Sets the signal strength value for this WSRFDevice.
     *
     * @param signalStrength Signal strength
     */
    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    /**
     * Gets the version value for this WSRFDevice.
     *
     * @return version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version value for this WSRFDevice.
     *
     * @param version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the detected value for this WSRFDevice.
     *
     * @return Detected
     */
    public boolean getDetected() {
        return detected;
    }

    /**
     * Sets the detected value for this WSRFDevice.
     *
     * @param detected
     */
    public void setdetected(boolean detected) {
        this.detected = detected;
    }

    @Override
    public String toString() {
        return String.format(
                "[ batteryLevel=%d, deviceType=%d, serialNumber=%d, signalStrength=%d, version=%d, detected=%b ]",
                batteryLevel, deviceType, serialNumber, signalStrength, version, detected);
    }
}
