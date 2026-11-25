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
package org.openhab.binding.groheondus.internal.discovery;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.THING_TYPE_SENSE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.THING_TYPE_SENSEGUARD;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.groheondus.internal.api.ApplianceInfo;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.handler.GroheOndusAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 * @author Arne Seime - Use own API client
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
        // Remove old results - or they will stay there forever
        removeOlderResults(getTimestampOfLastScan(), null, bridgeHandler.getThing().getUID());

        GroheOndusApi api = bridgeHandler.getApi();
        if (api == null) {
            logger.debug("Account {} is not online, skipping discovery", bridgeHandler.getThing().getUID());
            return;
        }
        List<ApplianceInfo> discoveredAppliances;
        try {
            discoveredAppliances = api.getAppliances();
        } catch (GroheOndusApiException e) {
            logger.debug("Could not discover appliances: {}", e.getMessage());
            return;
        }

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        discoveredAppliances.forEach(appliance -> {
            String applianceId = appliance.ref().applianceId();
            ThingUID thingUID = switch (appliance.type()) {
                case ApplianceInfo.TYPE_SENSE_GUARD -> new ThingUID(THING_TYPE_SENSEGUARD, bridgeUID, applianceId);
                case ApplianceInfo.TYPE_SENSE -> new ThingUID(THING_TYPE_SENSE, bridgeUID, applianceId);
                default -> null;
            };
            if (thingUID == null) {
                logger.debug("Ignoring unsupported appliance type {} ({})", appliance.type(), appliance.name());
                return;
            }

            Map<String, Object> properties = new HashMap<>(appliance.properties());
            properties.put(PROPERTY_LOCATION_ID, appliance.ref().locationId());
            properties.put(PROPERTY_ROOM_ID, appliance.ref().roomId());
            properties.put(PROPERTY_APPLIANCE_ID, applianceId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(appliance.name()).withRepresentationProperty(PROPERTY_APPLIANCE_ID)
                    .build();

            thingDiscovered(discoveryResult);
        });
    }
}
