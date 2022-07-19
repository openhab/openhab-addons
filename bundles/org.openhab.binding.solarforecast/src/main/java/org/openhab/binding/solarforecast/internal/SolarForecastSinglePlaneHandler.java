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

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;

/**
 * The {@link SolarForecastBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastSinglePlaneHandler extends SolarForecastPlaneHandler {
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private PointType homeLocation;
    private @Nullable SolarForecastConfiguration config;

    public SolarForecastSinglePlaneHandler(Thing thing, HttpClient httpClient, PointType location) {
        super(thing, httpClient);
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
            super.setLocation(homeLocation);
            config = getConfigAs(SolarForecastConfiguration.class);
        } else {
            super.setLocation(PointType.valueOf(config.location));
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

    private void getData() {
        super.fetchData();
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }
}
