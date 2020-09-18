/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;

/**
 * Converter class for Door Control.
 * This can't use the default because when closing the door the command that comes in as "closed" but "close" needs to
 * be
 * sent to Smartthings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsOpenCloseControlConverter extends SmartthingsConverter {

    public SmartthingsOpenCloseControlConverter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String smartthingsValue = (command.toString().toLowerCase().equals("open")) ? "open" : "close";
        smartthingsValue = surroundWithQuotes(smartthingsValue);

        String jsonMsg = String.format("{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"value\": %s}",
                thingTypeId, smartthingsName, smartthingsValue);

        return jsonMsg;
    }

    @Override
    public State convertToOpenHab(@Nullable String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        State state = defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);

        return state;
    }
}
