/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.internal.discovery;

import static org.openhab.binding.airquality.AirQualityBindingConstants.*;
import static org.openhab.binding.airquality.internal.AirQualityConfiguration.LOCATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirQualityDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.airquality")
public class AirQualityDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AirQualityDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AQI);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;

    private LocationProvider locationProvider;
    private ScheduledFuture<?> discoveryJob;
    private PointType previousLocation;

    /**
     * Creates a AirQualityDiscoveryService with enabled autostart.
     */
    public AirQualityDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Air Quality discovery scan");
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
                if (!Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled Air Qualitylocation-changed job every {} seconds", LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    public void createResults(PointType location) {
        ThingUID localAirQualityThing = new ThingUID(THING_TYPE_AQI, LOCAL);
        Map<String, Object> properties = new HashMap<>();
        properties.put(LOCATION, String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        thingDiscovered(DiscoveryResultBuilder.create(localAirQualityThing).withLabel("Local Air Quality")
                .withProperties(properties).build());
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Air Quality background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            if (discoveryJob.cancel(true)) {
                discoveryJob = null;
                logger.debug("Stopped Air Quality background discovery");
            }
        }
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

}
