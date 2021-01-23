/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class E3DCHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCHandler.class);

    private @Nullable E3DCConfiguration config;
    private @Nullable ScheduledFuture<?> readDataJob;
    private @Nullable E3DCConnector e3dcconnect;

    public E3DCHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * if (CHANNEL_1.equals(channelUID.getId())) {
         * if (command instanceof RefreshType) {
         * // TODO: handle data refresh
         * }
         *
         * // TODO: handle command
         *
         * // Note: if communication with thing fails for some reason,
         * // indicate that by setting the status with detail information:
         * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
         * // "Could not control device at IP address x.x.x.x");
         * }
         */
    }

    @Override
    public void initialize() {
        config = getConfigAs(E3DCConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            e3dcconnect = new E3DCConnector(this, config);
            updateStatus(ThingStatus.ONLINE);
            scheduleReadDataJob();
        });
    }

    private void scheduleReadDataJob() {
        int readDataInterval = config.getUpdateinterval();
        // Ensure that the request is finished
        if (readDataInterval < 5) {
            readDataInterval = 5;
        }

        logger.debug("Data table request interval {} seconds", readDataInterval);

        readDataJob = scheduler.scheduleWithFixedDelay(() -> {
            e3dcconnect.requestE3DCData();
        }, 0, readDataInterval, TimeUnit.SECONDS);
    }

    private void cancelReadDataJob() {
        if (readDataJob != null) {
            if (!readDataJob.isDone()) {
                readDataJob.cancel(true);
                logger.debug("Scheduled data table requests cancelled");
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        cancelReadDataJob();
        e3dcconnect.close();
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    @Override
    protected void updateState(String strChannelName, State dt) {
        super.updateState(strChannelName, dt);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail, @Nullable String reason) {
        super.updateStatus(ts, statusDetail, reason);
    }

    @Override
    protected void updateStatus(ThingStatus ts, ThingStatusDetail statusDetail) {
        super.updateStatus(ts, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);
    }
}
