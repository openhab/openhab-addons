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
package org.openhab.binding.openweathermap.internal.discovery;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.handler.AbstractOpenWeatherMapHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWeatherMapDiscoveryService} creates things based on the configured location.
 *
 * @author Christoph Weitkamp - Initial Contribution
 */
@NonNullByDefault
public class OpenWeatherMapDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;
    private static final int DISCOVERY_INTERVAL_SECONDS = 60;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable LocationProvider locationProvider;
    private @Nullable PointType previousLocation;
    private @Nullable OpenWeatherMapAPIHandler bridgeHandler;

    /**
     * Creates an OpenWeatherMapLocationDiscoveryService.
     */
    public OpenWeatherMapDiscoveryService() {
        super(AbstractOpenWeatherMapHandler.SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OpenWeatherMapAPIHandler) {
            OpenWeatherMapAPIHandler localHandler = (OpenWeatherMapAPIHandler) handler;
            bridgeHandler = localHandler;
            locationProvider = localHandler.getLocationProvider();
            localeProvider = localHandler.getLocaleProvider();
            i18nProvider = localHandler.getTranslationProvider();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        OpenWeatherMapAPIHandler localBridgeHandler = bridgeHandler;
        if (localBridgeHandler != null) {
            removeOlderResults(new Date().getTime(), localBridgeHandler.getThing().getUID());
        }
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start manual OpenWeatherMap Location discovery scan.");
        scanForNewLocation(false);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual OpenWeatherMap Location discovery scan.");
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            logger.debug("Start OpenWeatherMap Location background discovery job at interval {} s.",
                    DISCOVERY_INTERVAL_SECONDS);
            localDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                scanForNewLocation(true);
            }, 0, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null && !localDiscoveryJob.isCancelled()) {
            logger.debug("Stop OpenWeatherMap Location background discovery job.");
            if (localDiscoveryJob.cancel(true)) {
                discoveryJob = null;
            }
        }
    }

    private void scanForNewLocation(boolean updateOnlyIfNewLocation) {
        OpenWeatherMapAPIHandler localBridgeHandler = bridgeHandler;
        LocationProvider localLocationProvider = locationProvider;
        if (localBridgeHandler == null || localLocationProvider == null) {
            return;
        }
        ThingUID bridgeUID = localBridgeHandler.getThing().getUID();
        PointType currentLocation = localLocationProvider.getLocation();
        if (currentLocation == null) {
            logger.debug("Location is not set -> Will not provide any discovery results.");
        } else if (!Objects.equals(currentLocation, previousLocation)) {
            logger.debug("Location has been changed from {} to {} -> Creating new discovery results.", previousLocation,
                    currentLocation);
            createResults(currentLocation, bridgeUID);
            previousLocation = currentLocation;
        } else if (!updateOnlyIfNewLocation) {
            createResults(currentLocation, bridgeUID);
        }
    }

    private void createResults(PointType location, ThingUID bridgeUID) {
        String locationString = location.toFullString();
        createWeatherAndForecastResult(locationString, bridgeUID);
        createAirPollutionResult(locationString, bridgeUID);
        createOneCallResult(locationString, bridgeUID);
        createOneCallHistoryResult(locationString, bridgeUID);
    }

    private void createWeatherAndForecastResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_WEATHER_AND_FORECAST, bridgeUID, LOCAL))
                .withLabel("@text/discovery.weather-and-forecast.local.label").withProperty(CONFIG_LOCATION, location)
                .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }

    private void createAirPollutionResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_AIR_POLLUTION, bridgeUID, LOCAL))
                .withLabel("@text/discovery.air-pollution.local.label").withProperty(CONFIG_LOCATION, location)
                .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }

    private void createOneCallResult(String location, ThingUID bridgeUID) {
        thingDiscovered(
                DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_ONECALL_WEATHER_AND_FORECAST, bridgeUID, LOCAL))
                        .withLabel("@text/discovery.onecall.local.label").withProperty(CONFIG_LOCATION, location)
                        .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }

    private void createOneCallHistoryResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_ONECALL_HISTORY, bridgeUID, LOCAL))
                .withLabel("@text/discovery.onecall-history.local.label").withProperty(CONFIG_LOCATION, location)
                .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }
}
