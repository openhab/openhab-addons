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
package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.*;

import java.math.BigDecimal;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerStationConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerStationHandler extends OpenSprinklerBaseHandler {
    private OpenSprinklerStationConfig config = new OpenSprinklerStationConfig();

    public OpenSprinklerStationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfig().as(OpenSprinklerStationConfig.class);
        OpenSprinklerApi api = getApi();
        if (api != null && config.stationIndex >= api.getNumberOfStations()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Station Index is higher than the number of stations that the OpenSprinkler is reporting. Make sure your Station Index is correct.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "OpenSprinkler bridge has no initialized API.");
            return;
        }
        try {
            if (command != RefreshType.REFRESH) {
                switch (channelUID.getIdWithoutGroup()) {
                    case NEXT_DURATION:
                        handleNextDurationCommand(channelUID, command);
                        break;
                    case STATION_STATE:
                        if (!(command instanceof OnOffType)) {
                            logger.warn("Received invalid command type for OpenSprinkler station ({}).", command);
                            return;
                        }
                        if (command == OnOffType.ON) {
                            api.openStation(config.stationIndex, nextDurationValue());
                        } else {
                            api.closeStation(config.stationIndex);
                        }
                        break;
                    case STATION_QUEUED:
                        if (command == OnOffType.OFF) {
                            api.closeStation(config.stationIndex);
                        }
                        break;
                    case CHANNEL_IGNORE_RAIN:
                        api.ignoreRain(config.stationIndex, command == OnOffType.ON);
                        break;
                }
                OpenSprinklerHttpBridgeHandler localBridge = bridgeHandler;
                if (localBridge == null) {
                    return;
                }
                // update all controls after a command is sent in case a long poll time is set.
                localBridge.delayedRefresh();
            }
        } catch (GeneralApiException | CommunicationApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not control the station channel " + (config.stationIndex + 1)
                            + " for the OpenSprinkler. Error: " + e.getMessage());
        }
    }

    /**
     * Handles determining a channel's current state from the OpenSprinkler device.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @return State representation for the channel.
     */
    @Nullable
    private OnOffType getStationState(int stationId) {
        boolean stationOn = false;
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "OpenSprinkler bridge has no initialized API.");
            return null;
        }

        try {
            stationOn = api.isStationOpen(stationId);
        } catch (GeneralApiException | CommunicationApiException exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not get the station channel " + stationId
                            + " current state from the OpenSprinkler thing. Error: " + exp.getMessage());
        }

        if (stationOn) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    /**
     * Handles determining a channel's current state from the OpenSprinkler device.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @return State representation for the channel.
     */
    private @Nullable QuantityType<Time> getRemainingWaterTime(int stationId) {
        long remainingWaterTime = 0;
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "OpenSprinkler bridge has no initialized API.");
            return null;
        }

        try {
            remainingWaterTime = api.retrieveProgram(stationId).remainingWaterTime;
        } catch (CommunicationApiException exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not get current state of station channel " + stationId
                            + " for the OpenSprinkler device. Exception received: " + exp);
        }

        return new QuantityType<>(remainingWaterTime, Units.SECOND);
    }

    @Override
    protected void updateChannel(ChannelUID channel) {
        OnOffType currentDeviceState = getStationState(config.stationIndex);
        QuantityType<Time> remainingWaterTime = getRemainingWaterTime(config.stationIndex);
        OpenSprinklerApi api = getApi();
        switch (channel.getIdWithoutGroup()) {
            case STATION_STATE:
                if (currentDeviceState != null) {
                    updateState(channel, currentDeviceState);
                }
                break;
            case REMAINING_WATER_TIME:
                if (remainingWaterTime != null) {
                    updateState(channel, remainingWaterTime);
                }
                break;
            case NEXT_DURATION:
                BigDecimal duration = nextDurationValue();
                updateState(channel, new QuantityType<>(duration, Units.SECOND));
                break;
            case STATION_QUEUED:
                if (remainingWaterTime != null && currentDeviceState != null && currentDeviceState == OnOffType.OFF
                        && remainingWaterTime.intValue() != 0) {
                    updateState(channel, OnOffType.ON);
                } else {
                    updateState(channel, OnOffType.OFF);
                }
                break;
            case CHANNEL_IGNORE_RAIN:
                if (api != null && api.isIgnoringRain(config.stationIndex)) {
                    updateState(channel, OnOffType.ON);
                } else {
                    updateState(channel, OnOffType.OFF);
                }
                break;
            default:
                logger.debug("Not updating unknown channel {}", channel);
        }
    }
}
