/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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
        String smartthingsValue = ("open".equals(command.toString().toLowerCase())) ? "open" : "close";
        smartthingsValue = surroundWithQuotes(smartthingsValue);

        return String.format("{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"value\": %s}", thingTypeId,
                smartthingsName, smartthingsValue);
    }

    @Override
    public State convertToOpenHab(@Nullable String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        return defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);
    }
}
