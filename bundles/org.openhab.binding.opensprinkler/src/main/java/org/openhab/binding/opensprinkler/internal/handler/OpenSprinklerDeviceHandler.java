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

import java.util.ArrayList;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerDeviceHandler extends OpenSprinklerBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDeviceHandler.class);

    public OpenSprinklerDeviceHandler(Thing thing) {
        super(thing);
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
                updateState(channel, new QuantityType<Dimensionless>(localAPI.flowMeterCount(), Units.DECIBEL));
                break;
            case SENSOR_FLOW_METER_COUNT:
                updateState(channel, new QuantityType<Dimensionless>(localAPI.flowMeterCount(), Units.ONE));
                break;
            default:
                logger.debug("Can not update the unknown channel {}", channel);
        }
    }

    @Override
    public void initialize() {
        OpenSprinklerApi localAPI = getApi();
        if (localAPI != null) {
            ArrayList<Channel> removeChannels = new ArrayList<>();
            Channel channel = thing.getChannel(SENSOR_CURRENT_DRAW);
            if (localAPI.currentDraw() == -1 && channel != null) {
                logger.debug("No current sensor detected, removing channel.");
                removeChannels.add(channel);
            }
            channel = thing.getChannel(SENSOR_SIGNAL_STRENGTH);
            if (localAPI.signalStrength() == 0 && channel != null) {
                removeChannels.add(channel);
            }
            channel = thing.getChannel(SENSOR_FLOW_METER_COUNT);
            if (localAPI.flowMeterCount() == -1 && channel != null) {
                removeChannels.add(channel);
            }
            if (!removeChannels.isEmpty()) {
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withoutChannels(removeChannels);
                updateThing(thingBuilder.build());
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do here
    }
}
