/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastBridgeConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeHandler extends BaseBridgeHandler implements SolarForecastProvider, TimeZoneProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastBridgeHandler.class);
    private final TimeZoneProvider localTimeZoneProvider;

    private List<SolcastPlaneHandler> parts = new ArrayList<SolcastPlaneHandler>();
    private Optional<SolcastBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public SolcastBridgeHandler(Bridge bridge, TimeZoneProvider tzp) {
        super(bridge);
        localTimeZoneProvider = tzp;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        SolcastBridgeConfiguration config = getConfigAs(SolcastBridgeConfiguration.class);
        configuration = Optional.of(config);
        if (!EMPTY.equals(config.apiKey)) {
            updateStatus(ThingStatus.ONLINE);
            startSchedule(configuration.get().channelRefreshInterval);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.site.status.api-key-missing");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            getData();
        }
    }

    private void startSchedule(int interval) {
        /**
         * Interval given in minutes so seconds needs multiplier. Wait for 10 seconds until attached planes are created
         * and registered. If now waiting time is defined user will see some glitches e.g. after restart if no or not
         * all planes are initialized
         */
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 10, interval * 60, TimeUnit.SECONDS));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional
                    .of(scheduler.scheduleWithFixedDelay(this::getData, 10, interval * 60, TimeUnit.SECONDS));
        });
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    /**
     * Get data for all planes. Protect parts map from being modified during update
     */
    private synchronized void getData() {
        if (parts.isEmpty()) {
            logger.debug("No PV plane defined yet");
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(localTimeZoneProvider.getTimeZone());
        double actualSum = 0;
        double actualPowerSum = 0;
        double remainSum = 0;
        double todaySum = 0;
        double day1Sum = 0;
        double day1SumLow = 0;
        double day1SumHigh = 0;
        double day2Sum = 0;
        double day2SumLow = 0;
        double day2SumHigh = 0;
        double day3Sum = 0;
        double day3SumLow = 0;
        double day3SumHigh = 0;
        double day4Sum = 0;
        double day4SumLow = 0;
        double day4SumHigh = 0;
        double day5Sum = 0;
        double day5SumLow = 0;
        double day5SumHigh = 0;
        double day6Sum = 0;
        double day6SumLow = 0;
        double day6SumHigh = 0;

        for (Iterator<SolcastPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            SolcastPlaneHandler sfph = iterator.next();
            SolcastObject fo = sfph.fetchData();
            actualSum += fo.getActualValue(now, QueryMode.Estimation);
            actualPowerSum += fo.getActualPowerValue(now, QueryMode.Estimation);
            remainSum += fo.getRemainingProduction(now, QueryMode.Estimation);
            LocalDate nowDate = now.toLocalDate();
            todaySum += fo.getDayTotal(nowDate, QueryMode.Estimation);
            day1Sum += fo.getDayTotal(nowDate.plusDays(1), QueryMode.Estimation);
            day1SumLow += fo.getDayTotal(nowDate.plusDays(1), QueryMode.Pessimistic);
            day1SumHigh += fo.getDayTotal(nowDate.plusDays(1), QueryMode.Optimistic);
            day2Sum += fo.getDayTotal(nowDate.plusDays(2), QueryMode.Estimation);
            day2SumLow += fo.getDayTotal(nowDate.plusDays(2), QueryMode.Pessimistic);
            day2SumHigh += fo.getDayTotal(nowDate.plusDays(2), QueryMode.Optimistic);
            day3Sum += fo.getDayTotal(nowDate.plusDays(3), QueryMode.Estimation);
            day3SumLow += fo.getDayTotal(nowDate.plusDays(3), QueryMode.Pessimistic);
            day3SumHigh += fo.getDayTotal(nowDate.plusDays(3), QueryMode.Optimistic);
            day4Sum += fo.getDayTotal(nowDate.plusDays(4), QueryMode.Estimation);
            day4SumLow += fo.getDayTotal(nowDate.plusDays(4), QueryMode.Pessimistic);
            day4SumHigh += fo.getDayTotal(nowDate.plusDays(4), QueryMode.Optimistic);
            day5Sum += fo.getDayTotal(nowDate.plusDays(5), QueryMode.Estimation);
            day5SumLow += fo.getDayTotal(nowDate.plusDays(5), QueryMode.Pessimistic);
            day5SumHigh += fo.getDayTotal(nowDate.plusDays(5), QueryMode.Optimistic);
            day6Sum += fo.getDayTotal(nowDate.plusDays(6), QueryMode.Estimation);
            day6SumLow += fo.getDayTotal(nowDate.plusDays(6), QueryMode.Pessimistic);
            day6SumHigh += fo.getDayTotal(nowDate.plusDays(6), QueryMode.Optimistic);
        }
        updateState(CHANNEL_ACTUAL, Utils.getEnergyState(actualSum));
        updateState(CHANNEL_ACTUAL_POWER, Utils.getPowerState(actualPowerSum));
        updateState(CHANNEL_REMAINING, Utils.getEnergyState(remainSum));
        updateState(CHANNEL_TODAY, Utils.getEnergyState(todaySum));
        updateState(CHANNEL_DAY1, Utils.getEnergyState(day1Sum));
        updateState(CHANNEL_DAY1_HIGH, Utils.getEnergyState(day1SumHigh));
        updateState(CHANNEL_DAY1_LOW, Utils.getEnergyState(day1SumLow));
        updateState(CHANNEL_DAY2, Utils.getEnergyState(day2Sum));
        updateState(CHANNEL_DAY2_HIGH, Utils.getEnergyState(day2SumHigh));
        updateState(CHANNEL_DAY2_LOW, Utils.getEnergyState(day2SumLow));
        updateState(CHANNEL_DAY3, Utils.getEnergyState(day3Sum));
        updateState(CHANNEL_DAY3_HIGH, Utils.getEnergyState(day3SumHigh));
        updateState(CHANNEL_DAY3_LOW, Utils.getEnergyState(day3SumLow));
        updateState(CHANNEL_DAY4, Utils.getEnergyState(day4Sum));
        updateState(CHANNEL_DAY4_HIGH, Utils.getEnergyState(day4SumHigh));
        updateState(CHANNEL_DAY4_LOW, Utils.getEnergyState(day4SumLow));
        updateState(CHANNEL_DAY5, Utils.getEnergyState(day5Sum));
        updateState(CHANNEL_DAY5_HIGH, Utils.getEnergyState(day5SumHigh));
        updateState(CHANNEL_DAY5_LOW, Utils.getEnergyState(day5SumLow));
        updateState(CHANNEL_DAY6, Utils.getEnergyState(day6Sum));
        updateState(CHANNEL_DAY6_HIGH, Utils.getEnergyState(day6SumHigh));
        updateState(CHANNEL_DAY6_LOW, Utils.getEnergyState(day6SumLow));
    }

    synchronized void addPlane(SolcastPlaneHandler sph) {
        parts.add(sph);
    }

    synchronized void removePlane(SolcastPlaneHandler sph) {
        parts.remove(sph);
    }

    String getApiKey() {
        if (configuration.isPresent()) {
            return configuration.get().apiKey;
        }
        return EMPTY;
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        List<SolarForecast> l = new ArrayList<SolarForecast>();
        parts.forEach(entry -> {
            l.addAll(entry.getSolarForecasts());
        });
        return l;
    }

    @Override
    public ZoneId getTimeZone() {
        if (AUTODETECT.equals(configuration.get().timeZone)) {
            return localTimeZoneProvider.getTimeZone();
        } else {
            try {
                return ZoneId.of(configuration.get().timeZone);
            } catch (DateTimeException e) {
                logger.info("Timezone {} not found {}", configuration.get().timeZone, e.getMessage());
                return localTimeZoneProvider.getTimeZone();
            }
        }
    }
}
