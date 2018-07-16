/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.internal.discovery;

import static org.openhab.binding.openuv.OpenUVBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openuv.handler.OpenUVBridgeHandler;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenUVDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class OpenUVDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OpenUVDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;

    private final LocationProvider locationProvider;
    private final OpenUVBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable PointType previousLocation;

    /**
     * Creates a OpenUVDiscoveryService with enabled autostart.
     */
    public OpenUVDiscoveryService(OpenUVBridgeHandler bridgeHandler, LocationProvider locationProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting OpenUV discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (discoveryJob == null) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (currentLocation != null && !Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled OpenUV-changed job every {} seconds", LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    public void createResults(PointType location) {
        ThingUID localOpenUVThing = new ThingUID(LOCATION_REPORT_THING_TYPE, LOCAL);
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        properties.put(LOCATION, location.toString());
        thingDiscovered(DiscoveryResultBuilder.create(localOpenUVThing).withLabel("Local UV Information")
                .withProperties(properties).withRepresentationProperty(location.toString()).withBridge(bridgeUID)
                .build());
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping OpenUV background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            if (discoveryJob.cancel(true)) {
                discoveryJob = null;
                logger.debug("Stopped OpenUV background discovery");
            }
        }
    }

}
