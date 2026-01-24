/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForecastSolarBridgeHandler} is responsible for handling the attached planes and give an accumulated
 * update.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider {
    private static final String BASE_URL = "https://api.forecast.solar/";
    private static final int CALM_DOWN_TIME_MINUTES = 61;

    private final Logger logger = LoggerFactory.getLogger(ForecastSolarBridgeHandler.class);
    private final AtomicBoolean updateTimeseriesNeeded = new AtomicBoolean(true);
    private final ScheduledExecutorService sequentialScheduler = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService(BINDING_ID, "thingHandler");

    private ForecastSolarBridgeConfiguration configuration = new ForecastSolarBridgeConfiguration();
    private Instant calmDownEnd = Instant.MIN;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PointType homeLocation;

    protected CopyOnWriteArrayList<ForecastSolarPlaneHandler> planes = new CopyOnWriteArrayList<>();

    public ForecastSolarBridgeHandler(Bridge bridge, @Nullable PointType location) {
        super(bridge);
        homeLocation = location;
    }

    /**
     * #####################
     * Handler functionality
     * #####################
     */

    @Override
    public void initialize() {
        configuration = getConfigAs(ForecastSolarBridgeConfiguration.class);
        if (!configuration.location.isBlank()) {
            // if configuration location is set, it has precedence
            try {
                homeLocation = new PointType(configuration.location);
                // continue with location from configuration
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }
        // handle location error cases
        PointType localHomeLocation = homeLocation;
        if (localHomeLocation == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.site.status.location-missing");
            return;
        }

        // update configuration with location
        Configuration editConfig = editConfiguration();
        editConfig.put("location", localHomeLocation.toString());
        updateConfiguration(editConfig);
        configuration = getConfigAs(ForecastSolarBridgeConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = sequentialScheduler.scheduleWithFixedDelay(this::updateData, 0, REFRESH_ACTUAL_INTERVAL,
                TimeUnit.MINUTES);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            switch (channel) {
                case CHANNEL_ENERGY_ACTUAL:
                case CHANNEL_ENERGY_REMAIN:
                case CHANNEL_ENERGY_TODAY:
                case CHANNEL_POWER_ACTUAL:
                    sequentialScheduler.execute(this::updateData);
                    break;
                case CHANNEL_POWER_ESTIMATE:
                case CHANNEL_ENERGY_ESTIMATE:
                    updateTimeseriesNeeded.set(true);
                    sequentialScheduler.execute(this::updateData);
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public void addPlane(ForecastSolarPlaneHandler planeHandler) {
        logger.trace("Adding plane {}", planeHandler.getThing().getLabel());
        sequentialScheduler.execute(() -> planes.addIfAbsent(planeHandler));
        sequentialScheduler.execute(this::updateData);
    }

    public void removePlane(ForecastSolarPlaneHandler planeHandler) {
        logger.trace("Removing plane {}", planeHandler.getThing().getLabel());
        sequentialScheduler.execute(() -> planes.remove(planeHandler));
    }

    /**
     * #####################
     * Forecast functionality
     * #####################
     */

    /**
     * Callback of refreshJob to update all data, nowhere else called
     * 1) Check for planes
     * 2) Update all planes
     * 3) Update channels
     * 4) Update timeseries if needed
     */
    protected void updateData() {
        // 1) check if there are planes attached return immediately if not
        if (planes.isEmpty()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                    "@text/solarforecast.site.status.no-planes");
            return;
        }
        // 2) all planes update their data, dirty flags inside each handler is set if new forecast was fetched
        if (!isInCalmDownPeriod()) {
            planes.forEach(planeHandler -> {
                planeHandler.updateData();
            });
        }
        try {
            // 3) update channels each time with actual data
            updateChannels();
            // 4) only update timeseries if bridge or any plane indicates that an update is needed
            if (updateTimeseriesNeeded.getAndSet(false)
                    || planes.stream().anyMatch(plane -> plane.isTimeseriesUpdateNeeded())) {
                updateTimeseries();
            }
        } catch (SolarForecastException sfe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "@text/solarforecast.site.status.exception [\"" + sfe.getMessage() + "\"]");
        }
    }

    /**
     * Update the actual channels from all planes.
     */
    protected void updateChannels() {
        double energySum = 0;
        double powerSum = 0;
        double daySum = 0;
        List<ForecastSolarObject> forecastObjects = getForecasts();
        ZonedDateTime now = Utils.getZdtFromUTC(Utils.now());
        for (ForecastSolarObject forecast : forecastObjects) {
            energySum += forecast.getActualEnergyValue(now);
            powerSum += forecast.getActualPowerValue(now);
            daySum += forecast.getDayTotal(now.toLocalDate());
        }
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energySum));
        // during unit tests there's the possibility of slight negative values when adding up sums
        // 2026-01-05 22:03:55.147 [TRACE] [r.handler.ForecastSolarBridgeHandler] - Actual 1.0039800206714415 Day
        // 1.0039800206714413 Diff -2.220446049250313E-16
        // avoid negative remaining energy due to rounding issues
        double remainingEnergy = Math.max(0, daySum - energySum);
        updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(remainingEnergy));
        updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
        updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(powerSum));
    }

    /**
     * Update of the forecasted timeseries from all planes
     */
    protected void updateTimeseries() {
        TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<>();
        TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<>();
        List<SolarForecast> forecastObjects = getSolarForecasts();
        // bugfix: https://github.com/weymann/OH3-SolarForecast-Drops/issues/5
        // find common start and end time which fits to all forecast objects to avoid ambiguous values
        final Instant commonStart = Utils.getCommonStartTime(forecastObjects);
        final Instant commonEnd = Utils.getCommonEndTime(forecastObjects);
        for (SolarForecast fo : forecastObjects) {
            TimeSeries powerTS = fo.getPowerTimeSeries(QueryMode.Average);
            Utils.addAll(combinedPowerForecast, powerTS, commonStart, commonEnd);
            TimeSeries energyTS = fo.getEnergyTimeSeries(QueryMode.Average);
            Utils.addAll(combinedEnergyForecast, energyTS, commonStart, commonEnd);
        }
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, Utils.toTimeseries(combinedPowerForecast));
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, Utils.toTimeseries(combinedEnergyForecast));
    }

    public List<ForecastSolarObject> getForecasts() {
        return planes.stream().map(plane -> plane.getForecast()).toList();
    }

    private boolean isInCalmDownPeriod() {
        if (calmDownEnd.isAfter(Utils.now())) {
            // wait until calm down time is expired
            long minutes = Duration.between(Utils.now(), calmDownEnd).toMinutes();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/solarforecast.site.status.calmdown [\"" + minutes + "\"]");
            return true;
        }
        return false;
    }

    public void calmDown() {
        calmDownEnd = Utils.now().plus(CALM_DOWN_TIME_MINUTES, ChronoUnit.MINUTES);
    }

    /**
     * #####################
     * Helper functionality
     * #####################
     */

    private PointType location() {
        PointType localHomeLocation = homeLocation;
        if (localHomeLocation == null) {
            throw new IllegalStateException("Location is not set");
        }
        return localHomeLocation;
    }

    ScheduledExecutorService getSequentialScheduler() {
        return sequentialScheduler;
    }

    /**
     * #####################
     * URL functionality
     * #####################
     */

    /**
     * Calculates base URL for API access with
     * - api key if available
     * - latitude and longitude from location
     *
     * @return base URL as String
     */
    public String getBaseUrl() {
        String url = BASE_URL;
        if (!configuration.apiKey.isBlank()) {
            url += configuration.apiKey + SLASH;
        }
        return url + "estimate/" + location().getLatitude() + SLASH + location().getLongitude() + SLASH;
    }

    /**
     * Helper function to add inverter kWp parameter if configured
     *
     * @param Mutable map of parameters to add inverter kWp
     */
    void queryParameters(Map<String, String> parameters) {
        if (configuration.inverterKwp != Double.MAX_VALUE) {
            parameters.put("inverter", String.valueOf(configuration.inverterKwp));
        }
    }

    /**
     * #####################
     * Actions functionality
     * #####################
     */

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public List<SolarForecast> getSolarForecasts() {
        return planes.stream().flatMap(plane -> plane.getSolarForecasts().stream()).toList();
    }
}
