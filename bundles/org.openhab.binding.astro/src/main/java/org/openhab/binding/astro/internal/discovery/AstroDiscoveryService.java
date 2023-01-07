/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.discovery;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.config.AstroThingConfig.*;

import java.util.Arrays;
import java.util.HashSet;
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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AstroDiscoveryService} creates things based on the configured location.
 *
 * @author Gerhard Riegler - Initial Contribution
 * @author Stefan Triller - Use configured location
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.astro")
public class AstroDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private static final Set<String> METEO_BASED_COUNTRIES = Set.of("NZ", "AU");
    private static final ThingUID SUN_THING = new ThingUID(THING_TYPE_SUN, LOCAL);
    private static final ThingUID MOON_THING = new ThingUID(THING_TYPE_MOON, LOCAL);

    private final Logger logger = LoggerFactory.getLogger(AstroDiscoveryService.class);

    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> astroDiscoveryJob;
    private @Nullable PointType previousLocation;

    @Activate
    public AstroDiscoveryService(final @Reference LocationProvider locationProvider,
            final @Reference LocaleProvider localeProvider, final @Reference TranslationProvider i18nProvider,
            @Nullable Map<String, Object> configProperties) {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        activate(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Astro discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location);
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (astroDiscoveryJob == null) {
            astroDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                if (currentLocation != null && !Objects.equals(currentLocation, previousLocation)) {
                    logger.debug("Location has been changed from {} to {}: Creating new discovery results",
                            previousLocation, currentLocation);
                    createResults(currentLocation);
                    previousLocation = currentLocation;
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled astro location-changed job every {} seconds", LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Astro device background discovery");
        ScheduledFuture<?> discoveryJob = astroDiscoveryJob;
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
        }
        astroDiscoveryJob = null;
    }

    public void createResults(PointType location) {
        String propGeolocation = location.toString();
        String country = localeProvider.getLocale().getCountry();

        thingDiscovered(DiscoveryResultBuilder.create(SUN_THING).withLabel("Local Sun")
                .withProperty(GEOLOCATION, propGeolocation)
                .withProperty(USE_METEOROLOGICAL_SEASON, METEO_BASED_COUNTRIES.contains(country))
                .withRepresentationProperty(GEOLOCATION).build());

        thingDiscovered(DiscoveryResultBuilder.create(MOON_THING).withLabel("Local Moon")
                .withProperty(GEOLOCATION, propGeolocation).withRepresentationProperty(GEOLOCATION).build());
    }
}
