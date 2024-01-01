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
package org.openhab.binding.homematic.internal.discovery.eq3udp;

/**
 * Extracts a UDP response from a Homematic CCU gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public class Eq3UdpResponse {
    private int senderId;
    private String deviceTypeId;
    private String serialNumber;

    /**
     * Extracts the received UDP response.
     */
    public Eq3UdpResponse(byte[] buffer) throws IndexOutOfBoundsException {
        int index = 0;
        byte protocolVersion = buffer[(index++)];
        if (protocolVersion == 2) {
            senderId = readInt(buffer, index, 3);
            index += 4;
        }
        deviceTypeId = readString(buffer, index++);
        index += deviceTypeId.length();
        serialNumber = readString(buffer, index);
    }

    /**
     * Returns the device type of the gateway.
     */
    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    /**
     * Returns the serial number of the gateway.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns true, if this response is from a Homematic CCU gateway.
     */
    public boolean isValid() {
        return this.senderId == Eq3UdpRequest.getSenderId()
                && (deviceTypeId.startsWith("eQ3-HM-CCU") || deviceTypeId.startsWith("eQ3-HmIP-CCU3"))
                && !serialNumber.contains(Eq3UdpRequest.getEq3SerialNumber());
    }

    private String readString(byte[] data, int index) throws IndexOutOfBoundsException {
        String result = "";
        for (int i = index; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
            result += (char) data[i];
        }
        return result;
    }

    private int readInt(byte[] data, int index, int length) throws IndexOutOfBoundsException {
        int result = 0;
        for (int n = index; n < index + length; n++) {
            result <<= 8;
            result |= data[n] & 0xFF;
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s[deviceTypeId=%s,serialNumber=%s]", getClass().getSimpleName(), deviceTypeId,
                serialNumber);
    }
}
