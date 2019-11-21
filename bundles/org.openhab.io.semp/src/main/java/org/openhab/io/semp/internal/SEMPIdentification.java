/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

/**
 * SEMP consumers identification
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPIdentification {
    /*
     * Unique identification of the device.
     */
    private String deviceId;

    /*
     * Human readable device name
     */
    private String deviceName;

    /*
     * Human readable type of the device. See DeviceTypeRefType for well-known types.
     */
    private String deviceType;

    /*
     * Vendor specific serial number of the device
     */
    private String deviceSerial;

    /*
     * Human readable name of the device vendor
     */
    private String deviceVendor;

    /*
     * Configuration URL of the device.
     */
    private String deviceURL; /* occurs: 0 .. 1 */

    public SEMPIdentification() {
    }

    /*
     * Setter for DeviceId
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /*
     * Getter for DeviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /*
     * Checks if field DeviceId is set
     */
    public boolean isDeviceIdSet() {
        if (deviceId == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /*
     * Getter for deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /*
     * Checks if field deviceName is set
     */
    public boolean isDeviceNameSet() {
        if (deviceName == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for deviceType
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /*
     * Getter for deviceType
     */
    public String getDeviceType() {
        return deviceType;
    }

    /*
     * Checks if field deviceType is set
     */
    public boolean isDeviceTypeSet() {
        if (deviceType == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for deviceSerial
     */
    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    /*
     * Getter for deviceSerial
     */
    public String getDeviceSerial() {
        return deviceSerial;
    }

    /*
     * Checks if field deviceSerial is set
     */
    public boolean isDeviceSerialSet() {
        if (deviceSerial == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for deviceVendor
     */
    public void setDeviceVendor(String deviceVendor) {
        this.deviceVendor = deviceVendor;
    }

    /*
     * Getter for deviceVendor
     */
    public String getDeviceVendor() {
        return deviceVendor;
    }

    /*
     * Checks if field deviceVendor is set
     */
    public boolean isDeviceVendorSet() {
        if (deviceVendor == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for deviceSerial
     */
    public void setDeviceURL(String deviceURL) {
        this.deviceURL = deviceURL;
    }

    /*
     * Getter for deviceSerial
     */
    public String getDeviceURL() {
        return deviceURL;
    }

    /*
     * Checks if field deviceSerial is set
     */
    public boolean isDeviceURLSet() {
        if (deviceURL == null) {
            return false;
        } else {
            return true;
        }
    }
}
