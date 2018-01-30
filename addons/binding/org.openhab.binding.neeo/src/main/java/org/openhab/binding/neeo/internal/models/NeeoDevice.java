/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an NEEO Device (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDevice {

    /** The device name */
    private final String name;

    /** The associated room name */
    private final String roomName;

    /** The associated room key */
    private final String roomKey;

    /** The adapter device id */
    private final String adapterDeviceId;

    /** The device key */
    private final String key;

    /** The macros for the device */
    private final NeeoMacros macros;

    /** The device details */
    private final NeeoDeviceDetails details;

    /**
     * Instantiates a new neeo device.
     *
     * @param name the name
     * @param roomName the room name
     * @param roomKey the room key
     * @param adapterDeviceId the adapter device id
     * @param key the key
     * @param macros the macros
     * @param details the device details
     */
    public NeeoDevice(String name, String roomName, String roomKey, String adapterDeviceId, String key,
            NeeoMacros macros, NeeoDeviceDetails details) {
        this.name = name;
        this.roomName = roomName;
        this.roomKey = roomKey;
        this.adapterDeviceId = adapterDeviceId;
        this.key = key;
        this.macros = macros;
        this.details = details;
    }

    /**
     * Gets the device name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the associated room name
     *
     * @return the room name
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the associated room key
     *
     * @return the room key
     */
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Gets the adapter device id
     *
     * @return the adapter device id
     */
    public String getAdapterDeviceId() {
        return adapterDeviceId;
    }

    /**
     * Gets the macro key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the macros for the device
     *
     * @return the macros
     */
    public NeeoMacros getMacros() {
        return macros;
    }

    /**
     * Gets the details for the device
     *
     * @return the details
     */
    public NeeoDeviceDetails getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "NeeoDevice [name=" + name + ", roomName=" + roomName + ", roomKey=" + roomKey + ", adapterDeviceId="
                + adapterDeviceId + ", key=" + key + ", macros=" + macros + ", details=" + details + "]";
    }
}
