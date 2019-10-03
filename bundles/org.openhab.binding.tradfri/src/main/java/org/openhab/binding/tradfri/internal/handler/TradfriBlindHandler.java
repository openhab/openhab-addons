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
package org.openhab.binding.tradfri.internal.handler;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.tradfri.internal.model.TradfriBlindData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriBlindHandler} is responsible for handling commands for individual blinds.
 *
 * @author Manuel Raffel - Initial contribution
 */
@NonNullByDefault
public class TradfriBlindHandler extends TradfriThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriBlindHandler.class);

    // keeps track of the current state for handling of stop/move
    private @Nullable TradfriBlindData state;

    public TradfriBlindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            TradfriBlindData state = new TradfriBlindData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            PercentType position = state.getPosition();
            if (position != null) {
                updateState(CHANNEL_POSITION, position);
            }

            DecimalType batteryLevel = state.getBatteryLevel();
            if (batteryLevel != null) {
                updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            }

            OnOffType batteryLow = state.getBatteryLow();
            if (batteryLow != null) {
                updateState(CHANNEL_BATTERY_LOW, batteryLow);
            }

            updateDeviceProperties(state);

            this.state = state;

            logger.debug(
                    "Updating thing for blindId {} to state {position: {}, firmwareVersion: {}, modelId: {}, vendor: {}}",
                    state.getDeviceId(), position, state.getFirmwareVersion(), state.getModelId(),
                    state.getVendor());
        }
    }

    private void setPosition(PercentType percent) {
        TradfriBlindData data = new TradfriBlindData();
        data.setPosition(percent);
        set(data.getJsonString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (active) {
            if (command instanceof RefreshType) {
                logger.debug("Refreshing channel {}", channelUID);
                coapClient.asyncGet(this);
                return;
            }

            switch (channelUID.getId()) {
                case CHANNEL_POSITION:
                    handlePositionCommand(command);
                    break;
                default:
                    logger.error("Unknown channel UID {}", channelUID);
            }
        }
    }

    private void handlePositionCommand(Command command) {
        if (command instanceof PercentType) {
            setPosition((PercentType) command);
        } else if (command instanceof StopMoveType) {
            final TradfriBlindData state = this.state;
            if (state != null && state.getPosition() != null) {
                if(StopMoveType.STOP.equals(command)) {
                    // setPosition(state.getPosition());
                } else {
                    // (what) TODO (?)
                }            
            } else {
                logger.debug("Cannot handle stop/move as current state is not known.");
            }
        } else if (command instanceof UpDownType) {
            if (UpDownType.UP.equals(command)) {
                setPosition(PercentType.ZERO);
            } else {
                setPosition(PercentType.HUNDRED);
            }
        } else {
            logger.debug("Cannot handle command {} for channel {}", command, CHANNEL_POSITION);
        }
    }
}
