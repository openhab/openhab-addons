/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.api.RingDevicesTO;
import org.openhab.binding.ring.internal.device.Chime;
import org.openhab.binding.ring.internal.device.Doorbell;
import org.openhab.binding.ring.internal.device.OtherDevice;
import org.openhab.binding.ring.internal.device.RingDevice;
import org.openhab.binding.ring.internal.device.Stickupcam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton registry of found devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class RingDeviceRegistry {
    private final Logger logger = LoggerFactory.getLogger(RingDeviceRegistry.class);

    /**
     * Key: device id. Value: the RingDevice implementation object.
     */
    private final Map<String, RingDevice> devices = new ConcurrentHashMap<>();

    private void addOrUpdateRingDevice(RingDeviceTO deviceTO, Function<RingDeviceTO, RingDevice> creator) {
        devices.compute(deviceTO.id, (id, existing) -> {
            if (existing == null) {
                return creator.apply(deviceTO);
            } else {
                logger.debug(
                        "RingDeviceRegistry - addRingDevices - Ring device with duplicate id {} ignored.  Updating Json.",
                        deviceTO.id);
                existing.setDeviceStatus(deviceTO);
                return existing;
            }
        });
    }

    /**
     * Add a new ring device collection.
     */
    public synchronized void addOrUpdateRingDevices(RingDevicesTO ringDevices) {
        ringDevices.doorbells.forEach(deviceTO -> addOrUpdateRingDevice(deviceTO, Doorbell::new));
        ringDevices.chimes.forEach(deviceTO -> addOrUpdateRingDevice(deviceTO, Chime::new));
        ringDevices.stickupCams.forEach(deviceTO -> addOrUpdateRingDevice(deviceTO, Stickupcam::new));
        ringDevices.other.forEach(deviceTO -> addOrUpdateRingDevice(deviceTO, OtherDevice::new));
    }

    /**
     * Get the device registered with the given id.
     *
     * @param id the device id.
     * @return the RingDevice instance from the registry.
     */
    public @Nullable RingDevice getRingDevice(String id) {
        return devices.get(id);
    }

    /**
     * Get a collection of all {@link RingDevice}s
     *
     * @return the (possibly empty) collection.
     */
    public Collection<RingDevice> getRingDevices() {
        return devices.values();
    }
}
