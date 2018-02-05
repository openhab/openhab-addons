/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.digiplex.DigiplexBindingConstants;
import org.openhab.binding.digiplex.communication.AreaLabelRequest;
import org.openhab.binding.digiplex.communication.AreaLabelResponse;
import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.communication.DigiplexRequest;
import org.openhab.binding.digiplex.communication.ZoneLabelRequest;
import org.openhab.binding.digiplex.communication.ZoneLabelResponse;
import org.openhab.binding.digiplex.handler.DigiplexBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for discovering things on Digiplex alarm systems
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class DigiplexDiscoveryService extends AbstractDiscoveryService
        implements DigiplexMessageHandler, ExtendedDiscoveryService {

    private static final int MAX_ZONE = 96;
    private static final int MAX_AREA = 8;

    private final Logger logger = LoggerFactory.getLogger(DigiplexDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT = 30;

    @Nullable
    private DiscoveryServiceCallback discoveryServiceCallback;

    DigiplexBridgeHandler bridgeHandler;

    public DigiplexDiscoveryService(DigiplexBridgeHandler bridgeHandler) {
        super(Collections.singleton(DigiplexBindingConstants.THING_TYPE_ZONE), DISCOVERY_TIMEOUT, false);
        this.bridgeHandler = bridgeHandler;
        bridgeHandler.registerMessageHandler(this);
    }

    @Override
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
    protected synchronized void stopScan() {
        bridgeHandler.unregisterMessageHandler(this);
        super.stopScan();
    }

    @Override
    public void handleZoneLabelResponse(ZoneLabelResponse response) {
        // we have no other option to check whether zone is actually enabled than to compare its name with the default
        if (isDefaultName(response)) {
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(DigiplexBindingConstants.THING_TYPE_ZONE, bridgeUID,
                String.format("zone%d", response.getZoneNo()));

        if (discoveryServiceCallback != null && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
            return;
        }

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(DigiplexBindingConstants.PROPERTY_ZONE_NO, Integer.toString(response.getZoneNo()));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.getZoneName()).build();

        thingDiscovered(discoveryResult);
    }

    private boolean isDefaultName(ZoneLabelResponse response) {
        return DigiplexBindingConstants.ZONE_DEFAULT_NAMES.stream().anyMatch(format -> {
            if (String.format(format, response.getZoneNo()).equals(response.getZoneName())) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public void handleAreaLabelResponse(@NonNull AreaLabelResponse response) {
        // we have no other option to check whether area is actually enabled than to compare its name with the default
        if (response.isSuccess() && response.getAreaName()
                .equals(String.format(DigiplexBindingConstants.AREA_DEFAULT_NAME, response.getAreaNo()))) {
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(DigiplexBindingConstants.THING_TYPE_AREA, bridgeUID,
                String.format("area%d", response.getAreaNo()));

        if (discoveryServiceCallback != null && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
            return;
        }

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(DigiplexBindingConstants.PROPERTY_AREA_NO, Integer.toString(response.getAreaNo()));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.getAreaName()).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void setDiscoveryServiceCallback(@NonNull DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

}
