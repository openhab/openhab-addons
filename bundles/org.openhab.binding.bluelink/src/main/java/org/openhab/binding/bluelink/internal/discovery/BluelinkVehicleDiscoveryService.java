/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.discovery;

import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.handler.BluelinkAccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Bluelink vehicles.
 *
 * @author Marcus Better - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = BluelinkVehicleDiscoveryService.class)
@NonNullByDefault
public class BluelinkVehicleDiscoveryService extends AbstractThingHandlerDiscoveryService<BluelinkAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(BluelinkVehicleDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;

    public BluelinkVehicleDiscoveryService() {
        super(BluelinkAccountHandler.class, Set.of(THING_TYPE_VEHICLE), DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Bluelink vehicle discovery");
        final BluelinkAccountHandler handler = thingHandler;
        final List<VehicleInfo> vehicles;
        try {
            vehicles = handler.getVehicles();
            logger.debug("Found {} vehicles", vehicles.size());
            vehicles.forEach(v -> discoverVehicle(v, handler));
        } catch (final BluelinkApiException e) {
            logger.debug("discovery failed: {}", e.getMessage());
        }
    }

    private void discoverVehicle(final VehicleInfo vehicle, final BluelinkAccountHandler handler) {
        if (vehicle.vin() == null || vehicle.vin().isBlank()) {
            logger.debug("Skipping vehicle with no VIN");
            return;
        }

        final ThingUID bridgeUID = handler.getThing().getUID();
        final ThingUID thingUID = new ThingUID(THING_TYPE_VEHICLE, bridgeUID, vehicle.vin());

        String label = vehicle.getDisplayName();
        if (label == null) {
            label = "Bluelink Vehicle " + vehicle.vin();
        }

        final String modelCode = vehicle.modelCode();
        final String evStatus = vehicle.evStatus();
        final Map<String, Object> properties = Map.of(PROPERTY_VIN, vehicle.vin(), PROPERTY_MODEL,
                modelCode != null ? modelCode : "", PROPERTY_ENGINE_TYPE, evStatus != null ? evStatus : "");

        final DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(PROPERTY_VIN).build();

        thingDiscovered(result);
        logger.debug("Discovered vehicle: {} ({})", label, vehicle.vin());
    }
}
