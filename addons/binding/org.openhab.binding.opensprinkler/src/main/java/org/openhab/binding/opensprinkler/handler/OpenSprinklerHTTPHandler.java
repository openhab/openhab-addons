/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.handler;

import static org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.Station;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerHTTPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerHTTPHandler extends OpenSprinklerHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerHTTPHandler.class);

    private OpenSprinklerConfig openSprinklerConfig = null;

    public OpenSprinklerHTTPHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        openSprinklerConfig = getConfig().as(OpenSprinklerConfig.class);

        if (openSprinklerConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Could not parse the config for the OpenSprinkler.");
            return;
        }

        logger.debug("Initializing OpenSprinkler with config (Hostname: {}, Port: {}, Password: {}, Refresh: {}).",
                openSprinklerConfig.hostname, openSprinklerConfig.port, openSprinklerConfig.password,
                openSprinklerConfig.refresh);

        try {
            openSprinklerDevice = OpenSprinklerApiFactory.getHttpApi(openSprinklerConfig.hostname,
                    openSprinklerConfig.port, openSprinklerConfig.password);
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the OpenSprinkler.");
            logger.debug("Could not create API connection to the OpenSprinkler device. Exception received: {}",
                    exp.toString());

            return;
        }

        logger.debug("Successfully created API connection to the OpenSprinkler device.");

        try {
            openSprinklerDevice.openConnection();
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not open the connection to the OpenSprinkler.");
            logger.debug("Could not open API connection to the OpenSprinkler device. Exception received: {}",
                    exp.toString());
        }

        if (openSprinklerDevice.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("OpenSprinkler connected.");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not initialize the connection to the OpenSprinkler.");

            return;
        }

        onUpdate();
    }

    /**
     * Creates a new polling job to sync state with the OpenSprinkler device.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            int refresh;

            try {
                refresh = getConfig().as(OpenSprinklerConfig.class).refresh;
            } catch (Exception exp) {
                refresh = this.refreshInterval;
            }

            pollingJob = scheduler.scheduleWithFixedDelay(refreshService, DEFAULT_WAIT_BEFORE_INITIAL_REFRESH, refresh,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Threaded scheduled job that periodically syncs the state of the OpenSprinkler device.
     */
    private Runnable refreshService = new Runnable() {
        @Override
        public void run() {
            if (openSprinklerDevice != null) {
                if (openSprinklerDevice.isConnected()) {
                    logger.debug("Refreshing state with the OpenSprinkler device.");

                    try {
                        if (openSprinklerDevice.isRainDetected()) {
                            updateState(new ChannelUID(getThing().getUID(), SENSOR_RAIN), OnOffType.ON);
                        } else {
                            updateState(new ChannelUID(getThing().getUID(), SENSOR_RAIN), OnOffType.OFF);
                        }

                        for (int i = 0; i < openSprinklerDevice.getNumberOfStations(); i++) {
                            ChannelUID channel = new ChannelUID(getThing().getUID(), Station.get(i).channelID());
                            State command = getStationState(i);
                            updateState(channel, command);
                        }

                        updateStatus(ThingStatus.ONLINE);
                    } catch (Exception exp) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Could not refresh current state from the OpenSprinkler.");
                        logger.debug(
                                "Could not refresh current state of the OpenSprinkler device. Exception received: {}",
                                exp.toString());
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Could not sync status with the OpenSprinkler.");
                }
            }
        }
    };
}
