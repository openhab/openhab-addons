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
package org.openhab.binding.weathercompany.internal.discovery;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyAbstractHandler;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherCompanyDiscoveryService} creates things based on the location
 * configured in openHAB
 *
 * @author Mark Hilbush - Initial Contribution
 */
@NonNullByDefault
public class WeatherCompanyDiscoveryService extends AbstractDiscoveryService {
    // Thing for local weather created during discovery
    private static final String LOCAL = "local";

    private static final int DISCOVER_TIMEOUT_SECONDS = 4;
    private static final int DISCOVERY_INTERVAL_SECONDS = 1200;

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyDiscoveryService.class);

    private final LocationProvider locationProvider;
    private final WeatherCompanyBridgeHandler bridgeHandler;

    private @Nullable ScheduledFuture<?> discoveryJob;

    /**
     * Creates a WeatherCompanyDiscoveryService with discovery enabled
     */
    public WeatherCompanyDiscoveryService(WeatherCompanyBridgeHandler bridgeHandler, LocationProvider locationProvider,
            LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
        this.bridgeHandler = bridgeHandler;
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
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
        logger.debug("Discovery: Starting Weather Company discovery scan");
        createDiscoveryResult();
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            job = scheduler.scheduleWithFixedDelay(() -> {
                createDiscoveryResult();
            }, 15, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
            logger.debug("Discovery: Scheduled Weather Company discovery job to run every {} seconds",
                    DISCOVERY_INTERVAL_SECONDS);
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
        PointType location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("Discovery: Can't create discovery result because location is not set in openHAB");
            return;
        }
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(CONFIG_LOCATION_TYPE, CONFIG_LOCATION_TYPE_GEOCODE);
        properties.put(CONFIG_GEOCODE, String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        properties.put(CONFIG_LANGUAGE, WeatherCompanyAbstractHandler.lookupLanguage(localeProvider.getLocale()));
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID localWeatherThing = new ThingUID(THING_TYPE_WEATHER_FORECAST, bridgeUID, LOCAL);
        thingDiscovered(DiscoveryResultBuilder.create(localWeatherThing).withBridge(bridgeUID)
                .withLabel("@text/discovery.weather-forecast.local.label").withProperties(properties).build());
    }
}
