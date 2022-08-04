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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SolcastBridgeHandler.class);
    private List<SolcastPlaneHandler> parts = new ArrayList<SolcastPlaneHandler>();
    private Optional<SolcastBridgeConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public SolcastBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        SolcastBridgeConfiguration config = getConfigAs(SolcastBridgeConfiguration.class);
        configuration = Optional.of(config);
        if (!EMPTY.equals(config.apiKey)) {
            updateStatus(ThingStatus.ONLINE);
            getData();
            startSchedule(configuration.get().channelRefreshInterval);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key is mandatory");
            logger.info("API Key missing");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void startSchedule(int interval) {
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
        });
    }

    /**
     * Get data for all planes. Protect parts map from being modified during update
     */
    private synchronized void getData() {
        if (parts.isEmpty()) {
            logger.debug("No PV plane defined yet");
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(SolcastConstants.zonedId);
        double actualSum = 0;
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
            actualSum += fo.getActualValue(now);
            remainSum += fo.getRemainingProduction(now);
            todaySum += fo.getDayTotal(now, 0);
            day1Sum += fo.getDayTotal(now, 1);
            day1SumLow += fo.getPessimisticDayTotal(now, 1);
            day1SumHigh += fo.getOptimisticDayTotal(now, 1);
            day2Sum += fo.getDayTotal(now, 2);
            day2SumLow += fo.getPessimisticDayTotal(now, 2);
            day2SumHigh += fo.getOptimisticDayTotal(now, 2);
            day3Sum += fo.getDayTotal(now, 3);
            day3SumLow += fo.getPessimisticDayTotal(now, 3);
            day3SumHigh += fo.getOptimisticDayTotal(now, 3);
            day4Sum += fo.getDayTotal(now, 4);
            day4SumLow += fo.getPessimisticDayTotal(now, 4);
            day4SumHigh += fo.getOptimisticDayTotal(now, 4);
            day5Sum += fo.getDayTotal(now, 5);
            day5SumLow += fo.getPessimisticDayTotal(now, 5);
            day5SumHigh += fo.getOptimisticDayTotal(now, 5);
            day6Sum += fo.getDayTotal(now, 6);
            day6SumLow += fo.getPessimisticDayTotal(now, 6);
            day6SumHigh += fo.getOptimisticDayTotal(now, 6);
        }
        updateState(CHANNEL_ACTUAL, SolcastObject.getStateObject(actualSum));
        updateState(CHANNEL_REMAINING, SolcastObject.getStateObject(remainSum));
        updateState(CHANNEL_TODAY, SolcastObject.getStateObject(todaySum));
        updateState(CHANNEL_DAY1, SolcastObject.getStateObject(day1Sum));
        updateState(CHANNEL_DAY1_HIGH, SolcastObject.getStateObject(day1SumHigh));
        updateState(CHANNEL_DAY1_LOW, SolcastObject.getStateObject(day1SumLow));
        updateState(CHANNEL_DAY2, SolcastObject.getStateObject(day2Sum));
        updateState(CHANNEL_DAY2_HIGH, SolcastObject.getStateObject(day2SumHigh));
        updateState(CHANNEL_DAY2_LOW, SolcastObject.getStateObject(day2SumLow));
        updateState(CHANNEL_DAY3, SolcastObject.getStateObject(day3Sum));
        updateState(CHANNEL_DAY3_HIGH, SolcastObject.getStateObject(day3SumHigh));
        updateState(CHANNEL_DAY3_LOW, SolcastObject.getStateObject(day3SumLow));
        updateState(CHANNEL_DAY4, SolcastObject.getStateObject(day4Sum));
        updateState(CHANNEL_DAY4_HIGH, SolcastObject.getStateObject(day4SumHigh));
        updateState(CHANNEL_DAY4_LOW, SolcastObject.getStateObject(day4SumLow));
        updateState(CHANNEL_DAY5, SolcastObject.getStateObject(day5Sum));
        updateState(CHANNEL_DAY5_HIGH, SolcastObject.getStateObject(day5SumHigh));
        updateState(CHANNEL_DAY5_LOW, SolcastObject.getStateObject(day5SumLow));
        updateState(CHANNEL_DAY6, SolcastObject.getStateObject(day6Sum));
        updateState(CHANNEL_DAY6_HIGH, SolcastObject.getStateObject(day6SumHigh));
        updateState(CHANNEL_DAY6_LOW, SolcastObject.getStateObject(day6SumLow));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    synchronized void addPlane(SolcastPlaneHandler sph) {
        parts.add(sph);
        getData();
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
}
