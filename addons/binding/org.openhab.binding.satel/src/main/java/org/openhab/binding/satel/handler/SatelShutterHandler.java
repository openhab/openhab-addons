/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.event.IntegraStateEvent;
import org.openhab.binding.satel.internal.event.SatelEvent;
import org.openhab.binding.satel.internal.types.OutputControl;
import org.openhab.binding.satel.internal.types.OutputState;
import org.openhab.binding.satel.internal.types.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelShutterHandler} is responsible for handling commands, which are
 * sent to one of the channels of a shutter device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelShutterHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SHUTTER);

    private final Logger logger = LoggerFactory.getLogger(SatelShutterHandler.class);

    public SatelShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void incomingEvent(SatelEvent event) {
        if (event instanceof IntegraStateEvent) {
            logger.trace("Handling incoming event: {}", event);

            IntegraStateEvent stateEvent = (IntegraStateEvent) event;
            if (thingConfig.isCommandOnly() || !stateEvent.hasDataForState(OutputState.STATE)) {
                return;
            }
            Channel channel = getThing().getChannel(CHANNEL_SHUTTER_STATE);
            if (channel != null) {
                int upBitNbr = thingConfig.getUpId() - 1;
                int downBitNbr = thingConfig.getDownId() - 1;
                if (stateEvent.isSet(OutputState.STATE, upBitNbr)) {
                    if (!stateEvent.isSet(OutputState.STATE, downBitNbr)) {
                        updateState(channel.getUID(), UpDownType.UP);
                    }
                } else if (stateEvent.isSet(OutputState.STATE, downBitNbr)) {
                    updateState(channel.getUID(), UpDownType.DOWN);
                }
            }
        } else {
            super.incomingEvent(event);
        }
    }

    @Override
    protected StateType getStateType(String channelId) {
        return CHANNEL_SHUTTER_STATE.equals(channelId) ? OutputState.STATE : null;
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        if (CHANNEL_SHUTTER_STATE.equals(channel.getId())) {
            int cmdBytes = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            if (command == UpDownType.UP) {
                byte[] outputs = getObjectBitset(cmdBytes, thingConfig.getUpId());
                return new ControlObjectCommand(OutputControl.ON, outputs, bridgeHandler.getUserCode(), scheduler);
            } else if (command == UpDownType.DOWN) {
                byte[] outputs = getObjectBitset(cmdBytes, thingConfig.getDownId());
                return new ControlObjectCommand(OutputControl.ON, outputs, bridgeHandler.getUserCode(), scheduler);
            } else if (command == StopMoveType.STOP) {
                byte[] outputs = getObjectBitset(cmdBytes, thingConfig.getUpId(), thingConfig.getDownId());
                return new ControlObjectCommand(OutputControl.OFF, outputs, bridgeHandler.getUserCode(), scheduler);
            }
        }

        return null;
    }

}
