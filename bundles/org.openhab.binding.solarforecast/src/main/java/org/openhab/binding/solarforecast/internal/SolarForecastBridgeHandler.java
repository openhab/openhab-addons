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

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
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

    private List<SolarForecastPlaneHandler> parts = new ArrayList<SolarForecastPlaneHandler>();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private @Nullable SolarForecastConfiguration config;

    public SolarForecastBridgeHandler(Bridge bridge, HttpClient httpClient, PointType location) {
        super(bridge);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarForecastConfiguration.class);
        startSchedule(config.refreshInterval);
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

    private void getData() {
        LocalDateTime now = LocalDateTime.now();
        QuantityType<Energy> today = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        QuantityType<Energy> remain = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        for (Iterator<SolarForecastPlaneHandler> iterator = parts.iterator(); iterator.hasNext();) {
            ForecastObject fo = iterator.next().fetchData();
            if (fo.isValid()) {
                today = today.add(fo.getDayTotal());
                actual = actual.add(fo.getCurrentValue(now));
                remain = remain.add(fo.getRemainingProduction(now));
            } else {
                logger.info("Fetched data not valid {}", fo.toString());
            }
        }
        updateState(CHANNEL_TODAY, today);
        updateState(CHANNEL_REMAINING, remain);
        updateState(CHANNEL_TODAY, today);
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
}
