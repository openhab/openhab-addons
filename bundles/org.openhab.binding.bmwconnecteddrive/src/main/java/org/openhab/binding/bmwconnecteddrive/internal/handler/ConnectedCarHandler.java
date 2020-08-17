/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.BMWConnectedDriveBindingConstants.CHANNEL_1;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedCarConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.CarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConnectedCarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedCarHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);

    private final ConnectedDrivePortalHandler cdpHandler;
    protected @Nullable ScheduledFuture<?> refreshJob;

    public ConnectedCarHandler(Thing thing, HttpClient hc) {
        super(thing);
        cdpHandler = new ConnectedDrivePortalHandler(hc);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        ConnectedCarConfiguration config = getConfigAs(ConnectedCarConfiguration.class);
        if (config != null) {
            scheduler.execute(() -> {
                cdpHandler.initialize(config);
                startSchedule(config.refreshInterval);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void startSchedule(int interval) {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            if (localRefreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::dataUpdate, 0, interval, TimeUnit.MINUTES);
            } // else - scheduler is already running!
        } else {
            refreshJob = scheduler.scheduleWithFixedDelay(this::dataUpdate, 0, interval, TimeUnit.MINUTES);
        }
    }

    protected void dataUpdate() {
        CarData carData = cdpHandler.getData();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }
}
