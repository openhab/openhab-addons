/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Converter class for Door Control.
 * This can't use the default because when closing the door the command that comes in as "closed" but "close" needs to
 * be
 * sent to SmartThings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsOpenCloseControlConverter extends SmartThingsConverter {

    public SmartThingsOpenCloseControlConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command) {
        String smartthingsValue = (SmartThingsBindingConstants.OPEN_VALUE.equals(command.toString().toLowerCase()))
                ? SmartThingsBindingConstants.OPEN_VALUE
                : SmartThingsBindingConstants.CLOSE_VALUE;
        smartthingsValue = surroundWithQuotes(smartthingsValue);

        // @todo : to review, no action !
        // String msg = String.format("{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"value\": %s}",
        // thing.getThingTypeUID(), "smartthingsName", smartthingsValue);
    }

    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartThings) {
        return defaultConvertToOpenHab(thing, channelUid, dataFromSmartThings);
    }
}
