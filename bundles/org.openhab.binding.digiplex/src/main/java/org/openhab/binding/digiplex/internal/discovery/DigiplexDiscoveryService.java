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
package org.openhab.binding.digiplex.internal.discovery;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digiplex.internal.communication.AreaLabelRequest;
import org.openhab.binding.digiplex.internal.communication.AreaLabelResponse;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneLabelRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneLabelResponse;
import org.openhab.binding.digiplex.internal.handler.DigiplexBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service for discovering things on Digiplex alarm systems
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DigiplexDiscoveryService.class)
@NonNullByDefault
public class DigiplexDiscoveryService extends AbstractThingHandlerDiscoveryService<DigiplexBridgeHandler>
        implements DigiplexMessageHandler {

    private static final int MAX_ZONE = 96;
    private static final int MAX_AREA = 8;

    private static final int DISCOVERY_TIMEOUT = 30;

    public DigiplexDiscoveryService() {
        super(DigiplexBridgeHandler.class, Set.of(THING_TYPE_ZONE), DISCOVERY_TIMEOUT, false);
    }

    @Override
    @SuppressWarnings("null")
    protected void startScan() {
        thingHandler.registerMessageHandler(this);
        // find zones
        for (int i = 1; i <= MAX_ZONE; i++) {
            DigiplexRequest command = new ZoneLabelRequest(i);
            thingHandler.sendRequest(command);
        }
        // find areas
        for (int i = 1; i <= MAX_AREA; i++) {
            DigiplexRequest command = new AreaLabelRequest(i);
            thingHandler.sendRequest(command);
        }
    }

    @Override
    @SuppressWarnings("null")
    protected synchronized void stopScan() {
        thingHandler.unregisterMessageHandler(this);
        super.stopScan();
    }

    @Override
    @SuppressWarnings("null")
    public void handleZoneLabelResponse(ZoneLabelResponse response) {
        // we have no other option to check whether zone is actually enabled than to compare its name with the default
        if (isDefaultName(response)) {
            return;
        }
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, String.format("zone%d", response.zoneNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PROPERTY_ZONE_NO, Integer.toString(response.zoneNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.zoneName).build();

        thingDiscovered(discoveryResult);
    }

    private boolean isDefaultName(ZoneLabelResponse response) {
        return ZONE_DEFAULT_NAMES.stream()
                .anyMatch(format -> String.format(format, response.zoneNo).equals(response.zoneName));
    }

    @Override
    @SuppressWarnings("null")
    public void handleAreaLabelResponse(AreaLabelResponse response) {
        // we have no other option to check whether area is actually enabled than to compare its name with the default
        if (response.success && response.areaName.equals(String.format(AREA_DEFAULT_NAME, response.areaNo))) {
            return;
        }

        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_AREA, bridgeUID, String.format("area%d", response.areaNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PROPERTY_AREA_NO, Integer.toString(response.areaNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.areaName).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void initialize() {
        thingHandler.registerMessageHandler(this);
        super.initialize();
    }
}
