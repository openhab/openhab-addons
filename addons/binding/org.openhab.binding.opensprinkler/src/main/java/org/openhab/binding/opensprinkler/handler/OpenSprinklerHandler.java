/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.handler;

import static org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.DEFAULT_REFRESH_RATE;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.opensprinkler.OpenSprinklerBindingConstants.Station;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerHandler} is the superclass responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Chris Graham - Initial contribution
 */
public abstract class OpenSprinklerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerHandler.class);

    protected ScheduledFuture<?> pollingJob;

    protected OpenSprinklerApi openSprinklerDevice = null;

    protected int refreshInterval = DEFAULT_REFRESH_RATE;

    public OpenSprinklerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int stationId;

        try {
            stationId = Station.get(channelUID.getId()).toNumber();

            if (command == RefreshType.REFRESH) {
                /* A refresh command means we just need to poll OpenSprinkler for current state. */
                if (stationId == -1) {
                    /* A station ID number of -1 means the rain sensor is being refreshed. */
                    State currentDeviceState = getRainSensorState();
                    updateState(channelUID, currentDeviceState);
                } else {
                    State currentDeviceState = getStationState(stationId);
                    updateState(channelUID, currentDeviceState);
                }
            } else {
                /* Other command types control the OpenSprinkler. Pass off handling. */
                if (stationId == -1) {
                    /* A station ID number of -1 means the rain sensor is attempting to be manipulated. */
                    logger.warn("Attempted to change state of the rain sensor. The rain sensor is read-only.");
                } else {
                    handleStationCommand(stationId, command);
                }
            }
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not control the OpenSprinkler.");
            logger.debug("Error controlling and OpenSprinkler station. Exception received: {}", exp.toString());
        }
    }

    @Override
    public void dispose() {
        if (openSprinklerDevice != null) {
            try {
                openSprinklerDevice.closeConnection();
            } catch (Exception exp) {
                logger.debug("Could not close API connection to the OpenSprinkler device. Exception received: {}",
                        exp.toString());
            }

            openSprinklerDevice = null;

            if (pollingJob != null && !pollingJob.isCancelled()) {
                pollingJob.cancel(true);
                pollingJob = null;
            }

            logger.debug("The handler for the OpenSprinkler device was disposed.");
        }
    }

    /**
     * Handles control of an OpenSprnkler station based on commanded
     * received by a channel call.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @param command Command being issues to the channel.
     */
    protected void handleStationCommand(int stationId, Command command) {
        try {
            if (command == OnOffType.ON) {
                openSprinklerDevice.openStation(stationId);
            } else if (command == OnOffType.OFF) {
                openSprinklerDevice.closeStation(stationId);
            } else {
                logger.error("Received invalid command type for OpenSprinkler station ({}).", command);
            }
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not control the station channel " + (stationId + 1) + " for the OpenSprinkler.");
            logger.debug(
                    "Could not control the station channel {} for the OpenSprinkler device. Exception received: {}",
                    (stationId + 1), exp.toString());
        }
    }

    /**
     * Handles determining a channel's current state from the OpenSprinkler device.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @return State representation for the channel.
     */
    protected State getStationState(int stationId) {
        boolean stationOn = false;

        try {
            stationOn = openSprinklerDevice.isStationOpen(stationId);
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not get the station channel " + (stationId + 1)
                            + " current state from the OpenSprinkler thing.");
            logger.debug(
                    "Could not get current state of station channel {} for the OpenSprinkler device. Exception received: {}",
                    (stationId + 1), exp.toString());
        }

        if (stationOn) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    /**
     * Handles determining the rain sensor channel's current state from the OpenSprinkler device.
     *
     * @return State representation for the rain sensor channel.
     */
    protected State getRainSensorState() {
        boolean rainSensorOn = false;

        try {
            rainSensorOn = openSprinklerDevice.isRainDetected();
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not get the rain sensor current state from the OpenSprinkler thing.");
            logger.debug(
                    "Could not get current state of rain sensor channel from the OpenSprinkler device. Exception received: {}",
                    exp.toString());
        }

        if (rainSensorOn) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }
}
