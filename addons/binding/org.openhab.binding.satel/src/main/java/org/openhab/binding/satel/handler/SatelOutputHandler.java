/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.THING_TYPE_OUTPUT;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.OutputControl;
import org.openhab.binding.satel.internal.types.OutputState;
import org.openhab.binding.satel.internal.types.StateType;

/**
 * The {@link SatelOutputHandler} is responsible for handling commands, which are
 * sent to one of the channels of an output device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelOutputHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_OUTPUT);

    public SatelOutputHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected StateType getStateType(String channelId) {
        return OutputState.valueOf(channelId.toUpperCase());
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType && getStateType(channel.getId()) == OutputState.STATE) {
            boolean switchOn = (command == OnOffType.ON);
            int size = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            byte[] outputs = getObjectBitset(size, thingConfig.getId());
            boolean newState = switchOn ^ thingConfig.isStateInverted();
            return new ControlObjectCommand(newState ? OutputControl.ON : OutputControl.OFF, outputs,
                    bridgeHandler.getUserCode(), scheduler);
        }

        return null;
    }

}
