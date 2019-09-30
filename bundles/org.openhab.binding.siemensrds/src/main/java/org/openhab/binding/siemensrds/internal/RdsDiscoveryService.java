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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Siemens RDS thermostats
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class RdsDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(RdsDiscoveryService.class);

    private ScheduledFuture<?> discoveryScheduler;
    private RdsCloudHandler cloud;

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_RDS).collect(Collectors.toSet()));

    public RdsDiscoveryService(RdsCloudHandler cloud) {
        // note: background discovery is enabled in the super method..
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIMEOUT);
        this.cloud = cloud;
    }

    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            cloud.getToken();
        }
        if (cloud.getThing().getStatus() == ThingStatus.ONLINE) {
            discoverPlants();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("start background discovery..");

        if (discoveryScheduler == null || discoveryScheduler.isCancelled()) {
            discoveryScheduler = scheduler.scheduleWithFixedDelay(this::startScan, 10, 
                    DISCOVERY_REFRESH_PERIOD, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("stop background discovery..");

        if (discoveryScheduler != null && !discoveryScheduler.isCancelled()) {
            discoveryScheduler.cancel(true);
        }
    }

    private void discoverPlants() {
        if (cloud != null) {
            RdsPlants plants = RdsPlants.create(cloud.getApiKey(), cloud.getToken());
            if (plants != null) {
                for (RdsPlants.PlantInfo plant : plants.getPlants()) {
                    publishPlant(plant);
                }
            }
        }
    }

    private void publishPlant(RdsPlants.PlantInfo plant) {
        if (plant != null) {
            String plantId = plant.getId();

            if (plantId != null && !plantId.isEmpty()) {
                RdsDataPoints points = RdsDataPoints.create(cloud.getApiKey(), cloud.getToken(), plantId);

                if (points != null) {
                    State desc = points.getRaw(HIE_DESCRIPTION);

                    if (desc != null) {
                        String label = desc.toString().replaceAll("\\s+", "_");

                        ThingTypeUID typeUID = THING_TYPE_RDS;
                        ThingUID bridgeUID = cloud.getThing().getUID();
                        ThingUID plantUID = new ThingUID(typeUID, bridgeUID, plantId);

                        DiscoveryResult disco = DiscoveryResultBuilder.create(plantUID).withBridge(bridgeUID)
                                .withLabel(label).withProperty(PROP_PLANT_ID, plantId)
                                .withRepresentationProperty(PROP_PLANT_ID).build();

                        logger.debug("discovered typeUID={}, plantUID={}, brigeUID={}, label={}, plantId={}, ", 
                                typeUID, plantUID, bridgeUID, label, plantId);

                        thingDiscovered(disco);

                        return;
                    }
                }
            }
        }
        logger.debug("discovery error!");
    }

}
