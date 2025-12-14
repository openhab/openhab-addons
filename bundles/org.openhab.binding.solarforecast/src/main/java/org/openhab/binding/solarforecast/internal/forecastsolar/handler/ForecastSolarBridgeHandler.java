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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.core.types.TimeSeries.Policy;
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

    private ForecastSolarBridgeConfiguration configuration = new ForecastSolarBridgeConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable PointType homeLocation;
    private Instant calmDownEnd = Instant.MIN;

    protected ScheduledExecutorService sequentialScheduler = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService(BINDING_ID, null);
    protected CopyOnWriteArrayList<ForecastSolarPlaneHandler> planes = new CopyOnWriteArrayList<>();

    public ForecastSolarBridgeHandler(Bridge bridge, @Nullable PointType location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

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
                    updateData();
                    break;
                case CHANNEL_POWER_ESTIMATE:
                case CHANNEL_ENERGY_ESTIMATE:
                    requestForecastUpdate();
                    break;
            }
        }
    }

    protected void updateData() {
        if (planes.isEmpty()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                    "@text/solarforecast.site.status.no-planes");
            return;
        }
        if (calmDownEnd.isAfter(Utils.now())) {
            // wait until calm down time is expired
            long minutes = Duration.between(Utils.now(), calmDownEnd).toMinutes();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/solarforecast.site.status.calmdown [\"" + minutes + "\"]");
            return;
        }
        boolean update = true;
        double energySum = 0;
        double powerSum = 0;
        double daySum = 0;
        for (Iterator<ForecastSolarPlaneHandler> planeIter = planes.iterator(); planeIter.hasNext();) {
            try {
                ForecastSolarObject fo = planeIter.next().getData();
                ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
                energySum += fo.getActualEnergyValue(now);
                powerSum += fo.getActualPowerValue(now);
                daySum += fo.getDayTotal(now.toLocalDate());
            } catch (SolarForecastException sfe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/solarforecast.site.status.exception [\"" + sfe.getMessage() + "\"]");
                update = false;
            }
        }
        if (update) {
            updateStatus(ThingStatus.ONLINE);
            updateState(CHANNEL_ENERGY_ACTUAL, Utils.getEnergyState(energySum));
            updateState(CHANNEL_ENERGY_REMAIN, Utils.getEnergyState(daySum - energySum));
            updateState(CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
            updateState(CHANNEL_POWER_ACTUAL, Utils.getPowerState(powerSum));
        }
    }

    public void requestForecastUpdate() {
        sequentialScheduler.execute(this::forecastUpdate);
    }

    protected void forecastUpdate() {
        if (planes.isEmpty()) {
            return;
        }
        TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<>();
        TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<>();
        List<SolarForecast> forecastObjects = new ArrayList<>();
        for (Iterator<ForecastSolarPlaneHandler> planeIter = planes.iterator(); planeIter.hasNext();) {
            forecastObjects.addAll(planeIter.next().getSolarForecasts());
        }
        // bugfix: https://github.com/weymann/OH3-SolarForecast-Drops/issues/5
        // find common start and end time which fits to all forecast objects to avoid ambiguous values
        final Instant commonStart = Utils.getCommonStartTime(forecastObjects);
        final Instant commonEnd = Utils.getCommonEndTime(forecastObjects);
        forecastObjects.forEach(fc -> {
            TimeSeries powerTS = fc.getPowerTimeSeries(QueryMode.Average);
            powerTS.getStates().forEach(entry -> {
                if (Utils.isAfterOrEqual(entry.timestamp(), commonStart)
                        && Utils.isBeforeOrEqual(entry.timestamp(), commonEnd)) {
                    Utils.addState(combinedPowerForecast, entry);
                }
            });
            TimeSeries energyTS = fc.getEnergyTimeSeries(QueryMode.Average);
            energyTS.getStates().forEach(entry -> {
                if (Utils.isAfterOrEqual(entry.timestamp(), commonStart)
                        && Utils.isBeforeOrEqual(entry.timestamp(), commonEnd)) {
                    Utils.addState(combinedEnergyForecast, entry);
                }
            });
        });

        TimeSeries powerSeries = new TimeSeries(Policy.REPLACE);
        combinedPowerForecast.forEach((timestamp, state) -> {
            powerSeries.add(timestamp, state);
        });
        sendTimeSeries(CHANNEL_POWER_ESTIMATE, powerSeries);

        TimeSeries energySeries = new TimeSeries(Policy.REPLACE);
        combinedEnergyForecast.forEach((timestamp, state) -> {
            energySeries.add(timestamp, state);
        });
        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, energySeries);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }

    public void addPlane(ForecastSolarPlaneHandler sfph) {
        logger.trace("Adding plane {}", sfph.getThing().getUID());
        planes.add(sfph);
        sequentialScheduler.execute(this::updateData);
    }

    public void removePlane(ForecastSolarPlaneHandler sfph) {
        logger.trace("Removing plane {}", sfph.getThing().getUID());
        planes.remove(sfph);
    }

    @Override
    public List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        for (Iterator<ForecastSolarPlaneHandler> planeIter = planes.iterator(); planeIter.hasNext();) {
            l.addAll(planeIter.next().getSolarForecasts());
        }
        return l;
    }

    public void calmDown() {
        calmDownEnd = Utils.now().plus(CALM_DOWN_TIME_MINUTES, ChronoUnit.MINUTES);
    }

    public String getBaseUrl() {
        String url = BASE_URL;
        if (!configuration.apiKey.isBlank()) {
            url += configuration.apiKey + SLASH;
        }
        return url + "estimate/" + location().getLatitude() + SLASH + location().getLongitude() + SLASH;
    }

    private PointType location() {
        PointType localHomeLocation = homeLocation;
        if (localHomeLocation == null) {
            throw new IllegalStateException("Location is not set");
        }
        return localHomeLocation;
    }

    Map<String, String> queryParameters(Map<String, String> parameters) {
        if (configuration.inverterKwp != Double.MAX_VALUE) {
            parameters.put("inverter", String.valueOf(configuration.inverterKwp));
        }
        return parameters;
    }
}
