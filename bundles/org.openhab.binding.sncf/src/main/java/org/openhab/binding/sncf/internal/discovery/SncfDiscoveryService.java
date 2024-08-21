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
package org.openhab.binding.sncf.internal.discovery;

import static org.openhab.binding.sncf.internal.SncfBindingConstants.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sncf.internal.SncfException;
import org.openhab.binding.sncf.internal.dto.PlaceNearby;
import org.openhab.binding.sncf.internal.handler.SncfBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SncfDiscoveryService} searches for available
 * station discoverable through API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SncfDiscoveryService.class)
@NonNullByDefault
public class SncfDiscoveryService extends AbstractThingHandlerDiscoveryService<SncfBridgeHandler> {
    private static final int SEARCH_TIME = 7;

    private final Logger logger = LoggerFactory.getLogger(SncfDiscoveryService.class);

    private @NonNullByDefault({}) LocationProvider locationProvider;

    private int searchRange = 1500;

    @Activate
    public SncfDiscoveryService() {
        super(SncfBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
    }

    @Reference(unbind = "-")
    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    public void startScan() {
        PointType location = locationProvider.getLocation();
        if (location != null) {
            ThingUID bridgeUID = thingHandler.getThing().getUID();
            searchRange += 500;
            try {
                List<PlaceNearby> places = thingHandler.discoverNearby(location, searchRange);
                if (places != null && !places.isEmpty()) {
                    places.forEach(place -> {
                        // stop_point:SNCF:87386573:Bus
                        List<String> idElts = new LinkedList<>(Arrays.asList(place.id.split(":")));
                        idElts.remove(0);
                        idElts.remove(0);
                        thingDiscovered(DiscoveryResultBuilder
                                .create(new ThingUID(STATION_THING_TYPE, bridgeUID, String.join("_", idElts)))
                                .withLabel(
                                        String.format("%s (%s)", place.stopPoint.name, idElts.get(1)).replace("-", "_"))
                                .withBridge(bridgeUID).withRepresentationProperty(STOP_POINT_ID)
                                .withProperty(STOP_POINT_ID, place.id).build());
                    });
                } else {
                    logger.info("No station found in a perimeter of {} m, extending search", searchRange);
                    startScan();
                }
            } catch (SncfException e) {
                logger.warn("Error calling SNCF Api : {}", e.getMessage());
            }
        } else {
            logger.info("Please set a system location to enable station discovery");
        }
    }
}
