/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.discovery;

import static org.openhab.binding.innogysmarthome.InnogyBindingConstants.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.openhab.binding.innogysmarthome.handler.InnogyBridgeHandler;
import org.openhab.binding.innogysmarthome.handler.InnogyDeviceHandler;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.listener.DeviceStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyDeviceDiscoveryService} is responsible for discovering new devices.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class InnogyDeviceDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(InnogyDeviceDiscoveryService.class);
    private InnogyBridgeHandler bridgeHandler;

    /**
     * Construct an {@link InnogyDeviceDiscoveryService} with the given {@link BridgeHandler}.
     *
     * @param bridgeHandler
     */
    public InnogyDeviceDiscoveryService(InnogyBridgeHandler bridgeHandler) {
        super(SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Deactivates the {@link InnogyDeviceDiscoveryService} by unregistering it as {@link DeviceStatusListener} on the
     * {@link InnogyBridgeHandler}. Older discovery results will be removed.
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#deactivate()
     */
    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return InnogyDeviceHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.debug("SCAN for new innogy devices started...");

        Collection<Device> devices = bridgeHandler.loadDevices();
        if (devices != null) {
            for (Device d : devices) {
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
        ThingUID thingUID = getThingUID(device);
        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        if (thingUID != null && thingTypeUID != null) {

            ThingUID bridgeUID = bridgeHandler.getThing().getUID();

            String name = device.getName();
            if (name.isEmpty()) {
                name = device.getSerialnumber();
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_ID, device.getId());

            String label;
            if (device.hasLocation()) {
                label = device.getType() + ": " + name + " (" + device.getLocation().getName() + ")";
            } else {
                label = device.getType() + ": " + name;
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(label).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered unsupported device of type '{}' and name '{}' with id {}", device.getType(),
                    device.getName(), device.getId());
        }
    }

    /**
     * Returns the {@link ThingUID} for the given {@link Device} or null, if the device type is not available.
     *
     * @param device
     * @return
     */
    private ThingUID getThingUID(Device device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(device);

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
    private ThingTypeUID getThingTypeUID(Device device) {
        String thingTypeId = device.getType();
        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }
}
