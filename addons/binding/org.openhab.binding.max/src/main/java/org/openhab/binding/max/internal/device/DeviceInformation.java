/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
