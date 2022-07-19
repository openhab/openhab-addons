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
package org.openhab.binding.solarforecast.internal;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link SolarForecastBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SolarForecastBridgeHandler.class);
    private final PointType homeLocation;

    private List<SolarForecastPlaneHandler> parts = new ArrayList<SolarForecastPlaneHandler>();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private @Nullable SolarForecastConfiguration config;

    public SolarForecastBridgeHandler(Bridge bridge, PointType location) {
        super(bridge);
        homeLocation = location;
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarForecastConfiguration.class);
        startSchedule(config.refreshInterval);
        if (config.location.equals(SolarForecastBindingConstants.AUTODETECT)) {
            Configuration editConfig = editConfiguration();
            editConfig.put("location", homeLocation.toString());
            updateConfiguration(editConfig);
            config = getConfigAs(SolarForecastConfiguration.class);
        }
        updateStatus(ThingStatus.ONLINE);
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
        LocalDateTime now = LocalDateTime.now();
        double todaySum = 0;
        double tomorrowSum = 0;
        double actualSum = 0;
        double remainSum = 0;
        for (Iterator<SolarForecastPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            ForecastObject fo = iterator.next().fetchData();
            if (fo.isValid()) {
                todaySum += fo.getDayTotal(now, 0);
                tomorrowSum = fo.getDayTotal(now, 1);
                actualSum = fo.getActualValue(now);
                remainSum = fo.getRemainingProduction(now);
            } else {
                logger.info("Fetched data not valid {}", fo.toString());
            }
        }
        updateState(CHANNEL_TODAY, ForecastObject.getStateObject(todaySum));
        updateState(CHANNEL_TOMORROW, ForecastObject.getStateObject(tomorrowSum));
        updateState(CHANNEL_REMAINING, ForecastObject.getStateObject(remainSum));
        updateState(CHANNEL_ACTUAL, ForecastObject.getStateObject(actualSum));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    synchronized void addPlane(SolarForecastPlaneHandler sfph) {
        parts.add(sfph);
    }

    synchronized void removePlane(SolarForecastPlaneHandler sfph) {
        parts.remove(sfph);
    }

    public PointType getLocation() {
        return PointType.valueOf(config.location);
    }
}
