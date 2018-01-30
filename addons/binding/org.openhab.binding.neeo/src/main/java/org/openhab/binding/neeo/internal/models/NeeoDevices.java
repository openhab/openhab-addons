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

import org.apache.commons.lang.StringUtils;

/**
 * The model representing Neeo Devices (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDevices {

    /** The devices. */
    private final NeeoDevice[] devices;

    /**
     * Instantiates a new neeo devices.
     *
     * @param devices the devices
     */
    public NeeoDevices(NeeoDevice[] devices) {
        this.devices = devices;
    }

    /**
     * Gets the devices.
     *
     * @return the devices
     */
    public NeeoDevice[] getDevices() {
        return devices;
    }

    /**
     * Gets the device.
     *
     * @param key the key
     * @return the device
     */
    public NeeoDevice getDevice(String key) {
        for (NeeoDevice device : devices) {
            if (StringUtils.equalsIgnoreCase(key, device.getKey())) {
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
