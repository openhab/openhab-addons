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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link SolcastBridgeHandler} is a non active handler instance. It will be triggered by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider, TimeZoneProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastBridgeHandler.class);

    private List<SolcastPlaneHandler> planes = new ArrayList<>();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private SolcastBridgeConfiguration configuration = new SolcastBridgeConfiguration();
    private ZoneId timeZone;

    public SolcastBridgeHandler(Bridge bridge, TimeZoneProvider tzp) {
        super(bridge);
        timeZone = tzp.getTimeZone();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastBridgeConfiguration.class);
        if (!configuration.apiKey.isBlank()) {
            if (!configuration.timeZone.isBlank()) {
                try {
                    timeZone = ZoneId.of(configuration.timeZone);
                } catch (DateTimeException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.site.status.timezone" + " [\"" + configuration.timeZone + "\"]");
                    return;
                }
            }
            updateStatus(ThingStatus.UNKNOWN);
            refreshJob = Optional
                    .of(scheduler.scheduleWithFixedDelay(this::getData, 0, REFRESH_ACTUAL_INTERVAL, TimeUnit.MINUTES));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.site.status.api-key-missing");
        }
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

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    /**
     * Get data for all planes. Protect plane list from being modified during update
     */
    public synchronized void getData() {
        if (planes.isEmpty()) {
            logger.debug("No PV plane defined yet");
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(getTimeZone());
        List<QueryMode> modes = List.of(QueryMode.Average, QueryMode.Pessimistic, QueryMode.Optimistic);
        modes.forEach(mode -> {
            String group = switch (mode) {
                case Average -> GROUP_AVERAGE;
                case Optimistic -> GROUP_OPTIMISTIC;
                case Pessimistic -> GROUP_PESSIMISTIC;
                default -> GROUP_AVERAGE;
            };
            boolean update = true;
            double energySum = 0;
            double powerSum = 0;
            double daySum = 0;
            for (Iterator<SolcastPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
                try {
                    SolcastPlaneHandler sfph = iterator.next();
                    SolcastObject fo = sfph.fetchData();
                    energySum += fo.getActualEnergyValue(now, mode);
                    powerSum += fo.getActualPowerValue(now, mode);
                    daySum += fo.getDayTotal(now.toLocalDate(), mode);
                } catch (SolarForecastException sfe) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/solarforecast.site.status.exception [\"" + sfe.getMessage() + "\"]");
                    update = false;
                }
            }
            if (update) {
                updateStatus(ThingStatus.ONLINE);
                updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                        Utils.getEnergyState(energySum));
                updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                        Utils.getEnergyState(daySum - energySum));
                updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY,
                        Utils.getEnergyState(daySum));
                updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                        Utils.getPowerState(powerSum));
            }
        });
    }

    public synchronized void forecastUpdate() {
        if (planes.isEmpty()) {
            return;
        }
        // get all available forecasts
        List<SolarForecast> forecastObjects = new ArrayList<>();
        for (Iterator<SolcastPlaneHandler> iterator = planes.iterator(); iterator.hasNext();) {
            SolcastPlaneHandler sfph = iterator.next();
            forecastObjects.addAll(sfph.getSolarForecasts());
        }
        // sort in Tree according to times for each scenario
        List<QueryMode> modes = List.of(QueryMode.Average, QueryMode.Pessimistic, QueryMode.Optimistic);
        modes.forEach(mode -> {
            TreeMap<Instant, QuantityType<?>> combinedPowerForecast = new TreeMap<>();
            TreeMap<Instant, QuantityType<?>> combinedEnergyForecast = new TreeMap<>();

            // bugfix: https://github.com/weymann/OH3-SolarForecast-Drops/issues/5
            // find common start and end time which fits to all forecast objects to avoid ambiguous values
            final Instant commonStart = Utils.getCommonStartTime(forecastObjects);
            final Instant commonEnd = Utils.getCommonEndTime(forecastObjects);
            forecastObjects.forEach(fc -> {
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
            });
            // create TimeSeries and distribute
            TimeSeries powerSeries = new TimeSeries(Policy.REPLACE);
            combinedPowerForecast.forEach((timestamp, state) -> {
                powerSeries.add(timestamp, state);
            });

            TimeSeries energySeries = new TimeSeries(Policy.REPLACE);
            combinedEnergyForecast.forEach((timestamp, state) -> {
                energySeries.add(timestamp, state);
            });
            switch (mode) {
                case Average:
                    sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                            energySeries);
                    sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                            powerSeries);
                    break;
                case Optimistic:
                    sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                            energySeries);
                    sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                            powerSeries);
                    break;
                case Pessimistic:
                    sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                            energySeries);
                    sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                            powerSeries);
                    break;
                default:
                    break;
            }
        });
    }

    public synchronized void addPlane(SolcastPlaneHandler sph) {
        planes.add(sph);
    }

    public synchronized void removePlane(SolcastPlaneHandler sph) {
        planes.remove(sph);
    }

    String getApiKey() {
        return configuration.apiKey;
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<>();
        planes.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }

    @Override
    public ZoneId getTimeZone() {
        return timeZone;
    }
}
