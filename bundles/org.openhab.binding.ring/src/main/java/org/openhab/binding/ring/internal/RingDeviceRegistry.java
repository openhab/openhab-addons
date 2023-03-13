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
package org.openhab.binding.ring.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.ring.internal.data.RingDevice;
import org.openhab.binding.ring.internal.errors.DeviceNotFoundException;
import org.openhab.binding.ring.internal.errors.DuplicateIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton registry of found devices.
 *
 * @author Wim Vissers - Initial contribution
 */

public class RingDeviceRegistry {

    /**
     * static Singleton instance.
     */
    private static volatile RingDeviceRegistry instance;
    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(RingDeviceRegistry.class);
    /**
     * Will be set after initialization.
     */
    private boolean initialized;

    /**
     * Key: device id.
     * Value: the RingDevice implementation object.
     */
    private ConcurrentHashMap<String, RingDevice> devices;

    /**
     * Private constructor for singleton.
     */
    private RingDeviceRegistry() {
        devices = new ConcurrentHashMap<>();
    }

    /**
     * Return a singleton instance of RingDeviceRegistry.
     */
    public static RingDeviceRegistry getInstance() {
        if (instance == null) {
            synchronized (RingDeviceRegistry.class) {
                if (instance == null) {
                    instance = new RingDeviceRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * Add a new ring device.
     */
    public void addRingDevice(RingDevice ringDevice) throws DuplicateIdException {
        if (ringDevice == null) {
            logger.debug("Ignoring null ringDevice");
        } else {
            if (devices.containsKey(ringDevice.getId())) {
                // logger.trace("Ring device with duplicate id " + ringDevice.getId() + " ignored");
                throw new DuplicateIdException("Ring device with duplicate id " + ringDevice.getId() + " ignored");
            } else {
                ringDevice.setRegistrationStatus(Status.ADDED);
                devices.put(ringDevice.getId(), ringDevice);
            }
        }
    }

    /**
     * Add a new ring device collection.
     */
    public synchronized void addRingDevices(Collection<RingDevice> ringDevices) throws DuplicateIdException {
        for (RingDevice device : ringDevices) {
            addRingDevice(device);
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

    /**
     * Get the device registered with the given id.
     *
     * @param id the device id.
     * @return the RingDevice instance from the registry.
     * @throws DeviceNotFoundException
     */
    public RingDevice getRingDevice(String id) throws DeviceNotFoundException {
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
    public void removeRingDevice(String id) throws DeviceNotFoundException {
        if (devices.containsKey(id)) {
            devices.remove(id);
        } else {
            throw new DeviceNotFoundException("Device with id '" + id + "' not found");
        }
    }

    /**
     * Get a collection with RingDevices with the given status.
     *
     * @param filter the registration status to filter on.
     * @return the (possibly empty) collection.
     */
    public Collection<RingDevice> getRingDevices(Status filterStatus) {
        List<RingDevice> result = new ArrayList<>();
        for (RingDevice device : devices.values()) {
            if (device.getRegistrationStatus().equals(filterStatus)) {
                result.add(device);
            }
        }
        return result;
    }

    /**
     * Set the registration status.
     *
     * @param id the id of the RingDevice.
     * @param status the new registration status.
     */
    public void setStatus(String id, Status status) {
        RingDevice result = devices.get(id);
        if (result != null) {
            result.setRegistrationStatus(status);
        }
    }

    /**
     * The registry status of the device.
     *
     * @author Wim Vissers
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
