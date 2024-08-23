/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Actions;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles event dispatching to {@link DeviceStateListener}s.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceStateDispatcher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<DeviceStateListener> listeners = new CopyOnWriteArrayList<>();
    private Set<String> previousDeviceIdentifiers = new HashSet<>();
    private final DeviceCache cache = new DeviceCache();

    /**
     * Adds a listener. The listener will be immediately invoked with the current status of all known devices.
     *
     * @param listener The listener to add.
     */
    public void addListener(DeviceStateListener listener) {
        if (listeners.contains(listener)) {
            logger.warn("Listener '{}' was registered multiple times.", listener);
        }
        listeners.add(listener);

        cache.getDeviceIds().forEach(deviceIdentifier -> cache.getDevice(deviceIdentifier)
                .ifPresent(device -> listener.onDeviceStateUpdated(new DeviceState(deviceIdentifier, device))));
    }

    /**
     * Removes a listener.
     */
    public void removeListener(DeviceStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears the internal device state cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Dispatches device status updates to all registered {@link DeviceStateListener}. This includes device removal.
     *
     * @param devices {@link DeviceCollection} which contains the state information to dispatch.
     */
    public void dispatchDeviceStateUpdates(DeviceCollection devices) {
        cache.replaceAllDevices(devices);
        dispatchDevicesRemoved(devices);
        cache.getDeviceIds().forEach(this::dispatchDeviceState);
    }

    /**
     * Dispatches the cached state of the device identified by the given device identifier.
     */
    public void dispatchDeviceState(String deviceIdentifier) {
        cache.getDevice(deviceIdentifier).ifPresent(device -> listeners
                .forEach(listener -> listener.onDeviceStateUpdated(new DeviceState(deviceIdentifier, device))));
    }

    /**
     * Dispatches device action updates to all registered {@link DeviceStateListener}.
     *
     * @param deviceId ID of the device to dispatch the {@link Actions} for.
     * @param actions {@link Actions} to dispatch.
     */
    public void dispatchActionStateUpdates(String deviceId, Actions actions) {
        listeners.forEach(listener -> listener.onProcessActionUpdated(new ActionsState(deviceId, actions)));
    }

    private void dispatchDevicesRemoved(DeviceCollection devices) {
        Set<String> presentDeviceIdentifiers = devices.getDeviceIdentifiers();
        Set<String> removedDeviceIdentifiers = previousDeviceIdentifiers;
        removedDeviceIdentifiers.removeAll(presentDeviceIdentifiers);

        previousDeviceIdentifiers = devices.getDeviceIdentifiers();

        removedDeviceIdentifiers
                .forEach(deviceIdentifier -> listeners.forEach(listener -> listener.onDeviceRemoved(deviceIdentifier)));
    }
}
