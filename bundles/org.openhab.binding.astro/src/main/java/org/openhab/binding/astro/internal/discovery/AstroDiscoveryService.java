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
package org.openhab.binding.astro.internal.discovery;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AstroDiscoveryService} creates things based on the configured location.
 *
 * @author Gerhard Riegler - Initial Contribution
 * @author Stefan Triller - Use configured location
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.astro")
public class AstroDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AstroDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;
    private LocationProvider locationProvider;
    private ScheduledFuture<?> astroDiscoveryJob;
    private PointType previousLocation;

    private static final ThingUID SUN_THING = new ThingUID(THING_TYPE_SUN, LOCAL);
    private static final ThingUID MOON_THING = new ThingUID(THING_TYPE_MOON, LOCAL);

    /**
     * Creates a AstroDiscoveryService with enabled autostart.
     */
    public AstroDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    @Activate
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
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
                if (!Objects.equals(currentLocation, previousLocation)) {
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
        if (astroDiscoveryJob != null && !astroDiscoveryJob.isCancelled()) {
            if (astroDiscoveryJob.cancel(true)) {
                astroDiscoveryJob = null;
                logger.debug("Stopped Astro device background discovery");
            }
        }
    }

    public void createResults(PointType location) {
        String propGeolocation;
        propGeolocation = String.format("%s,%s,%s", location.getLatitude(), location.getLongitude(),
                location.getAltitude());
        thingDiscovered(DiscoveryResultBuilder.create(SUN_THING).withLabel("Local Sun")
                .withProperty("geolocation", propGeolocation).withRepresentationProperty("geolocation").build());
        thingDiscovered(DiscoveryResultBuilder.create(MOON_THING).withLabel("Local Moon")
                .withProperty("geolocation", propGeolocation).withRepresentationProperty("geolocation").build());
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
