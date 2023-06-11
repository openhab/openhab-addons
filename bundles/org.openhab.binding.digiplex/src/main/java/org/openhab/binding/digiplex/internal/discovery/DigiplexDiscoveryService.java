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
package org.openhab.binding.digiplex.internal.discovery;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.digiplex.internal.communication.AreaLabelRequest;
import org.openhab.binding.digiplex.internal.communication.AreaLabelResponse;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneLabelRequest;
import org.openhab.binding.digiplex.internal.communication.ZoneLabelResponse;
import org.openhab.binding.digiplex.internal.handler.DigiplexBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Service for discovering things on Digiplex alarm systems
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class DigiplexDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService, DigiplexMessageHandler {

    private static final int MAX_ZONE = 96;
    private static final int MAX_AREA = 8;

    private static final int DISCOVERY_TIMEOUT = 30;

    private @Nullable DigiplexBridgeHandler bridgeHandler;

    public DigiplexDiscoveryService() {
        super(Collections.singleton(THING_TYPE_ZONE), DISCOVERY_TIMEOUT, false);
    }

    @Override
    @SuppressWarnings("null")
    protected void startScan() {
        bridgeHandler.registerMessageHandler(this);
        // find zones
        for (int i = 1; i <= MAX_ZONE; i++) {
            DigiplexRequest command = new ZoneLabelRequest(i);
            bridgeHandler.sendRequest(command);
        }
        // find areas
        for (int i = 1; i <= MAX_AREA; i++) {
            DigiplexRequest command = new AreaLabelRequest(i);
            bridgeHandler.sendRequest(command);
        }
    }

    @Override
    @SuppressWarnings("null")
    protected synchronized void stopScan() {
        bridgeHandler.unregisterMessageHandler(this);
        super.stopScan();
    }

    @Override
    @SuppressWarnings("null")
    public void handleZoneLabelResponse(ZoneLabelResponse response) {
        // we have no other option to check whether zone is actually enabled than to compare its name with the default
        if (isDefaultName(response)) {
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, bridgeUID, String.format("zone%d", response.zoneNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PROPERTY_ZONE_NO, Integer.toString(response.zoneNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.zoneName).build();

        thingDiscovered(discoveryResult);
    }

    private boolean isDefaultName(ZoneLabelResponse response) {
        return ZONE_DEFAULT_NAMES.stream().anyMatch(format -> {
            if (String.format(format, response.zoneNo).equals(response.zoneName)) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    @SuppressWarnings("null")
    public void handleAreaLabelResponse(AreaLabelResponse response) {
        // we have no other option to check whether area is actually enabled than to compare its name with the default
        if (response.success && response.areaName.equals(String.format(AREA_DEFAULT_NAME, response.areaNo))) {
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_AREA, bridgeUID, String.format("area%d", response.areaNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(PROPERTY_AREA_NO, Integer.toString(response.areaNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.areaName).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DigiplexBridgeHandler) {
            bridgeHandler = (DigiplexBridgeHandler) handler;
            bridgeHandler.registerMessageHandler(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
