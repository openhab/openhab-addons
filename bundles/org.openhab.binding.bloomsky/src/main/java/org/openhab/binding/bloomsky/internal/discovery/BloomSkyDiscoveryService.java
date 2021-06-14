/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.discovery;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bloomsky.internal.dto.BloomSkyJsonSensorData;
import org.openhab.binding.bloomsky.internal.handler.BloomSkyBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BloomSkyDiscoveryService} is responsible for creating things based on devices
 * retrieved from the BloomSky API. The API returns an array of 1 or more locations with the
 * device owners Sky and Storm weather stations.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkyDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BloomSkyDiscoveryService.class);

    private static final int DISCOVERY_INTERVAL_SECONDS = (60 * 5); // Every 5 Minutes
    private static final int DISCOVER_TIMEOUT_SECONDS = 4;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private final BloomSkyBridgeHandler bridgeHandler;

    /**
     * Creates an BloomSky Device Discovery Service.
     *
     * @param bridgeHandler
     */
    public BloomSkyDiscoveryService(BloomSkyBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
        activate(null);
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
        logger.debug("Discovery: Activating discovery service for {}", bridgeHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        logger.debug("Discovery: Deactivating discovery service for {}", bridgeHandler.getThing().getUID());
    }

    @Override
    protected void startScan() {
        logger.debug("Discovery: Starting BloomSky Device discovery scan.");
        scanForNewDevices();
        stopScan();
    }

    private void scanForNewDevices() {
        Optional<BloomSkyJsonSensorData[]> registeredSkyDevices = bridgeHandler.getBloomSkyDevices();

        registeredSkyDevices.ifPresent(skyDevices -> {
            for (BloomSkyJsonSensorData device : skyDevices) {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                ThingTypeUID thingTypeUID = THING_TYPE_SKY;
                ThingUID skyThingUid = new ThingUID(THING_TYPE_SKY, bridgeUID, device.getDeviceID());

                String skyThingLabel = (device.getDeviceName() + " (" + device.getData().getDeviceType() + " at "
                        + device.getFullAddress() + ")");

                Map<String, Object> skyProperties = new HashMap<>();
                skyProperties.put(SKY_DEVICE_ID, device.getDeviceID());
                skyProperties.put(SKY_DEVICE_NAME, device.getDeviceName());
                skyProperties.put(SKY_DEVICE_MODEL, device.getData().getDeviceType());
                logger.debug("scanForNewDevices - found SKY called {} with DeviceID = {}", device.getDeviceName(),
                        device.getDeviceID());
                DiscoveryResult skyDiscoveryResult = DiscoveryResultBuilder.create(skyThingUid)
                        .withThingType(thingTypeUID).withProperties(skyProperties).withBridge(bridgeUID)
                        .withRepresentationProperty(SKY_DEVICE_ID).withLabel(skyThingLabel).build();

                thingDiscovered(skyDiscoveryResult);
                // Check for STORM device associated with this SKY weather station
                if (!device.getStorm().getuVIndex().isBlank() && !device.getStorm().getWindDirection().isBlank()) {
                    logger.debug("scanForNewDevices found a STORM associated with the SKY device called: {}",
                            device.getDeviceName());

                    ThingTypeUID stormThingTypeUID = THING_TYPE_STORM;
                    ThingUID stormThingUid = new ThingUID(THING_TYPE_STORM, bridgeUID, device.getDeviceID());

                    String stormThingLabel = (device.getDeviceName() + " (" + STORM_MODEL + " at "
                            + device.getFullAddress() + ")");
                    Map<String, Object> stormProperties = new HashMap<>();
                    stormProperties.clear();
                    stormProperties.put(STORM_ASSOCIATED_WITH_SKY_DEVICE_ID, device.getDeviceID());
                    stormProperties.put(STORM_ASSOCAITED_WITH_SKY_DEVICE_NAME, device.getDeviceName());
                    stormProperties.put(STORM_DEVICE_MODEL, STORM_MODEL);
                    DiscoveryResult stormDiscoveryResult = DiscoveryResultBuilder.create(stormThingUid)
                            .withThingType(stormThingTypeUID).withProperties(stormProperties).withBridge(bridgeUID)
                            .withRepresentationProperty(STORM_ASSOCIATED_WITH_SKY_DEVICE_ID).withLabel(stormThingLabel)
                            .build();
                    thingDiscovered(stormDiscoveryResult);
                }
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual BloomSky Device discovery scan.");
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            logger.debug("Start BloomSky device background discovery job at interval {} s.",
                    DISCOVERY_INTERVAL_SECONDS);
            job = scheduler.scheduleWithFixedDelay(() -> {
                scanForNewDevices();
            }, 15, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
            // job = scheduler.scheduleWithFixedDelay(this::scanForNewDevices, 0, DISCOVERY_INTERVAL_SECONDS,
            // TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job != null && !job.isCancelled()) {
            logger.debug("Stop BloomSky Device background discovery job.");
            if (job.cancel(true)) {
                discoveryJob = null;
            }
        }
    }
}
