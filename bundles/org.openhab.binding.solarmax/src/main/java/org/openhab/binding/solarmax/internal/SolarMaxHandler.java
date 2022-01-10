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
package org.openhab.binding.solarmax.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarmax.internal.connector.SolarMaxCommandKey;
import org.openhab.binding.solarmax.internal.connector.SolarMaxConnector;
import org.openhab.binding.solarmax.internal.connector.SolarMaxData;
import org.openhab.binding.solarmax.internal.connector.SolarMaxException;
import org.openhab.core.thing.Channel;
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
 * The {@link SolarMaxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jamie Townsend - Initial contribution
 */
public class SolarMaxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SolarMaxHandler.class);

    @Nullable
    private SolarMaxConfiguration config;

    @Nullable
    private ScheduledFuture<?> pollingJob;

    public SolarMaxHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SolarMax");

        config = getConfigAs(SolarMaxConfiguration.class);

        configurePolling(); // Setup the scheduler
    }

    /**
     * This is called to start the refresh job and also to reset that refresh job when a config change is done.
     */
    private void configurePolling() {
        logger.debug("Polling data from {} at {}:{} every {} seconds ", getThing().getUID(), this.config.host,
                this.config.portNumber, this.config.refreshInterval);
        if (this.config.refreshInterval > 0) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, this.config.refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SolarMax Handler Thing");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
        }
        pollingJob = null;
    }

    /**
     * Polling event used to get data from the SolarMax device
     */
    private Runnable pollingRunnable = () -> {
        updateValuesFromDevice();
    };

    private synchronized void updateValuesFromDevice() {

        logger.debug("Updating data from {} at {}:{} ", getThing().getUID(), this.config.host, this.config.portNumber);
        // get the data from the SolarMax device
        try {
            SolarMaxData solarMaxData = SolarMaxConnector.getAllValuesFromSolarMax(config.host, config.portNumber);

            if (solarMaxData.wasCommunicationSuccessful()) {
                updateChannels(solarMaxData);
                updateStatus(ThingStatus.ONLINE);
                return;
            }
        } catch (SolarMaxException e) {
            logger.debug("Error refreshing source {} at {}:{} - {}", getThing().getUID(), this.config.host,
                    this.config.portNumber, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error with the device. Please retry later.");
        }
    }

    /*
     * Update the channels
     */
    private void updateChannels(SolarMaxData solarMaxData) {
        logger.debug("Updating all channels");
        for (SolarMaxChannel channelConfig : SolarMaxChannel.values()) {
            String channelId = channelConfig.getChannelId();

            Channel channel = getThing().getChannel(channelId);

            if (channelId.equals(SolarMaxChannel.CHANNEL_LAST_UPDATED.getChannelId())) {
                // channel shows when the device was last read, so handle it specially
                State state = solarMaxData.getDataDateTime();
                logger.debug("Update channel state: {} - {}", channelId, state);
                updateState(channel.getUID(), state);

            } else
            // must be somthing to collect from the device, so...
            if (solarMaxData.has(SolarMaxCommandKey.valueOf(channelId))) {

                if (channel == null) {
                    logger.error("No channel found with id: {}", channelId);
                }
                State state = solarMaxData.get(SolarMaxCommandKey.valueOf(channelId));

                if (channel != null && state != null) {
                    logger.debug("Update channel state: {} - {}", channelId, state);
                    updateState(channel.getUID(), state);
                } else {
                    logger.debug("Error refreshing channel {}: {}", getThing().getUID(), channelId);

                }
            }

        }
    }
}
