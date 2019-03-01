/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal.converter;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.smartthings.internal.SmartthingsStateData;

/**
 * Converter class for Door Control.
 * This can't use the default because when closing the door the command that comes in is "closed" but "close" need to be
 * sent to Smartthings
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsOpenCloseControlConverter extends SmartthingsConverter {

    SmartthingsOpenCloseControlConverter(String name) {
        super(name);
    }

    public SmartthingsOpenCloseControlConverter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String smartthingsValue = (command.toString().toLowerCase().equals("open")) ? "open" : "close";
        smartthingsValue = (new StringBuilder()).append('"').append(smartthingsValue).append('"').toString();

        String jsonMsg = String.format("{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"value\": %s}",
                thingTypeId, smartthingsName, smartthingsValue);

        return jsonMsg;
    }

    @Override
    public State convertToOpenHab(String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        State state = defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);

        return state;
    }

}
