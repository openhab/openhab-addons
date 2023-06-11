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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.event.IntegraStateEvent;
import org.openhab.binding.satel.internal.types.OutputControl;
import org.openhab.binding.satel.internal.types.OutputState;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelShutterHandler} is responsible for handling commands, which are
 * sent to one of the channels of a shutter device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelShutterHandler extends SatelStateThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SHUTTER);

    private final Logger logger = LoggerFactory.getLogger(SatelShutterHandler.class);

    public SatelShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void incomingEvent(IntegraStateEvent event) {
        logger.trace("Handling incoming event: {}", event);
        if (getThingConfig().isCommandOnly() || !event.hasDataForState(OutputState.STATE)) {
            return;
        }
        Channel channel = getThing().getChannel(CHANNEL_SHUTTER_STATE);
        if (channel != null) {
            int upBitNbr = getThingConfig().getUpId() - 1;
            int downBitNbr = getThingConfig().getDownId() - 1;
            if (event.isSet(OutputState.STATE, upBitNbr)) {
                if (!event.isSet(OutputState.STATE, downBitNbr)) {
                    updateState(channel.getUID(), UpDownType.UP);
                }
            } else if (event.isSet(OutputState.STATE, downBitNbr)) {
                updateState(channel.getUID(), UpDownType.DOWN);
            }
        }
    }

    @Override
    protected StateType getStateType(String channelId) {
        return CHANNEL_SHUTTER_STATE.equals(channelId) ? OutputState.STATE : StateType.NONE;
    }

    @Override
    protected Optional<SatelCommand> convertCommand(ChannelUID channel, Command command) {
        ControlObjectCommand result = null;
        if (CHANNEL_SHUTTER_STATE.equals(channel.getId())) {
            final SatelBridgeHandler bridgeHandler = getBridgeHandler();
            int cmdBytes = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            if (command == UpDownType.UP) {
                byte[] outputs = getObjectBitset(cmdBytes, getThingConfig().getUpId());
                result = new ControlObjectCommand(OutputControl.ON, outputs, bridgeHandler.getUserCode(), scheduler);
            } else if (command == UpDownType.DOWN) {
                byte[] outputs = getObjectBitset(cmdBytes, getThingConfig().getDownId());
                result = new ControlObjectCommand(OutputControl.ON, outputs, bridgeHandler.getUserCode(), scheduler);
            } else if (command == StopMoveType.STOP) {
                byte[] outputs = getObjectBitset(cmdBytes, getThingConfig().getUpId(), getThingConfig().getDownId());
                result = new ControlObjectCommand(OutputControl.OFF, outputs, bridgeHandler.getUserCode(), scheduler);
            }
        }

        return result == null ? Optional.empty() : Optional.of(result);
    }
}
