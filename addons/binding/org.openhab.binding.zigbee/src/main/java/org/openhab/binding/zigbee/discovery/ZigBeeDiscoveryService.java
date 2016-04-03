/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bubblecloud.zigbee.api.Device;
import org.bubblecloud.zigbee.network.ZigBeeNode;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zigbee.ZigBeeBindingConstants;
import org.openhab.binding.zigbee.handler.ZigBeeCoordinatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZigBeeDiscoveryService} tracks ZigBee devices which are associated
 * to coordinator.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZigBeeDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ZigBeeDiscoveryService.class);

    private final static int SEARCH_TIME = 60;

    private ZigBeeCoordinatorHandler coordinatorHandler;

    public ZigBeeDiscoveryService(ZigBeeCoordinatorHandler coordinatorHandler) {
        super(SEARCH_TIME);
        this.coordinatorHandler = coordinatorHandler;
        logger.debug("Creating ZigBee discovery service for {}", coordinatorHandler.getThing().getUID());
    }

    public void activate() {
        logger.debug("Activating ZigBee discovery service for {}", coordinatorHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivating ZigBee discovery service for {}", coordinatorHandler.getThing().getUID());
    }

    @Override
    public void startScan() {
        logger.debug("Starting ZigBee scan for {}", coordinatorHandler.getThing().getUID());

        // Start the search for new devices
        coordinatorHandler.startDeviceDiscovery();
    }

    public void addThing(ZigBeeNode node, List<Device> devices, String manufacturer, String model) {
        if (manufacturer == null || model == null) {
            return;
        }
        String manufacturerSplitter[] = manufacturer.split(" ");
        String modelSplitter[] = model.split(" ");
        ThingTypeUID thingTypeUID = ZigBeeBindingConstants.THING_TYPE_GENERIC_DEVICE;

        if (thingTypeUID == null) {
            logger.info("Unknown ZigBee device '{}' :: '{}'", manufacturerSplitter[0], modelSplitter[0]);

            return;
        }

        ThingUID bridgeUID = coordinatorHandler.getThing().getUID();
        String thingId = node.getIeeeAddress().toLowerCase().replaceAll("[^a-z0-9_/]", "");
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingId);

        String label = null;
        if (manufacturer != null && model != null) {
            label = manufacturer.toString().trim() + " " + model.toString().trim();
        } else {
            label = "Unknown ZigBee Device " + node.getIeeeAddress();
        }

        logger.info("Creating ZigBee device {} with bridge {}", thingTypeUID, bridgeUID);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(ZigBeeBindingConstants.PARAMETER_MACADDRESS, node.getIeeeAddress());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(label).build();

        thingDiscovered(discoveryResult);
    }
}
