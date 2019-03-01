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
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the smartthings hub into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to smartthings
 *
 * @author Bob Raker - Initial contribution
 *
 */
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
    public State convertToOpenHab(String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        State state = defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);

        return state;
    }

}
