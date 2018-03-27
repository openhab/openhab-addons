/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.max.internal.message.C_Message;
import org.openhab.binding.max.internal.message.Message;

/**
 * Base class for configuration provided by the MAX! Cube C_Message.
 *
 * @author Andreas Heil (info@aheil.de)
 * @since 1.4.0
 */
public final class DeviceConfiguration {

    private DeviceType deviceType = null;
    private String rfAddress = null;
    private String serialNumber = null;
    private String name = null;
    private int roomId = -1;
    private String roomName = null;

    /** Extended configuration properties **/
    private HashMap<String, Object> properties = new HashMap<>();

    private DeviceConfiguration() {
    }

    public static DeviceConfiguration create(Message message) {
        DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues((C_Message) message);

        return configuration;
    }

    public static DeviceConfiguration create(DeviceInformation di) {
        DeviceConfiguration configuration = new DeviceConfiguration();
        configuration.setValues(di.getRFAddress(), di.getDeviceType(), di.getSerialNumber(), di.getRoomId(),
                di.getName());
        return configuration;
    }

    public void setValues(C_Message message) {
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
