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
package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerStateDescriptionProvider;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerDeviceHandler extends OpenSprinklerBaseHandler {
    public final OpenSprinklerStateDescriptionProvider stateDescriptionProvider;

    public OpenSprinklerDeviceHandler(Thing thing, OpenSprinklerStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    protected void updateChannel(ChannelUID channel) {
        OpenSprinklerApi localAPI = getApi();
        if (localAPI == null) {
            return;
        }
        switch (channel.getIdWithoutGroup()) {
            case SENSOR_RAIN:
                if (localAPI.isRainDetected()) {
                    updateState(channel, OnOffType.ON);
                } else {
                    updateState(channel, OnOffType.OFF);
                }
                break;
            case SENSOR_WATERLEVEL:
                updateState(channel, QuantityType.valueOf(localAPI.waterLevel(), PERCENT));
                break;
            case SENSOR_CURRENT_DRAW:
                updateState(channel, new QuantityType<ElectricCurrent>(localAPI.currentDraw(), MILLI(Units.AMPERE)));
                break;
            case SENSOR_SIGNAL_STRENGTH:
                updateState(channel, new QuantityType<Dimensionless>(localAPI.signalStrength(), Units.DECIBEL));
                break;
            case SENSOR_FLOW_COUNT:
                updateState(channel, new QuantityType<Dimensionless>(localAPI.flowSensorCount(), Units.ONE));
                break;
            case CHANNEL_PROGRAMS:
                break;
            case CHANNEL_STATIONS:
                break;
            case NEXT_DURATION:
                break;
            default:
                logger.debug("Can not update the unknown channel {}", channel);
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        OpenSprinklerApi localAPI = getApi();
        // Remove channels due to missing sensors or old firmware
        if (localAPI != null) {
            ArrayList<Channel> removeChannels = new ArrayList<>();
            Channel channel = thing.getChannel(SENSOR_CURRENT_DRAW);
            if (localAPI.currentDraw() == -1 && channel != null) {
                logger.debug("No current sensor detected, removing channel.");
                removeChannels.add(channel);
            }
            channel = thing.getChannel(SENSOR_SIGNAL_STRENGTH);
            if (localAPI.signalStrength() == 1 && channel != null) {
                removeChannels.add(channel);
            }
            channel = thing.getChannel(SENSOR_FLOW_COUNT);
            if (localAPI.flowSensorCount() == -1 && channel != null) {
                removeChannels.add(channel);
            }
            if (!removeChannels.isEmpty()) {
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withoutChannels(removeChannels);
                updateThing(thingBuilder.build());
            }
            updateProgramsChanOptions(localAPI);
            updateStationsChanOptions(localAPI);
            nextDurationTime = new BigDecimal(1800);
            updateState(NEXT_DURATION, new QuantityType<>(nextDurationTime, Units.SECOND));
        }
    }

    /**
     * Fetch the stored Program list and update the StateOptions on the channel so they match.
     *
     * @param api
     */
    private void updateProgramsChanOptions(OpenSprinklerApi api) {
        stateDescriptionProvider.setStateOptions(new ChannelUID(this.getThing().getUID(), CHANNEL_PROGRAMS),
                api.getPrograms());
    }

    private void updateStationsChanOptions(OpenSprinklerApi api) {
        stateDescriptionProvider.setStateOptions(new ChannelUID(this.getThing().getUID(), CHANNEL_STATIONS),
                api.getStations());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        OpenSprinklerApi api = getApi();
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "OpenSprinkler bridge returned no API.");
            return;
        }
        OpenSprinklerHttpBridgeHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return;
        }
        try {
            if (command instanceof RefreshType) {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PROGRAMS:
                        api.getProgramData();
                        updateProgramsChanOptions(api);
                        break;
                    case CHANNEL_STATIONS:
                        api.getStationNames();
                        updateStationsChanOptions(api);
                        break;
                }
            } else {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_PROGRAMS:
                        api.runProgram(command);
                        break;
                    case NEXT_DURATION:
                        handleNextDurationCommand(channelUID, command);
                        break;
                    case CHANNEL_STATIONS:
                        if (command instanceof StringType) {
                            BigDecimal temp = new BigDecimal(command.toString());
                            api.openStation(temp.intValue(), nextDurationValue());
                        }
                        break;
                }
                localBridge.refreshStations();// update sensors and controls after command is sent
            }
        } catch (Exception e) {
            localBridge.communicationError(e);
        }
    }
}
