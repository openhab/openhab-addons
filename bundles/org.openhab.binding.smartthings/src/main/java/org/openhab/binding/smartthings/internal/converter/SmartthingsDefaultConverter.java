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
 * This "Converter" is assigned to a channel when a special converter is not needed.
 * A channel specific converter is specified in the thing-type channel property smartthings-converter then that channel
 * is used.
 * If a channel specific converter is not found a convert based on the channel ID is used.
 * If there is no convert found then this Default converter is used.
 * Yes, it would be possible to change the SamrtthingsConverter class to not being abstract and implement these methods
 * there. But, this makes it explicit that the default converter is being used.
 * See SmartthingsThingHandler.initialize() for details
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDefaultConverter extends SmartthingsConverter {

    public SmartthingsDefaultConverter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String jsonMsg = defaultConvertToSmartthings(channelUid, command);
        return jsonMsg;
    }

    @Override
    public State convertToOpenHab(@Nullable String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        State state = defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);
        return state;
    }
}
