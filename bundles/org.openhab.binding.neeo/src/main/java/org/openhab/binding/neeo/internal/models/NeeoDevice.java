/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a NEEO Device (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDevice {

    /** The device name */
    @Nullable
    private String name;

    /** The associated room name */
    @Nullable
    private String roomName;

    /** The associated room key */
    @Nullable
    private String roomKey;

    /** The adapter device id */
    @Nullable
    private String adapterDeviceId;

    /** The device key */
    @Nullable
    private String key;

    /** The macros for the device */
    @Nullable
    private NeeoMacros macros;

    /** The device details */
    @Nullable
    private NeeoDeviceDetails details;

    /**
     * Gets the device name
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
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
     * Gets the associated room key
     *
     * @return the room key
     */
    @Nullable
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Gets the adapter device id
     *
     * @return the adapter device id
     */
    @Nullable
    public String getAdapterDeviceId() {
        return adapterDeviceId;
    }

    /**
     * Gets the macro key
     *
     * @return the key
     */
    @Nullable
    public String getKey() {
        return key;
    }

    /**
     * Gets the macros for the device
     *
     * @return the macros
     */
    public NeeoMacros getMacros() {
        final NeeoMacros localMacros = macros;
        return localMacros == null ? new NeeoMacros(new NeeoMacro[0]) : localMacros;
    }

    /**
     * Gets the details for the device
     *
     * @return the details
     */
    @Nullable
    public NeeoDeviceDetails getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "NeeoDevice [name=" + name + ", roomName=" + roomName + ", roomKey=" + roomKey + ", adapterDeviceId="
                + adapterDeviceId + ", key=" + key + ", macros=" + macros + ", details=" + details + "]";
    }
}
