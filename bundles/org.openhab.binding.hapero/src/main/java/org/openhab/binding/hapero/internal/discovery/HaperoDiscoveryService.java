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
package org.openhab.binding.hapero.internal.discovery;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hapero.internal.HaperoBindingConstants;
import org.openhab.binding.hapero.internal.handler.HaperoBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HaperoDiscoveryService} is responsible for discovery of new devices.
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
@Component(service = HaperoDiscoveryService.class, immediate = true, configurationPid = "discovery.hapero")
public class HaperoDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final String DEVICEID = "deviceID";

    private @NonNullByDefault({}) HaperoBridgeHandler bridgeHandler;

    /**
     * Constructor
     */
    public HaperoDiscoveryService() {
        super(Collections.singleton(new ThingTypeUID(HaperoBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS,
                true);
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof HaperoBridgeHandler bh) {
            bridgeHandler = bh;
            bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public void startScan() {
        // Query the current device list from the bridge
        String[] devices = bridgeHandler.getAllDevices();

        // Start a background task to check for new devices
        scheduler.execute(() -> {
            for (String device : devices) {
                deviceDiscovered(device);
            }
        });
    }

    /**
     * Called upon discovery of a new device.
     * Checks if the device is supported by a Handler and creates an appropriate {@link DiscoveryResult}
     *
     * @param deviceId Identifier of the device that has been discovered
     */
    private void deviceDiscovered(String deviceId) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = null;
        String label = "";

        // Check if the device is supported
        if (deviceId.startsWith(HaperoBindingConstants.BUFFER_ID)) {
            thingUID = new ThingUID(HaperoBindingConstants.THING_TYPE_BUFFER, bridgeUID, deviceId);
            label = "Buffer";
        } else if (deviceId.startsWith(HaperoBindingConstants.BOILER_ID)) {
            thingUID = new ThingUID(HaperoBindingConstants.THING_TYPE_BOILER, bridgeUID, deviceId);
            label = "Boiler";
        } else if (deviceId.startsWith(HaperoBindingConstants.HEATING_ID)) {
            thingUID = new ThingUID(HaperoBindingConstants.THING_TYPE_HEATING, bridgeUID, deviceId);
            label = "Heating Circuit";
        } else if (deviceId.startsWith(HaperoBindingConstants.FURNACE_ID)) {
            thingUID = new ThingUID(HaperoBindingConstants.THING_TYPE_FURNACE, bridgeUID, deviceId);
            label = "Furnace";
        }

        // If device is supported, create discovery result and inform openHAB
        if (thingUID != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperty(DEVICEID, deviceId).withRepresentationProperty(DEVICEID).build();
            thingDiscovered(discoveryResult);
        }
    }
}
