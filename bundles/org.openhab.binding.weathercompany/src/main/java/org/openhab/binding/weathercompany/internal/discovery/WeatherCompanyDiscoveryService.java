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
package org.openhab.binding.weathercompany.internal.discovery;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyAbstractHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherCompanyDiscoveryService} creates things based on the location
 * configured in openHAB
 *
 * @author Mark Hilbush - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.weathercompany")
public class WeatherCompanyDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 4;
    private static final int DISCOVERY_INTERVAL = 1200;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @NonNullByDefault({}) LocationProvider locationProvider;

    /**
     * Creates a WeatherCompanyDiscoveryService with discovery enabled
     */
    public WeatherCompanyDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Weather Company discovery scan");
        createDiscoveryResult();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            job = scheduler.scheduleWithFixedDelay(() -> {
                createDiscoveryResult();
            }, 15, DISCOVERY_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Discovery: Scheduled Weather Company discovery job every {} seconds", DISCOVERY_INTERVAL);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            discoveryJob = null;
            logger.debug("Discovery: Stopped Weather Company device background discovery");
        }
    }

    private void createDiscoveryResult() {
        if (locationProvider == null || locationProvider.getLocation() == null) {
            logger.debug("Discovery: Can't create discovery result because location is not set in openHAB");
            return;
        }
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("Discovery: Can't create discovery result because location is not set in openHAB");
            return;
        }
        if (localeProvider == null) {
            logger.debug("Discovery: Can't create discovery result because locale is not set in openHAB");
            return;
        }
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(CONFIG_LOCATION_TYPE, CONFIG_LOCATION_TYPE_GEOCODE);
        properties.put(CONFIG_GEOCODE, String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        properties.put(CONFIG_LANGUAGE,
                WeatherCompanyAbstractHandler.getWeatherCompanyLanguage(localeProvider.getLocale()));
        ThingUID localWeatherThing = new ThingUID(THING_TYPE_WEATHER_FORECAST, LOCAL);
        thingDiscovered(DiscoveryResultBuilder.create(localWeatherThing).withLabel(LOCAL_WEATHER)
                .withProperties(properties).build());
    }

    @Reference
    protected void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    protected void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }
}
