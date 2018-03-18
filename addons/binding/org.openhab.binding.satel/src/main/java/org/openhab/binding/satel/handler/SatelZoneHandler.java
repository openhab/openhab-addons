/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.THING_TYPE_ZONE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.binding.satel.internal.types.ZoneControl;
import org.openhab.binding.satel.internal.types.ZoneState;

/**
 * The {@link SatelZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels of a zone.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelZoneHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ZONE);

    public SatelZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected StateType getStateType(String channelId) {
        return ZoneState.valueOf(channelId.toUpperCase());
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType) {
            boolean switchOn = (command == OnOffType.ON);
            StateType stateType = getStateType(channel.getId());
            int size = bridgeHandler.getIntegraType().hasExtPayload() ? 32 : 16;
            byte[] zones = getObjectBitset(size, thingConfig.getId());
            ZoneControl action = null;
            switch ((ZoneState) stateType) {
                case BYPASS:
                    action = switchOn ? ZoneControl.BYPASS : ZoneControl.UNBYPASS;
                    break;
                case ISOLATE:
                    action = switchOn ? ZoneControl.ISOLATE : null;
                    break;
                default:
                    // do nothing for other types of state
                    break;
            }

            if (action != null) {
                return new ControlObjectCommand(action, zones, bridgeHandler.getUserCode(), scheduler);
            }
        }

        return null;
    }

}
