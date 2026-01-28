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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.MODES;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * The {@link SolcastBridgeHandler} is a non active handler instance. It will be triggered by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Move to sequential scheduler
 *
 */
@NonNullByDefault
public class SolcastBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider {
    private final ScheduledExecutorService sequentialScheduler = ThreadPoolManager
            .getPoolBasedSequentialScheduledExecutorService(BINDING_ID, "thingHandler");
    private final CopyOnWriteArrayList<SolcastPlaneHandler> planes = new CopyOnWriteArrayList<>();

    private SolcastBridgeConfiguration configuration = new SolcastBridgeConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;

    public SolcastBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastBridgeConfiguration.class);
        if (!configuration.apiKey.isBlank()) {
            updateStatus(ThingStatus.UNKNOWN);
            refreshJob = sequentialScheduler.scheduleWithFixedDelay(this::updateData, 0, REFRESH_ACTUAL_INTERVAL,
                    TimeUnit.MINUTES);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.site.status.api-key-missing");
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

    public void addPlane(SolcastPlaneHandler sph) {
        sequentialScheduler.execute(() -> planes.addIfAbsent(sph));
        sequentialScheduler.execute(() -> updateData());
    }

    public void removePlane(SolcastPlaneHandler sph) {
        sequentialScheduler.execute(() -> planes.remove(sph));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            switch (channel) {
                case CHANNEL_ENERGY_ACTUAL, CHANNEL_ENERGY_REMAIN, CHANNEL_ENERGY_TODAY, CHANNEL_POWER_ACTUAL,
                        CHANNEL_API_COUNT, CHANNEL_LATEST_UPDATE -> {
                    sequentialScheduler.execute(() -> updateData());
                }
                case CHANNEL_POWER_ESTIMATE, CHANNEL_ENERGY_ESTIMATE -> {
                    sequentialScheduler.execute(() -> updateTimeseries());
                }
            }
        }
    }

    /**
     * Get data for all planes. Protect plane list from being modified during update
     */
    public void updateData() {
        // 1) check if there are planes attached return immediately if not
        if (planes.isEmpty()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NOT_YET_READY,
                    "@text/solarforecast.site.status.no-planes");
            return;
        }
        // 2) all planes update their data, dirty flags inside each handler is set if new forecast was fetched
        planes.forEach(planeHandler -> {
            planeHandler.updateData();
        });
        try {
            // 3) update channels each time with actual data
            updateChannels();
            // 4) only update timeseries if bridge or any plane indicates that an update is needed
            boolean bridgeNeedsUpdate = planes.stream().anyMatch(plane -> plane.isTimeseriesUpdateNeeded());
            if (bridgeNeedsUpdate) {
                updateTimeseries();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (SolarForecastException sfe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "@text/solarforecast.site.status.exception [\"" + sfe.getMessage() + "\"]");
        }
    }

    protected void updateChannels() {
        ZonedDateTime now = ZonedDateTime.now(Utils.getClock());
        List<SolarForecast> forecastList = getSolarForecasts();

        // update counters and latest update
        Instant latestUpdate = Instant.MIN;
        for (SolarForecast forecastIterator : forecastList) {
            if (latestUpdate.isBefore(forecastIterator.getCreationInstant())) {
                latestUpdate = forecastIterator.getCreationInstant();
            }
        }
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_LATEST_UPDATE,
                new DateTimeType(latestUpdate));
        JSONObject totalCounter = new JSONObject("{\"200\":0,\"429\":0,\"other\":0}");
        planes.forEach(plane -> addCounter(totalCounter, plane.getCounter()));
        updateState(GROUP_UPDATE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_API_COUNT,
                StringType.valueOf(totalCounter.toString()));

        // loop on all modes
        MODES.forEach(mode -> {
            double energySum = 0;
            double powerSum = 0;
            double daySum = 0;
            for (SolarForecast forecastIterator : forecastList) {
                energySum += forecastIterator.getEnergy(Utils.startOfDayInstant(), now.toInstant(), mode.toString())
                        .doubleValue();
                powerSum += forecastIterator.getPower(now.toInstant(), mode.toString()).doubleValue();
                daySum += forecastIterator.getDay(now.toLocalDate(), mode.toString()).doubleValue();
            }
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                    Utils.getEnergyState(energySum));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                    Utils.getEnergyState(daySum - energySum));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY, Utils.getEnergyState(daySum));
            updateState(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                    Utils.getPowerState(powerSum));
        });
    }

    protected void updateTimeseries() {
        List<SolarForecast> forecastList = getSolarForecasts();
        // sort in Tree according to times for each scenario
        MODES.forEach(mode -> {
            TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<>();
            TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<>();

            // bugfix: https://github.com/weymann/OH3-SolarForecast-Drops/issues/5
            // find common start and end time which fits to all forecast objects to avoid ambiguous values
            final Instant commonStart = Utils.getCommonStartTime(forecastList);
            final Instant commonEnd = Utils.getCommonEndTime(forecastList);
            for (SolarForecast fc : forecastList) {
                TimeSeries powerTS = fc.getPowerTimeSeries(mode);
                powerTS.getStates().forEach(entry -> {
                    if (Utils.isAfterOrEqual(entry.timestamp(), commonStart)
                            && Utils.isBeforeOrEqual(entry.timestamp(), commonEnd)) {
                        Utils.addState(combinedPowerForecast, entry);
                    }
                });
                TimeSeries energyTS = fc.getEnergyTimeSeries(mode);
                energyTS.getStates().forEach(entry -> {
                    if (Utils.isAfterOrEqual(entry.timestamp(), commonStart)
                            && Utils.isBeforeOrEqual(entry.timestamp(), commonEnd)) {
                        Utils.addState(combinedEnergyForecast, entry);
                    }
                });
            }
            // create TimeSeries and distribute
            TimeSeries powerSeries = new TimeSeries(Policy.REPLACE);
            combinedPowerForecast.forEach((timestamp, state) -> {
                powerSeries.add(timestamp, state);
            });

            TimeSeries energySeries = new TimeSeries(Policy.REPLACE);
            combinedEnergyForecast.forEach((timestamp, state) -> {
                energySeries.add(timestamp, state);
            });
            sendTimeSeries(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE, energySeries);
            sendTimeSeries(mode + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE, powerSeries);
        });
    }

    private void addCounter(JSONObject target, JSONObject source) {
        target.put(HTTP_OK, target.getInt(HTTP_OK) + source.getInt(HTTP_OK));
        target.put(HTTP_TOO_MANY_REQUESTS,
                target.getInt(HTTP_TOO_MANY_REQUESTS) + source.getInt(HTTP_TOO_MANY_REQUESTS));
        target.put(HTTP_OTHER, target.getInt(HTTP_OTHER) + source.getInt(HTTP_OTHER));
    }

    String getApiKey() {
        return configuration.apiKey;
    }

    public ScheduledExecutorService getScheduler() {
        return sequentialScheduler;
    }

    @Override
    public List<SolarForecast> getSolarForecasts() {
        return planes.stream().flatMap(plane -> plane.getSolarForecasts().stream()).toList();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }
}
