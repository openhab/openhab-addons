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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.synopanalyzer.internal.config.SynopAnalyzerConfiguration;
import org.openhab.binding.synopanalyzer.internal.synop.StationDB;
import org.openhab.binding.synopanalyzer.internal.synop.StationDB.Station;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SynopAnalyzerDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class SynopAnalyzerDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(SynopAnalyzerDiscoveryService.class);
    private final Map<Integer, Double> distances = new HashMap<>();
    private final LocationProvider locationProvider;
    private final StationDB stationDB;

    /**
     * Creates a SynopAnalyzerDiscoveryService with enabled autostart.
     *
     */
    public SynopAnalyzerDiscoveryService(StationDB stationDB, LocationProvider locationProvider) {
        super(Collections.singleton(THING_SYNOP), DISCOVER_TIMEOUT_SECONDS);
        this.locationProvider = locationProvider;
        this.stationDB = stationDB;
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
        distances.clear();

        stationDB.stations.forEach(s -> {
            PointType stationLocation = new PointType(s.getLocation());
            DecimalType distance = serverLocation.distanceFrom(stationLocation);
            distances.put(s.idOmm, distance.doubleValue());
        });

        Map<Integer, Double> result = distances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Integer nearestId = result.entrySet().iterator().next().getKey();
        Optional<Station> station = stationDB.stations.stream().filter(s -> s.idOmm == nearestId).findFirst();
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_SYNOP, nearestId.toString()))
                .withLabel(String.format("Synop : %s", station.get().usualName))
                .withProperty(SynopAnalyzerConfiguration.STATION_ID, nearestId)
                .withRepresentationProperty(SynopAnalyzerConfiguration.STATION_ID).build());
    }
}
