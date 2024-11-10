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
package org.openhab.binding.fmiweather.internal.discovery;

import static org.openhab.binding.fmiweather.internal.BindingConstants.*;
import static org.openhab.binding.fmiweather.internal.discovery.CitiesOfFinland.CITIES_OF_FINLAND;

import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.fmiweather.internal.BindingConstants;
import org.openhab.binding.fmiweather.internal.client.Client;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.exception.FMIResponseException;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FMIWeatherDiscoveryService} creates things based on the configured location.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.fmiweather")
public class FMIWeatherDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(FMIWeatherDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_OBSERVATION, THING_TYPE_FORECAST);
    private static final long STATIONS_CACHE_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final int STATIONS_TIMEOUT_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int LOCATION_CHANGED_CHECK_INTERVAL_SECONDS = 60;
    private static final int FIND_STATION_METERS = 80_000;

    private final LocationProvider locationProvider;
    private final HttpClient httpClient;
    private final ExpiringCache<Set<Location>> stationsCache;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable PointType previousLocation;

    /**
     * Creates a {@link FMIWeatherDiscoveryService} with immediately enabled background discovery.
     */
    @Activate
    public FMIWeatherDiscoveryService(final @Reference LocationProvider locationProvider,
            final @Reference HttpClientFactory httpClientFactory) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        this.locationProvider = locationProvider;
        httpClient = httpClientFactory.getCommonHttpClient();

        stationsCache = new ExpiringCache<>(STATIONS_CACHE_MILLIS, () -> {
            try {
                return new Client(httpClient).queryWeatherStations(STATIONS_TIMEOUT_MILLIS);
            } catch (FMIUnexpectedResponseException e) {
                logger.warn(
                        "Unexpected error with the response, potentially API format has changed. Printing out details",
                        e);
            } catch (FMIResponseException e) {
                logger.warn("Error when querying stations, {}: {}", e.getClass().getSimpleName(), e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted");
            }
            // Return empty set on errors
            return Collections.emptySet();
        });
    }

    @Override
    protected void startScan() {
        PointType location = null;
        logger.debug("Starting FMI Weather discovery scan");
        location = locationProvider.getLocation();
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will discover all stations");
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
            }, 0, LOCATION_CHANGED_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
            logger.debug("Scheduled FMI Weather location-changed discovery job every {} seconds",
                    LOCATION_CHANGED_CHECK_INTERVAL_SECONDS);
        }
    }

    public void createResults(@Nullable PointType location) {
        createForecastForCurrentLocation(location);
        createForecastsForCities(location);
        createObservationsForStations(location);
    }

    private void createForecastForCurrentLocation(@Nullable PointType currentLocation) {
        if (currentLocation != null) {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(UID_LOCAL_FORECAST)
                    .withLabel("FMI local weather forecast")
                    .withProperty(LOCATION,
                            "%s,%s".formatted(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .withRepresentationProperty(LOCATION).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void createForecastsForCities(@Nullable PointType currentLocation) {
        CITIES_OF_FINLAND.stream().filter(location2 -> isClose(currentLocation, location2)).forEach(city -> {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_FORECAST, cleanId("city_%s".formatted(city.name))))
                    .withProperty(LOCATION,
                            "%s,%s".formatted(city.latitude.toPlainString(), city.longitude.toPlainString()))
                    .withLabel("FMI weather forecast for %s".formatted(city.name)).withRepresentationProperty(LOCATION)
                    .build();
            thingDiscovered(discoveryResult);
        });
    }

    private void createObservationsForStations(@Nullable PointType location) {
        List<Location> candidateStations = new LinkedList<>();
        List<Location> filteredStations = new LinkedList<>();
        cachedStations().peek(station -> {
            if (logger.isDebugEnabled()) {
                candidateStations.add(station);
            }
        }).filter(location2 -> isClose(location, location2)).peek(station -> {
            if (logger.isDebugEnabled()) {
                filteredStations.add(station);
            }
        }).forEach(station -> {
            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_OBSERVATION,
                            cleanId("station_%s_%s".formatted(station.id, station.name))))
                    .withLabel("FMI weather observation for %s".formatted(station.name))
                    .withProperty(BindingConstants.FMISID, station.id)
                    .withRepresentationProperty(BindingConstants.FMISID).build();
            thingDiscovered(discoveryResult);
        });
        if (logger.isDebugEnabled()) {
            logger.debug("Candidate stations: {}",
                    candidateStations.stream().map(station -> "%s (%s)".formatted(station.name, station.id))
                            .collect(Collectors.toCollection(TreeSet<String>::new)));
            logger.debug("Filtered stations: {}",
                    filteredStations.stream().map(station -> "%s (%s)".formatted(station.name, station.id))
                            .collect(Collectors.toCollection(TreeSet<String>::new)));
        }
    }

    private static String cleanId(String id) {
        return Normalizer.normalize(id, Normalizer.Form.NFKD).replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static boolean isClose(@Nullable PointType location, Location location2) {
        return location == null ? true
                : new PointType(new DecimalType(location2.latitude), new DecimalType(location2.longitude))
                        .distanceFrom(location).doubleValue() < FIND_STATION_METERS;
    }

    @SuppressWarnings("null")
    private Stream<Location> cachedStations() {
        Set<Location> stations = stationsCache.getValue();
        if (stations.isEmpty()) {
            stationsCache.invalidateValue();
        }
        return stationsCache.getValue().stream();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping FMI Weather background discovery");
        ScheduledFuture<?> discoveryJob = this.discoveryJob;
        if (discoveryJob != null) {
            if (discoveryJob.cancel(true)) {
                this.discoveryJob = null;
                logger.debug("Stopped FMI Weather background discovery");
            }
        }
    }
}
