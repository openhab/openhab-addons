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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemensrds.internal.RdsPlants.PlantInfo;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * Discovery service for Siemens RDS thermostats
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
@NonNullByDefault
public class RdsDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(RdsDiscoveryService.class);

    private @Nullable ScheduledFuture<?> discoveryScheduler;

    private @Nullable RdsCloudHandler cloud;

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
        RdsCloudHandler cloud = this.cloud;

        if (cloud != null && cloud.getThing().getStatus() != ThingStatus.ONLINE) {
            try {
                cloud.getToken();
            } catch (RdsCloudException e) {
                logger.debug("unexpected: {} = \"{}\"", e.getClass().getName(), e.getMessage());
            }
        }

        if (cloud != null && cloud.getThing().getStatus() == ThingStatus.ONLINE) {
            discoverPlants();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("start background discovery..");

        ScheduledFuture<?> discoveryScheduler = this.discoveryScheduler;
        if (discoveryScheduler == null || discoveryScheduler.isCancelled()) {
            this.discoveryScheduler = scheduler.scheduleWithFixedDelay(this::startScan, 10, DISCOVERY_REFRESH_PERIOD,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("stop background discovery..");

        ScheduledFuture<?> discoveryScheduler = this.discoveryScheduler;
        if (discoveryScheduler != null && !discoveryScheduler.isCancelled()) {
            discoveryScheduler.cancel(true);
            this.discoveryScheduler = null;
        }
    }

    private void discoverPlants() {
        RdsCloudHandler cloud = this.cloud;

        if (cloud != null) {
            @Nullable
            RdsPlants plantClass = null;

            try {
                String url = URL_PLANTS;

                logger.debug(LOG_HTTP_COMMAND, HTTP_GET, url.length());
                logger.debug(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);

                String json = RdsDataPoints.httpGenericGetJson(cloud.getApiKey(), cloud.getToken(), url);

                if (logger.isTraceEnabled()) {
                    logger.trace(LOG_CONTENT_LENGTH, LOG_RECEIVED_MSG, json.length());
                    logger.trace(LOG_PAYLOAD_FMT, LOG_RECEIVED_MARK, json);
                } else if (logger.isDebugEnabled()) {
                    logger.debug(LOG_CONTENT_LENGTH_ABR, LOG_RECEIVED_MSG, json.length());
                    logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_RECEIVED_MARK,
                            json.substring(0, Math.min(json.length(), 30)));
                }

                plantClass = RdsPlants.createFromJson(json);
            } catch (RdsCloudException e) {
                logger.warn(LOG_SYSTEM_EXCEPTION, "discoverPlants()", e.getClass().getName(), e.getMessage());
                return;
            } catch (JsonParseException | IOException e) {
                logger.warn(LOG_RUNTIME_EXCEPTION, "discoverPlants()", e.getClass().getName(), e.getMessage());
                return;
            }

            if (plantClass != null) {
                List<PlantInfo> plants = plantClass.getPlants();
                if (plants != null) {
                    for (PlantInfo plant : plants) {
                        publishPlant(plant);
                    }
                }
            }
        }
    }

    private void publishPlant(PlantInfo plant) {
        RdsCloudHandler cloud = this.cloud;
        try {
            if (cloud == null) {
                throw new RdsCloudException("missing cloud handler");
            }

            String plantId = plant.getId();
            String url = String.format(URL_POINTS, plantId);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_HTTP_COMMAND, HTTP_GET, url.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_HTTP_COMMAND_ABR, HTTP_GET, url.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_SENDING_MARK, url.substring(0, Math.min(url.length(), 30)));
            }

            String json = RdsDataPoints.httpGenericGetJson(cloud.getApiKey(), cloud.getToken(), url);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_CONTENT_LENGTH, LOG_RECEIVED_MSG, json.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_RECEIVED_MARK, json);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_CONTENT_LENGTH_ABR, LOG_RECEIVED_MSG, json.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_RECEIVED_MARK, json.substring(0, Math.min(json.length(), 30)));
            }

            RdsDataPoints points = RdsDataPoints.createFromJson(json);
            if (points == null) {
                throw new RdsCloudException("no points returned");
            }

            State desc = points.getPointByClass(HIE_DESCRIPTION).getState();
            String label = desc.toString().replaceAll("\\s+", "_");

            ThingTypeUID typeUID = THING_TYPE_RDS;
            ThingUID bridgeUID = cloud.getThing().getUID();
            ThingUID plantUID = new ThingUID(typeUID, bridgeUID, plantId);

            DiscoveryResult disco = DiscoveryResultBuilder.create(plantUID).withBridge(bridgeUID).withLabel(label)
                    .withProperty(PROP_PLANT_ID, plantId).withRepresentationProperty(PROP_PLANT_ID).build();

            logger.debug("discovered typeUID={}, plantUID={}, brigeUID={}, label={}, plantId={}, ", typeUID, plantUID,
                    bridgeUID, label, plantId);

            thingDiscovered(disco);
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "publishPlant()", e.getClass().getName(), e.getMessage());
        } catch (JsonParseException | IOException e) {
            logger.warn(LOG_RUNTIME_EXCEPTION, "publishPlant()", e.getClass().getName(), e.getMessage());
        }
    }
}
