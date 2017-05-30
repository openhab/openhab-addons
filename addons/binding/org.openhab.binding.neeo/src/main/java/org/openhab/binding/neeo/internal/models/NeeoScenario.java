/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

/**
 * The model representing an Neeo Scenario (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoScenario {

    /** The scenario name */
    private final String name;

    /** The main device key */
    private final String mainDeviceKey;

    /** The volume device key */
    private final String volumeDeviceKey;

    /** The scenario key */
    private final String key;

    /** Whether the scenario is pending configuration */
    private final boolean configured;

    /** The associated room key */
    private final String roomKey;

    /** The associated room name */
    private final String roomName;

    /** The devices that make up the scenario */
    private final String[] devices;

    // may be used in the future
    // private final NeeoShortcut[] shortcuts;
    // private final String[] deviceInputMacroNames;
    // private final NeeoCapability[] capabilities;

    /**
     * Instantiates a new neeo scenario.
     *
     * @param name the name
     * @param mainDeviceKey the main device key
     * @param volumeDeviceKey the volume device key
     * @param key the key
     * @param configured the configured
     * @param roomKey the room key
     * @param roomName the room name
     * @param devices the devices
     */
    public NeeoScenario(String name, String mainDeviceKey, String volumeDeviceKey, String key, boolean configured,
            String roomKey, String roomName, String[] devices) {
        this.name = name;
        this.mainDeviceKey = mainDeviceKey;
        this.volumeDeviceKey = volumeDeviceKey;
        this.key = key;
        this.configured = configured;
        this.roomKey = roomKey;
        this.roomName = roomName;
        this.devices = devices;
    }

    /**
     * Gets the scenario name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the main device key
     *
     * @return the main device key
     */
    public String getMainDeviceKey() {
        return mainDeviceKey;
    }

    /**
     * Gets the volume device key
     *
     * @return the volume device key
     */
    public String getVolumeDeviceKey() {
        return volumeDeviceKey;
    }

    /**
     * Gets the scenario key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Checks if the scenario is pending configuration
     *
     * @return true, if is configured
     */
    public boolean isConfigured() {
        return configured;
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
     * Gets the associated room name
     *
     * @return the room name
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the devices
     *
     * @return the devices
     */
    public String[] getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return "NeeoScenario [name=" + name + ", mainDeviceKey=" + mainDeviceKey + ", volumeDeviceKey="
                + volumeDeviceKey + ", key=" + key + ", configured=" + configured + ", roomKey=" + roomKey
                + ", roomName=" + roomName + ", devices=" + Arrays.toString(devices) + "]";
    }
}
