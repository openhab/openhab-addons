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
package org.openhab.binding.synopanalyzer.internal.discovery;

import static org.openhab.binding.synopanalyzer.internal.SynopAnalyzerBindingConstants.THING_SYNOP;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.synopanalyzer.internal.config.SynopAnalyzerConfiguration;
import org.openhab.binding.synopanalyzer.internal.stationdb.Station;
import org.openhab.binding.synopanalyzer.internal.stationdb.StationDbService;
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
 * The {@link SynopAnalyzerDiscoveryService} discovers synop stations based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(service = DiscoveryService.class)
@NonNullByDefault
public class SynopAnalyzerDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(SynopAnalyzerDiscoveryService.class);
    private final LocationProvider locationProvider;
    private final List<Station> stations;
    private double radius = 0;

    @Activate
    public SynopAnalyzerDiscoveryService(@Reference StationDbService dBService,
            @Reference LocationProvider locationProvider) {
        super(Set.of(THING_SYNOP), DISCOVER_TIMEOUT_SECONDS);
        this.locationProvider = locationProvider;
        this.stations = dBService.getStations();
    }

    @Override
    public void startScan() {
        logger.debug("Starting Synop Analyzer discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    public void createResults(PointType serverLocation) {
        Map<Double, Station> distances = new TreeMap<>();

        stations.forEach(station -> {
            PointType stationLocation = new PointType(station.getLocation());
            double distance = serverLocation.distanceFrom(stationLocation).doubleValue();
            if (distance > radius) {
                distances.put(distance, station);
            }
        });

        Iterator<Entry<Double, Station>> stationIterator = distances.entrySet().iterator();
        if (stationIterator.hasNext()) {
            Entry<Double, Station> nearest = stationIterator.next();
            Station station = nearest.getValue();
            radius = nearest.getKey();

            thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_SYNOP, Integer.toString(station.idOmm)))
                    .withLabel(String.format("Synop : %s", station.usualName))
                    .withProperty(SynopAnalyzerConfiguration.STATION_ID, station.idOmm)
                    .withRepresentationProperty(SynopAnalyzerConfiguration.STATION_ID).build());
        } else {
            logger.info("No Synop station available at a radius higher than {} m - resetting to 0 m", radius);
            radius = 0;
        }
    }
}
