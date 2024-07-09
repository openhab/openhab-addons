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
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
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

/**
 * The {@link ForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider {
    private static final int CALM_DOWN_TIME_MINUTES = 61;

    private List<ForecastSolarPlaneHandler> planes = new ArrayList<>();
    private Optional<PointType> homeLocation;
    private Optional<ForecastSolarBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Instant calmDownEnd = Instant.MIN;

    public ForecastSolarBridgeHandler(Bridge bridge, Optional<PointType> location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        ForecastSolarBridgeConfiguration config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        PointType locationConfigured;

        // handle location error cases
        if (config.location.isBlank()) {
            if (homeLocation.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/solarforecast.site.status.location-missing");
                return;
            } else {
                locationConfigured = homeLocation.get();
                // continue with openHAB location
            }
        } else {
            try {
                locationConfigured = new PointType(config.location);
                // continue with location from configuration
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }
        Configuration editConfig = editConfiguration();
        editConfig.put("location", locationConfigured.toString());
        updateConfiguration(editConfig);
        config = getConfigAs(ForecastSolarBridgeConfiguration.class);
        configuration = Optional.of(config);
        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::getData, 0, REFRESH_ACTUAL_INTERVAL, TimeUnit.MINUTES));
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
                    getData();
                    break;
                case CHANNEL_POWER_ESTIMATE:
                case CHANNEL_ENERGY_ESTIMATE:
                    forecastUpdate();
                    break;
            }
        }
    }

    /**
     * Get data for all planes. Synchronized to protect plane list from being modified during update
     */
    private synchronized void getData() {
        if (planes.isEmpty()) {
            return;
        }
        if (calmDownEnd.isAfter(Instant.now(Utils.getClock()))) {
            // wait until calm down time is expired
            long minutes = Duration.between(Instant.now(Utils.getClock()), calmDownEnd).toMinutes();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/solarforecast.site.status.calmdown [\"" + minutes + "\"]");
            return;
        }
        boolean update = true;
        double energySum = 0;
        double powerSum = 0;
        double daySum = 0;
        for (Iterator<ForecastSolarPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            try {
                ForecastSolarPlaneHandler sfph = iterator.next();
                ForecastSolarObject fo = sfph.fetchData();
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

    public synchronized void forecastUpdate() {
        if (planes.isEmpty()) {
            return;
        }
        TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<>();
        TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<>();
        List<SolarForecast> forecastObjects = new ArrayList<>();
        for (Iterator<ForecastSolarPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            ForecastSolarPlaneHandler sfph = iterator.next();
            forecastObjects.addAll(sfph.getSolarForecasts());
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
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    public synchronized void addPlane(ForecastSolarPlaneHandler sfph) {
        planes.add(sfph);
        // update passive PV plane with necessary data
        if (configuration.isPresent()) {
            sfph.setLocation(new PointType(configuration.get().location));
            if (!configuration.get().apiKey.isBlank()) {
                sfph.setApiKey(configuration.get().apiKey);
            }
        }
        getData();
    }

    public synchronized void removePlane(ForecastSolarPlaneHandler sfph) {
        planes.remove(sfph);
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        planes.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }

    public void calmDown() {
        calmDownEnd = Instant.now(Utils.getClock()).plus(CALM_DOWN_TIME_MINUTES, ChronoUnit.MINUTES);
    }
}
