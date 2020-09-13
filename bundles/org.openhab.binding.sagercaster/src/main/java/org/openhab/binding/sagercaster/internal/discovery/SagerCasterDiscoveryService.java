/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sagercaster.internal.discovery;

import static org.openhab.binding.sagercaster.internal.SagerCasterBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SagerCasterDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.sagercaster")
public class SagerCasterDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SagerCasterDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private @NonNullByDefault({}) LocationProvider locationProvider;
    private @NonNullByDefault({}) ScheduledFuture<?> sagerCasterDiscoveryJob;
    private @Nullable PointType previousLocation;

    private static final ThingUID sagerCasterThing = new ThingUID(THING_TYPE_SAGERCASTER, LOCAL);

    /**
     * Creates a SagerCasterDiscoveryService with enabled autostart.
     */
    public SagerCasterDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Sager Weathercaster discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (sagerCasterDiscoveryJob == null) {
            sagerCasterDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (currentLocation != null && !Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled SagerCaster location-changed job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Sager Weathercaster background discovery");
        if (sagerCasterDiscoveryJob != null && !sagerCasterDiscoveryJob.isCancelled()) {
            if (sagerCasterDiscoveryJob.cancel(true)) {
                sagerCasterDiscoveryJob = null;
                logger.debug("Stopped SagerCaster device background discovery");
            }
        }
    }

    public void createResults(PointType location) {
        String propGeolocation;
        propGeolocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        thingDiscovered(DiscoveryResultBuilder.create(sagerCasterThing).withLabel("Local Sager Weathercaster")
                .withRepresentationProperty(CONFIG_LOCATION).withProperty(CONFIG_LOCATION, propGeolocation).build());
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }
}
