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
package org.openhab.binding.weatherunderground.internal.discovery;

import static org.openhab.binding.weatherunderground.internal.WeatherUndergroundBindingConstants.*;
import static org.openhab.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.weatherunderground.internal.handler.WeatherUndergroundHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherUndergroundDiscoveryService} creates things based on the configured location.
 *
 * @author Laurent Garnier - Initial Contribution
 * @author Laurent Garnier - Consider locale (language) when discovering a new thing
 */
public class WeatherUndergroundDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WEATHER);
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL = 60;

    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private ScheduledFuture<?> discoveryJob;
    private PointType previousLocation;
    private String previousLanguage;
    private String previousCountry;

    private final ThingUID bridgeUID;

    /**
     * Creates a WeatherUndergroundDiscoveryService with enabled autostart.
     */

    public WeatherUndergroundDiscoveryService(ThingUID bridgeUID, LocaleProvider localeProvider,
            LocationProvider locationProvider) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        this.bridgeUID = bridgeUID;
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
    }

    /* We override this method to allow a call from the thing handler factory */
    @Override
    public void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    /* We override this method to allow a call from the thing handler factory */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Weather Underground discovery scan");
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }
        createResults(location, localeProvider.getLocale());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Weather Underground device background discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                PointType currentLocation = locationProvider.getLocation();
                String currentLanguage = localeProvider.getLocale().getLanguage();
                String currentCountry = localeProvider.getLocale().getCountry();
                if (currentLocation != null) {
                    boolean update = false;
                    if (!Objects.equals(currentLocation, previousLocation)) {
                        logger.debug("Location has been changed from {} to {}: Creating new discovery result",
                                previousLocation, currentLocation);
                        update = true;
                    } else if (!Objects.equals(currentLanguage, previousLanguage)) {
                        logger.debug("Language has been changed from {} to {}: Creating new discovery result",
                                previousLanguage, currentLanguage);
                        update = true;
                    } else if (!Objects.equals(currentCountry, previousCountry)) {
                        logger.debug("Country has been changed from {} to {}: Creating new discovery result",
                                previousCountry, currentCountry);
                        update = true;
                    }
                    if (update) {
                        createResults(currentLocation, localeProvider.getLocale());
                        previousLocation = currentLocation;
                        previousLanguage = currentLanguage;
                        previousCountry = currentCountry;
                    }
                }
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled Weather Underground location-changed job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Weather Underground device background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
            logger.debug("Stopped Weather Underground device background discovery");
        }
    }

    private void createResults(PointType location, Locale locale) {
        ThingUID localWeatherThing = new ThingUID(THING_TYPE_WEATHER, bridgeUID, LOCAL);
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(LOCATION, String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        String lang = WeatherUndergroundHandler.getCodeFromLanguage(locale);
        if (!lang.isEmpty()) {
            properties.put(LANGUAGE, lang);
        }
        thingDiscovered(DiscoveryResultBuilder.create(localWeatherThing).withLabel("Local Weather")
                .withProperties(properties).withBridge(bridgeUID).build());
    }
}
