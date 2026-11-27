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
package org.openhab.binding.transit.internal.discovery;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.transit.internal.handler.TransitBridgeHandler;
import org.openhab.binding.transit.internal.net.dto.StopsResult;
import org.openhab.binding.transit.internal.net.dto.StopsResult.Stop;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TransitStopDiscoveryService} discovers the stops near the location configured in openHAB.
 * <p>
 * Each scan costs one API request, so there is no background discovery; a scan is only started manually.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = TransitStopDiscoveryService.class)
public class TransitStopDiscoveryService extends AbstractThingHandlerDiscoveryService<TransitBridgeHandler> {
    private static final int DISCOVERY_TIMEOUT_SECONDS = API_TIMEOUT_SECONDS + 5;

    private final Logger logger = LoggerFactory.getLogger(TransitStopDiscoveryService.class);
    private final LocationProvider locationProvider;

    @Activate
    public TransitStopDiscoveryService(@Reference LocationProvider locationProvider) {
        super(TransitBridgeHandler.class, Set.of(THING_TYPE_STOP), DISCOVERY_TIMEOUT_SECONDS, false);
        this.locationProvider = locationProvider;
    }

    @Override
    protected void startScan() {
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("No location configured in openHAB, cannot discover nearby stops");
            return;
        }
        if (thingHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge {} is not ONLINE, cannot discover nearby stops", thingHandler.getThing().getUID());
            return;
        }

        try {
            StopsResult result = thingHandler.getNearbyStops(location.getLatitude().doubleValue(),
                    location.getLongitude().doubleValue());
            ThingUID bridgeUID = thingHandler.getThing().getUID();
            for (Stop stop : result.getStops()) {
                String globalStopId = stop.getGlobalStopId();
                String stopName = stop.getStopName();
                if (globalStopId == null || globalStopId.isBlank() || stopName == null) {
                    continue;
                }
                String city = stop.getCityName();
                String label = city != null && !city.isBlank() ? stopName + " (" + city + ")" : stopName;
                ThingUID thingUID = new ThingUID(THING_TYPE_STOP, bridgeUID, toThingId(globalStopId));
                thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                        .withProperty(CONFIG_GLOBAL_STOP_ID, globalStopId)
                        .withRepresentationProperty(CONFIG_GLOBAL_STOP_ID).build());
            }
        } catch (IOException e) {
            logger.debug("Failed to discover nearby stops: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Converts a global stop ID (e.g. {@code VVSDE:2394}) to a valid Thing ID (e.g. {@code VVSDE_2394}).
     */
    static String toThingId(String globalStopId) {
        return globalStopId.replaceAll("[^\\w-]", "_");
    }
}
