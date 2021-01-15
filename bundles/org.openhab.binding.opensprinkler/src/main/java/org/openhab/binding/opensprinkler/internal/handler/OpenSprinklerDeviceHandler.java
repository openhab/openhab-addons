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

import javax.measure.quantity.ElectricCurrent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.model.NoCurrentDrawSensorException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
        try {
            OpenSprinklerApi localAPI = getApi();
            if (localAPI == null) {
                logger.debug("localAPI is null");
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
                    updateState(channel,
                            new QuantityType<ElectricCurrent>(localAPI.currentDraw(), MILLI(Units.AMPERE)));
                    break;
                default:
                    logger.debug("Not updating unknown channel {}", channel);
            }
        } catch (NoCurrentDrawSensorException e) {
            logger.debug("Could not update {}, due to {}", channel, e.getMessage());
        } catch (CommunicationApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not communicate with the OpenSprinkler, check it can be reached on the network.");
        }
    }

    @Override
    public void initialize() {
        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Check you have a bridge selected for this thing.");
        } else {
            updateStatus(ThingStatus.ONLINE);
            Channel currentDrawChannel = thing.getChannel(SENSOR_CURRENT_DRAW);
            if (currentDrawChannel != null) {
                ThingBuilder thingBuilder = editThing();
                try {
                    OpenSprinklerApi localAPI = getApi();
                    if (localAPI != null) {
                        localAPI.currentDraw();
                    }
                } catch (NoCurrentDrawSensorException e) {
                    logger.debug("NoCurrentDrawSensorException");
                    thingBuilder.withoutChannel(currentDrawChannel.getUID());
                    updateThing(thingBuilder.build());
                } catch (CommunicationApiException e) {
                    logger.debug("Could not query current draw. Not removing channel as it could be temporary. {}",
                            e.getMessage());
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do here
    }
}
