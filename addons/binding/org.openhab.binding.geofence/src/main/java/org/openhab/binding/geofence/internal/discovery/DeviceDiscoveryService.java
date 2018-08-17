/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.discovery;


import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.geofence.internal.BindingConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Device discovery service.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DeviceDiscoveryService extends AbstractDiscoveryService {
    /**
     * Discovery timeout
     */
    private static final int TIMEOUT = 5;

    /**
     * Registered trackers
     */
    private Map<String, String> registeredTrackers = new HashMap<>();

    /**
     * Constructor.
     *
     * @throws IllegalArgumentException thrown by the super constructor
     */
    public DeviceDiscoveryService() throws IllegalArgumentException {
        super(BindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
    }

    protected void startScan() {
        this.discoverTracker();
    }

    protected void startBackgroundDiscovery() {
        this.discoverTracker();
    }

    /**
     * Called when the source device s not registered as a thing. These undiscovered devices will be registered by
     * the discovery service.
     *
     * @param deviceId Device id.
     */
    public void addDevice(String deviceId) {
        this.registeredTrackers.put(deviceId, getDeviceLabel(deviceId));
    }

    /**
     * Clear the device registration after device handler was created.
     * @param deviceId Device id.
     */
    public void removeTracker(String deviceId) {
        this.registeredTrackers.remove(deviceId);
    }

    /**
     * Creates discovery result from a tracker device.
     */
    private void discoverTracker() {
        for (Map.Entry<String, String> entry : this.registeredTrackers.entrySet()) {
            ThingUID id = new ThingUID(BindingConstants.THING_TYPE_DEVICE, entry.getKey());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(id).withLabel(entry.getValue()).build();
            this.thingDiscovered(discoveryResult);
        }
    }

    private String getDeviceLabel(String deviceId) {
        return String.format("GPS tracker: %s", deviceId);
    }
}
