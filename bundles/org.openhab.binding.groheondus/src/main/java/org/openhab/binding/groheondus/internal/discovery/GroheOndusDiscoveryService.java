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
package org.openhab.binding.groheondus.internal.discovery;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.BaseAppliance;
import org.grohe.ondus.api.model.Location;
import org.grohe.ondus.api.model.Room;
import org.grohe.ondus.api.model.SenseAppliance;
import org.grohe.ondus.api.model.SenseGuardAppliance;
import org.openhab.binding.groheondus.internal.handler.GroheOndusAccountHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public class GroheOndusDiscoveryService extends AbstractDiscoveryService {
    private static final String PROPERTY_APPLIANCE_ID = "applianceId";

    private static final String PROPERTY_ROOM_ID = "roomId";

    private static final String PROPERTY_LOCATION_ID = "locationId";

    private final Logger logger = LoggerFactory.getLogger(GroheOndusDiscoveryService.class);

    private final GroheOndusAccountHandler bridgeHandler;

    public GroheOndusDiscoveryService(GroheOndusAccountHandler bridgeHandler) {
        super(Collections
                .unmodifiableSet(Stream.of(THING_TYPE_SENSE, THING_TYPE_SENSEGUARD).collect(Collectors.toSet())), 30);
        logger.debug("initialize discovery service");
        this.bridgeHandler = bridgeHandler;
        this.activate(null);
    }

    @Override
    protected void startScan() {
        OndusService service;
        try {
            service = bridgeHandler.getService();
        } catch (IllegalStateException e) {
            logger.debug("No instance of OndusService given.", e);
            return;
        }
        List<BaseAppliance> discoveredAppliances = new ArrayList<>();
        try {
            discoveredAppliances = getApplianceList(service);
        } catch (IOException e) {
            logger.debug("Could not discover appliances.", e);
            return;
        }

        discoveredAppliances.forEach(appliance -> {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingUID thingUID = null;
            switch (appliance.getType()) {
                case SenseGuardAppliance.TYPE:
                    thingUID = new ThingUID(THING_TYPE_SENSEGUARD, appliance.getApplianceId());
                    break;
                case SenseAppliance.TYPE:
                    thingUID = new ThingUID(THING_TYPE_SENSE, appliance.getApplianceId());
                    break;
                default:
                    return;
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_LOCATION_ID, appliance.getRoom().getLocation().getId());
            properties.put(PROPERTY_ROOM_ID, appliance.getRoom().getId());
            properties.put(PROPERTY_APPLIANCE_ID, appliance.getApplianceId());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(appliance.getName())
                    .withRepresentationProperty(PROPERTY_APPLIANCE_ID).build();

            thingDiscovered(discoveryResult);
        });
    }

    private List<BaseAppliance> getApplianceList(OndusService service) throws IOException {
        List<BaseAppliance> discoveredAppliances = new ArrayList<>();

        for (Location location : service.getLocations()) {
            for (Room room : service.getRooms(location)) {
                discoveredAppliances.addAll(service.getAppliances(room));
            }
        }

        return discoveredAppliances;
    }
}
