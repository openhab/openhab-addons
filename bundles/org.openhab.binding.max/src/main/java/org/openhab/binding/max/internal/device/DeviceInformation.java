/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.device;

/**
 * Device information provided by the M message meta information.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 */
public class DeviceInformation {

    private final DeviceType deviceType;
    private final String serialNumber;
    private final String rfAddress;
    private final String name;
    private final int roomId;

    public DeviceInformation(DeviceType deviceType, String serialNumber, String rfAddress, String name, int roomId) {
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.rfAddress = rfAddress;
        this.name = name;
        this.roomId = roomId;
    }

    public String getRFAddress() {
        return rfAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }
}
