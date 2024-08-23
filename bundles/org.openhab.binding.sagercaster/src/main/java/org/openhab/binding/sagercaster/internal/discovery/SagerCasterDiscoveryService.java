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
package org.openhab.binding.sagercaster.internal.discovery;

import static org.openhab.binding.sagercaster.internal.SagerCasterBindingConstants.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
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
@Component(service = DiscoveryService.class, configurationPid = "discovery.sagercaster")
public class SagerCasterDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private static final ThingUID SAGER_CASTER_THING = new ThingUID(THING_TYPE_SAGERCASTER, LOCAL);

    private final Logger logger = LoggerFactory.getLogger(SagerCasterDiscoveryService.class);
    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable PointType previousLocation;

    /**
     * Creates a SagerCasterDiscoveryService with enabled autostart.
     */
    @Activate
    public SagerCasterDiscoveryService(final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference LocationProvider locationProvider) {
        super(Set.of(THING_TYPE_SAGERCASTER), DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(@Nullable Map<String, Object> configProperties) {
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
            logger.debug("Scheduled SagerCaster location-changed job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> localJob = discoveryJob;
        logger.debug("Stopping Sager Weathercaster background discovery");
        if (localJob != null && !localJob.isCancelled()) {
            if (localJob.cancel(true)) {
                discoveryJob = null;
                logger.debug("Stopped SagerCaster device background discovery");
            }
        }
    }

    public void createResults(PointType location) {
        String propGeolocation = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        thingDiscovered(DiscoveryResultBuilder.create(SAGER_CASTER_THING).withLabel("Local Weather Forecast")
                .withRepresentationProperty(CONFIG_LOCATION).withProperty(CONFIG_LOCATION, propGeolocation).build());
    }
}
