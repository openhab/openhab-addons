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
package org.openhab.binding.solarforecast.internal.forecastsolar;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ForecastSolarBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ForecastSolarBridgeHandler.class);
    private final PointType homeLocation;

    private List<ForecastSolarPlaneHandler> parts = new ArrayList<ForecastSolarPlaneHandler>();
    private Optional<ForecastSolarConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public ForecastSolarBridgeHandler(Bridge bridge, PointType location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public void initialize() {
        ForecastSolarConfiguration config = getConfigAs(ForecastSolarConfiguration.class);
        if (config.location.equals(SolarForecastBindingConstants.AUTODETECT)) {
            Configuration editConfig = editConfiguration();
            editConfig.put("location", homeLocation.toString());
            updateConfiguration(editConfig);
            config = getConfigAs(ForecastSolarConfiguration.class);
        }
        configuration = Optional.of(config);
        updateStatus(ThingStatus.ONLINE);
        getData();
        startSchedule(1);
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
            logger.info("No plane defined yet");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        double actualSum = 0;
        double remainSum = 0;
        double todaySum = 0;
        double tomorrowSum = 0;
        double day2Sum = 0;
        double day3Sum = 0;
        for (Iterator<ForecastSolarPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            ForecastSolarPlaneHandler sfph = iterator.next();
            ForecastSolarObject fo = sfph.fetchData();
            if (fo.isValid()) {
                actualSum += fo.getActualValue(now);
                remainSum += fo.getRemainingProduction(now);
                todaySum += fo.getDayTotal(now, 0);
                tomorrowSum += fo.getDayTotal(now, 1);
                day2Sum += fo.getDayTotal(now, 2);
                day3Sum += fo.getDayTotal(now, 3);
            } else {
                logger.info("Fetched data not valid {}", fo.toString());
            }
        }
        logger.info("Remain: {}", remainSum);
        updateState(CHANNEL_REMAINING, SolcastObject.getStateObject(remainSum));
        updateState(CHANNEL_ACTUAL, SolcastObject.getStateObject(actualSum));
        updateState(CHANNEL_TODAY, SolcastObject.getStateObject(todaySum));
        updateState(CHANNEL_TOMORROW, SolcastObject.getStateObject(tomorrowSum));
        updateState(CHANNEL_DAY2, SolcastObject.getStateObject(day2Sum));
        updateState(CHANNEL_DAY3, SolcastObject.getStateObject(day3Sum));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    synchronized void addPlane(ForecastSolarPlaneHandler sfph) {
        parts.add(sfph);
        // update passive PV plane with necessary data
        if (configuration.isPresent()) {
            sfph.setLocation(new PointType(configuration.get().location));
            if (!EMPTY.equals(configuration.get().apiKey)) {
                sfph.setApiKey(configuration.get().apiKey);
            }
        }
        getData();
    }

    synchronized void removePlane(ForecastSolarPlaneHandler sfph) {
        parts.remove(sfph);
    }
}
