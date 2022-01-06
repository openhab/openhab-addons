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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an Neeo Scenario (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoScenario {

    /** The scenario name */
    @Nullable
    private String name;

    /** The main device key */
    @Nullable
    private String mainDeviceKey;

    /** The volume device key */
    @Nullable
    private String volumeDeviceKey;

    /** The scenario key */
    @Nullable
    private String key;

    /** Whether the scenario is pending configuration */
    private boolean configured;

    /** The associated room key */
    @Nullable
    private String roomKey;

    /** The associated room name */
    @Nullable
    private String roomName;

    /** The devices that make up the scenario */
    private String @Nullable [] devices;

    // may be used in the future
    // private final NeeoShortcut[] shortcuts;
    // @Nullable private String[] deviceInputMacroNames;
    // private final NeeoCapability[] capabilities;

    /**
     * Gets the scenario name
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Gets the main device key
     *
     * @return the main device key
     */
    @Nullable
    public String getMainDeviceKey() {
        return mainDeviceKey;
    }

    /**
     * Gets the volume device key
     *
     * @return the volume device key
     */
    @Nullable
    public String getVolumeDeviceKey() {
        return volumeDeviceKey;
    }

    /**
     * Gets the scenario key
     *
     * @return the key
     */
    @Nullable
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
    @Nullable
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Gets the associated room name
     *
     * @return the room name
     */
    @Nullable
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the devices
     *
     * @return the devices
     */
    @Nullable
    public String[] getDevices() {
        final String @Nullable [] localDevices = devices;
        return localDevices == null ? new String[0] : localDevices;
    }

    @Override
    public String toString() {
        return "NeeoScenario [name=" + name + ", mainDeviceKey=" + mainDeviceKey + ", volumeDeviceKey="
                + volumeDeviceKey + ", key=" + key + ", configured=" + configured + ", roomKey=" + roomKey
                + ", roomName=" + roomName + ", devices=" + Arrays.toString(devices) + "]";
    }
}
