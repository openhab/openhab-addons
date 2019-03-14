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
package org.openhab.binding.hdl.internal.device;

/**
 * The DeviceInformation class is used to get the information in Devices.
 *
 * @author stigla - Initial contribution
 */
public class DeviceInformation {

    private DeviceType deviceType = DeviceType.Invalid;
    private int subNet = -1;
    private int deviceID = -1;
    private String serialNr = "";

    public DeviceInformation(String SerialNr, DeviceType deviceType, int subNet, int deviceID) {
        this.serialNr = SerialNr;
        this.deviceType = deviceType;
        this.subNet = subNet;
        this.deviceID = deviceID;
    }

    public int getsubNet() {
        return subNet;
    }

    public int getdeviceID() {
        return deviceID;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getSerialNr() {
        return serialNr;
    }
}
