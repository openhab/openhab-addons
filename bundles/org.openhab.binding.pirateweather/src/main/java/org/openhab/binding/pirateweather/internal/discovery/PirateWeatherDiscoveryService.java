/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.pirateweather.internal.discovery;

import static org.openhab.binding.pirateweather.internal.PirateWeatherBindingConstants.*;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pirateweather.internal.handler.PirateWeatherAPIHandler;
import org.openhab.binding.pirateweather.internal.handler.PirateWeatherWeatherAndForecastHandler;
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
 * The {@link PirateWeatherDiscoveryService} creates things based on the configured location.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PirateWeatherDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PirateWeatherDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;
    private static final int DISCOVERY_INTERVAL_SECONDS = 60;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private final LocationProvider locationProvider;
    private @Nullable PointType previousLocation;

    private final PirateWeatherAPIHandler bridgeHandler;

    /**
     * Creates an PirateWeatherLocationDiscoveryService.
     */
    public PirateWeatherDiscoveryService(PirateWeatherAPIHandler bridgeHandler, LocationProvider locationProvider,
            LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        super(PirateWeatherWeatherAndForecastHandler.SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        activate(null);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), bridgeHandler.getThing().getUID());
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start manual Pirate Weather Location discovery scan.");
        scanForNewLocation();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual Pirate Weather Location discovery scan.");
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            logger.debug("Start Pirate Weather Location background discovery job at interval {} s.",
                    DISCOVERY_INTERVAL_SECONDS);
            discoveryJob = scheduler.scheduleWithFixedDelay(this::scanForNewLocation, 0, DISCOVERY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            logger.debug("Stop Pirate Weather Location background discovery job.");
            if (discoveryJob.cancel(true)) {
                discoveryJob = null;
            }
        }
    }

    private void scanForNewLocation() {
        PointType currentLocation = locationProvider.getLocation();
        if (currentLocation == null) {
            logger.debug("Location is not set -> Will not provide any discovery results.");
        } else if (!Objects.equals(currentLocation, previousLocation)) {
            logger.debug("Location has been changed from {} to {} -> Creating new discovery results.", previousLocation,
                    currentLocation);
            createResults(currentLocation);
            previousLocation = currentLocation;
        } else {
            createResults(currentLocation);
        }
    }

    private void createResults(PointType location) {
        String locationString = location.toFullString();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        createWeatherAndForecastResult(locationString, bridgeUID);
    }

    private void createWeatherAndForecastResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_WEATHER_AND_FORECAST, bridgeUID, LOCAL))
                .withLabel("Local weather and forecast").withProperty(CONFIG_LOCATION, location)
                .withRepresentationProperty(CONFIG_LOCATION).withBridge(bridgeUID).build());
    }
}
