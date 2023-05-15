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
package org.openhab.binding.rainsoft.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.rainsoft.internal.data.RainSoftDevice;
import org.openhab.binding.rainsoft.internal.errors.DeviceNotFoundException;
import org.openhab.binding.rainsoft.internal.errors.DuplicateIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton registry of found devices.
 *
 * @author Ben Rosenblum - Initial contribution
 */

public class RainSoftDeviceRegistry {

    /**
     * static Singleton instance.
     */
    private static volatile RainSoftDeviceRegistry instance;
    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(RainSoftDeviceRegistry.class);
    /**
     * Will be set after initialization.
     */
    private boolean initialized;

    /**
     * Key: device id.
     * Value: the RainSoftDevice implementation object.
     */
    private ConcurrentHashMap<String, RainSoftDevice> devices;

    /**
     * Private constructor for singleton.
     */
    private RainSoftDeviceRegistry() {
        devices = new ConcurrentHashMap<>();
    }

    /**
     * Return a singleton instance of RainSoftDeviceRegistry.
     */
    public static RainSoftDeviceRegistry getInstance() {
        if (instance == null) {
            synchronized (RainSoftDeviceRegistry.class) {
                if (instance == null) {
                    instance = new RainSoftDeviceRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * Add a new rainsoft device.
     */
    public void addRainSoftDevice(RainSoftDevice rainSoftDevice) throws DuplicateIdException {
        if (rainSoftDevice == null) {
            logger.debug("Ignoring null rainSoftDevice");
        } else {
            if (devices.containsKey(rainSoftDevice.getId())) {
                logger.trace("RainSoft device with duplicate id {} ignored", rainSoftDevice.getId());
                throw new DuplicateIdException(
                        "RainSoft device with duplicate id " + rainSoftDevice.getId() + " ignored");
            } else {
                logger.debug("New RainSoft device with id {} added", rainSoftDevice.getId());
                rainSoftDevice.setRegistrationStatus(Status.ADDED);
                devices.put(rainSoftDevice.getId(), rainSoftDevice);
            }
        }
    }

    /**
     * Add a new rainsoft device collection.
     */
    public synchronized void addRainSoftDevices(Collection<RainSoftDevice> rainSoftDevices) {
        for (RainSoftDevice device : rainSoftDevices) {
            logger.debug("RainSoftDeviceRegistry - addRainSoftDevices - Trying: {}", device.getId());
            try {
                addRainSoftDevice(device);
            } catch (DuplicateIdException e) {
                logger.debug("RainSoft device with duplicate id {} ignored", device.getId());
            }
        }
        initialized = true;
    }

    /**
     * Return true after the registry is filled with devices.
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Get the device registered with the given id.
     *
     * @param id the device id.
     * @return the RainSoftDevice instance from the registry.
     * @throws DeviceNotFoundException
     */
    public RainSoftDevice getRainSoftDevice(String id) throws DeviceNotFoundException {
        if (devices.containsKey(id)) {
            return devices.get(id);
        } else {
            throw new DeviceNotFoundException("Device with id '" + id + "' not found");
        }
    }

    /**
     * Remove the device registered with the given id.
     *
     * @param id the device id.
     * @throws DeviceNotFoundException
     */
    public void removeRainSoftDevice(String id) throws DeviceNotFoundException {
        if (devices.containsKey(id)) {
            devices.remove(id);
        } else {
            throw new DeviceNotFoundException("Device with id '" + id + "' not found");
        }
    }

    /**
     * Get a collection with RainSoftDevices with the given status.
     *
     * @param filter the registration status to filter on.
     * @return the (possibly empty) collection.
     */
    public Collection<RainSoftDevice> getRainSoftDevices(Status filterStatus) {
        List<RainSoftDevice> result = new ArrayList<>();
        for (RainSoftDevice device : devices.values()) {
            if (device.getRegistrationStatus().equals(filterStatus)) {
                result.add(device);
            }
        }
        return result;
    }

    /**
     * Set the registration status.
     *
     * @param id the id of the RainSoftDevice.
     * @param status the new registration status.
     */
    public void setStatus(String id, Status status) {
        RainSoftDevice result = devices.get(id);
        if (result != null) {
            result.setRegistrationStatus(status);
        }
    }

    /**
     * The registry status of the device.
     *
     */
    public enum Status {
        /**
         * When first added to the registry, the status will be 'ADDED'.
         */
        ADDED,
        /**
         * When reported to the system as discovered device. It will show up
         * in the inbox.
         */
        DISCOVERED,
        /**
         * When a thing is created, the status will be configured.
         */
        CONFIGURED;
    }
}
