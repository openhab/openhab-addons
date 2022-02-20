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
package org.openhab.binding.livisismarthome.internal.discovery;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livisismarthome.internal.LivisiBindingConstants;
import org.openhab.binding.livisismarthome.internal.client.entity.device.Device;
import org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeHandler;
import org.openhab.binding.livisismarthome.internal.handler.LivisiDeviceHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LivisiDeviceDiscoveryService} is responsible for discovering new devices.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
public class LivisiDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final int SEARCH_TIME_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(LivisiDeviceDiscoveryService.class);

    private @Nullable LivisiBridgeHandler bridgeHandler;

    /**
     * Construct an {@link LivisiDeviceDiscoveryService}.
     */
    public LivisiDeviceDiscoveryService() {
        super(SEARCH_TIME_SECONDS);
    }

    /**
     * Deactivates the {@link LivisiDeviceDiscoveryService} by unregistering it as
     * {@link org.openhab.binding.livisismarthome.internal.listener.DeviceStatusListener} on the
     * {@link LivisiBridgeHandler}. Older discovery results will be removed.
     *
     * @see org.openhab.core.config.discovery.AbstractDiscoveryService#deactivate()
     */
    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return LivisiDeviceHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.debug("SCAN for new LIVISI SmartHome devices started...");
        if (bridgeHandler != null) {
            for (final Device d : bridgeHandler.loadDevices()) {
                onDeviceAdded(d);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void onDeviceAdded(Device device) {
        if (bridgeHandler == null) {
            return;
        }
        final ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(bridgeUID, device);
        final ThingTypeUID thingTypeUID = getThingTypeUID(device);

        if (thingUID != null && thingTypeUID != null) {
            String name = device.getConfig().getName();
            if (name.isEmpty()) {
                name = device.getSerialnumber();
            }

            final Map<String, Object> properties = new HashMap<>();
            properties.put(LivisiBindingConstants.PROPERTY_ID, device.getId());

            final String label;
            if (device.hasLocation()) {
                label = device.getType() + ": " + name + " (" + device.getLocation().getName() + ")";
            } else {
                label = device.getType() + ": " + name;
            }

            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(label).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered unsupported device of type '{}' and name '{}' with id {}", device.getType(),
                    device.getConfig().getName(), device.getId());
        }
    }

    /**
     * Returns the {@link ThingUID} for the given {@link Device} or null, if the device type is not available.
     *
     * @param device
     * @return
     */
    private @Nullable ThingUID getThingUID(ThingUID bridgeUID, Device device) {
        final ThingTypeUID thingTypeUID = getThingTypeUID(device);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, device.getId());
        }
        return null;
    }

    /**
     * Returns a {@link ThingTypeUID} for the given {@link Device} or null, if the device type is not available.
     *
     * @param device
     * @return
     */
    private @Nullable ThingTypeUID getThingTypeUID(Device device) {
        final String thingTypeId = device.getType();
        return thingTypeId != null ? new ThingTypeUID(LivisiBindingConstants.BINDING_ID, thingTypeId) : null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof LivisiBridgeHandler) {
            bridgeHandler = (LivisiBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
