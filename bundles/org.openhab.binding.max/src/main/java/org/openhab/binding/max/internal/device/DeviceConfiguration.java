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

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.max.internal.message.CMessage;
import org.openhab.binding.max.internal.message.Message;

/**
 * Base class for configuration provided by the MAX! Cube C Message.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 */
public final class DeviceConfiguration {

    private DeviceType deviceType;
    private String rfAddress;
    private String serialNumber;
    private String name;
    private int roomId = -1;
    private String roomName;

    /** Extended configuration properties **/
    private Map<String, Object> properties = new HashMap<>();

    private DeviceConfiguration() {
    }

    public static DeviceConfiguration create(Message message) {
        final DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues((CMessage) message);

        return configuration;
    }

    public static DeviceConfiguration create(DeviceInformation di) {
        DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues(di.getRFAddress(), di.getDeviceType(), di.getSerialNumber(), di.getRoomId(),
                di.getName());
        return configuration;
    }

    public void setValues(CMessage message) {
        setValues(message.getRFAddress(), message.getDeviceType(), message.getSerialNumber(), message.getRoomID());
        properties = new HashMap<>(message.getProperties());
    }

    private void setValues(String rfAddress, DeviceType deviceType, String serialNumber, int roomId, String name) {
        setValues(rfAddress, deviceType, serialNumber, roomId);
        this.name = name;
    }

    private void setValues(String rfAddress, DeviceType deviceType, String serialNumber, int roomId) {
        this.rfAddress = rfAddress;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
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

    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        if (roomName == null) {
            return "";
        } else {
            return roomName;
        }
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }
}
