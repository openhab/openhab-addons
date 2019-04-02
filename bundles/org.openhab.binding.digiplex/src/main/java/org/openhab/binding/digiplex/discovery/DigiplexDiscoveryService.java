/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.digiplex.DigiplexBindingConstants;
import org.openhab.binding.digiplex.communication.AreaLabelRequest;
import org.openhab.binding.digiplex.communication.AreaLabelResponse;
import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.communication.DigiplexRequest;
import org.openhab.binding.digiplex.communication.ZoneLabelRequest;
import org.openhab.binding.digiplex.communication.ZoneLabelResponse;
import org.openhab.binding.digiplex.handler.DigiplexBridgeHandler;
import org.osgi.service.component.annotations.Component;

/**
 * Service for discovering things on Digiplex alarm systems
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = false, configurationPid = "discovery.digiplex")
public class DigiplexDiscoveryService extends AbstractDiscoveryService
        implements ThingHandlerService, DigiplexMessageHandler {

    private static final int MAX_ZONE = 96;
    private static final int MAX_AREA = 8;

    private static final int DISCOVERY_TIMEOUT = 30;

    @Nullable
    DigiplexBridgeHandler bridgeHandler;

    public DigiplexDiscoveryService() {
        super(Collections.singleton(DigiplexBindingConstants.THING_TYPE_ZONE), DISCOVERY_TIMEOUT, false);
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
        ThingUID thingUID = new ThingUID(DigiplexBindingConstants.THING_TYPE_ZONE, bridgeUID,
                String.format("zone%d", response.zoneNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(DigiplexBindingConstants.PROPERTY_ZONE_NO, Integer.toString(response.zoneNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.zoneName).build();

        thingDiscovered(discoveryResult);
    }

    private boolean isDefaultName(ZoneLabelResponse response) {
        return DigiplexBindingConstants.ZONE_DEFAULT_NAMES.stream().anyMatch(format -> {
            if (String.format(format, response.zoneNo).equals(response.zoneName)) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    @SuppressWarnings("null")
    public void handleAreaLabelResponse(@NonNull AreaLabelResponse response) {
        // we have no other option to check whether area is actually enabled than to compare its name with the default
        if (response.success && response.areaName
                .equals(String.format(DigiplexBindingConstants.AREA_DEFAULT_NAME, response.areaNo))) {
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(DigiplexBindingConstants.THING_TYPE_AREA, bridgeUID,
                String.format("area%d", response.areaNo));

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(DigiplexBindingConstants.PROPERTY_AREA_NO, Integer.toString(response.areaNo));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(response.areaName).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        bridgeHandler = (DigiplexBridgeHandler) handler;
        if (bridgeHandler != null) {
            bridgeHandler.registerMessageHandler(this);
        }
    }

    @Override
    @Nullable
    public ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

}
