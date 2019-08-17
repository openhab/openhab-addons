/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerStationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerStationHandler extends OpenSprinklerBaseHandler {
    private static final String STORAGE_NAME = "opensprinkler";
    private static final String DURATION_PREFIX = "DURATION_";
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerStationHandler.class);

    @Nullable
    private OpenSprinklerStationConfig config;

    private StorageService storageService;

    public OpenSprinklerStationHandler(Thing thing, StorageService storageService) {
        super(thing);
        this.storageService = storageService;
    }

    @Override
    public void initialize() {
        config = getConfig().as(OpenSprinklerStationConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "OpenSprinkler bridge has no initialized API.");
            return;
        }
        try {
            if (command == RefreshType.REFRESH) {
                updateChannels();
                return;
            }
            switch (channelUID.getIdWithoutGroup()) {
                case NEXT_DURATION:
                    if (!(command instanceof QuantityType<?>)) {
                        logger.info("Ignoring implausible non-DecimalType command for NEXT_DURATION");
                        return;
                    }
                    Storage<BigDecimal> storage = this.storageService.getStorage(STORAGE_NAME);
                    storage.put(DURATION_PREFIX + this.getStationIndex(), ((QuantityType<?>) command).toBigDecimal());
                    updateState(channelUID, (DecimalType) command);
                    break;
                case STATION_STATE:
                    handleStationCommand(api, command);
                    break;
            }
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not control the OpenSprinkler.");
            logger.debug("Error controlling and OpenSprinkler station. Exception received: {}", exp.toString());
        }
    }

    /**
     * Handles control of an OpenSprnkler station based on commanded
     * received by a channel call.
     *
     * @param command Command being issues to the channel.
     */
    protected void handleStationCommand(OpenSprinklerApi api, Command command) {
        try {
            if (command == OnOffType.ON) {
                api.openStation(this.getStationIndex(), nextStationDuration());
            } else if (command == OnOffType.OFF) {
                api.closeStation(this.getStationIndex());
            } else {
                logger.error("Received invalid command type for OpenSprinkler station ({}).", command);
            }
        } catch (CommunicationApiException | GeneralApiException exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not control the station channel " + (this.getStationIndex() + 1)
                            + " for the OpenSprinkler. Error: " + exp.getMessage());
        }
    }

    private BigDecimal nextStationDuration() {
        BigDecimal nextDurationItemValue = nextDurationValue();
        Channel nextDuration = getThing().getChannel(NEXT_DURATION);
        if (nextDuration != null && isLinked(nextDuration.getUID()) && nextDurationItemValue != null) {
            return nextDurationItemValue;
        }
        return config.duration;
    }

    /**
     * Handles determining a channel's current state from the OpenSprinkler device.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @return State representation for the channel.
     */
    @Nullable
    protected State getStationState(int stationId) {
        boolean stationOn = false;
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "OpenSprinkler bridge has no initialized API.");
            return null;
        }

        try {
            stationOn = api.isStationOpen(stationId);
        } catch (Exception exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not get the station channel " + stationId + " current state from the OpenSprinkler thing.");
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
     * Handles determining a channel's current state from the OpenSprinkler device.
     *
     * @param stationId Int of the station to control. Starts at 0.
     * @return State representation for the channel.
     */
    @Nullable
    protected State getRemainingWaterTime(int stationId) {
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

        return new QuantityType<Time>(remainingWaterTime, Units.SECOND);
    }

    @Override
    protected void updateChannel(@NonNull ChannelUID channel) {
        switch (channel.getIdWithoutGroup()) {
            case STATION_STATE:
                State currentDeviceState = getStationState(this.getStationIndex());
                if (currentDeviceState != null) {
                    updateState(channel, currentDeviceState);
                }
                break;
            case REMAINING_WATER_TIME:
                State remainingWaterTime = getRemainingWaterTime(config.stationIndex);
                if (remainingWaterTime != null) {
                    updateState(channel, remainingWaterTime);
                }
                break;
            case NEXT_DURATION:
                BigDecimal duration = nextDurationValue();
                if (duration != null) {
                    updateState(channel, new DecimalType(duration));
                }
                break;
            default:
                logger.debug("Not updating unknown channel {}", channel);
        }
    }

    private @Nullable BigDecimal nextDurationValue() {
        Storage<BigDecimal> storage = storageService.getStorage(STORAGE_NAME);
        BigDecimal duration = storage.get(DURATION_PREFIX + this.getStationIndex());
        return duration;
    }

    private int getStationIndex() {
        OpenSprinklerStationConfig config = this.config;
        if (config == null) {
            throw new IllegalStateException();
        }
        return config.stationIndex;
    }

}
