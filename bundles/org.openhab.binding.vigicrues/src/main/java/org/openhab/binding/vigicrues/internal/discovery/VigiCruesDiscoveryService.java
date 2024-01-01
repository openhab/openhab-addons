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
package org.openhab.binding.vigicrues.internal.discovery;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vigicrues.internal.StationConfiguration;
import org.openhab.binding.vigicrues.internal.api.ApiHandler;
import org.openhab.binding.vigicrues.internal.api.VigiCruesException;
import org.openhab.binding.vigicrues.internal.dto.hubeau.HubEauResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VigiCruesDiscoveryService} searches for available
 * hydro stations discoverable through API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.vigicrues")
@NonNullByDefault
public class VigiCruesDiscoveryService extends AbstractDiscoveryService {
    private static final int SEARCH_TIME = 5;

    private final Logger logger = LoggerFactory.getLogger(VigiCruesDiscoveryService.class);
    private final LocationProvider locationProvider;
    private final ApiHandler apiHandler;

    private int searchRange = 10;

    @Activate
    public VigiCruesDiscoveryService(@Reference ApiHandler apiHandler, @Reference LocationProvider locationProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.apiHandler = apiHandler;
        this.locationProvider = locationProvider;
    }

    @Override
    public void startScan() {
        PointType location = locationProvider.getLocation();
        if (location != null) {
            try {
                HubEauResponse response = apiHandler.discoverStations(location, searchRange);
                if (response.count > 0) {
                    response.stations.stream().filter(station -> station.enService).forEach(station -> {
                        thingDiscovered(DiscoveryResultBuilder
                                .create(new ThingUID(THING_TYPE_STATION, station.codeStation))
                                .withLabel(station.libelleStation).withRepresentationProperty(StationConfiguration.ID)
                                .withProperty(StationConfiguration.ID, station.codeStation).build());
                    });
                } else {
                    logger.info("No station exists in a neighbourhood of {} km", searchRange);
                }
            } catch (VigiCruesException e) {
                logger.warn("Error discovering nearby hydro stations : {}", e.getMessage());
            }
            searchRange += 10;
        }
        stopScan();
    }
}
