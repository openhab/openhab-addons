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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing Neeo Devices (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDevices {

    /** The devices. */
    private final NeeoDevice @Nullable [] devices;

    /**
     * Instantiates a new neeo devices.
     *
     * @param devices the devices
     */
    NeeoDevices(NeeoDevice[] devices) {
        Objects.requireNonNull(devices, "devices cannot be null");
        this.devices = devices;
    }

    /**
     * Gets the devices.
     *
     * @return the devices
     */
    public NeeoDevice[] getDevices() {
        final NeeoDevice[] localDevices = devices;
        return localDevices == null ? new NeeoDevice[0] : localDevices;
    }

    /**
     * Gets the device.
     *
     * @param key the key
     * @return the device
     */
    @Nullable
    public NeeoDevice getDevice(String key) {
        for (NeeoDevice device : getDevices()) {
            if (key.equalsIgnoreCase(device.getKey())) {
                return device;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NeeoDevice [devices=" + Arrays.toString(devices) + "]";
    }
}
