/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.internal.converter;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.UPBBindingConstants;
import org.openhab.binding.upb.internal.UPBMessage;

/**
 * A {@link StateChannelConverter} for the {@link UPBBindingConstants#CHANNEL_BRIGHTNESS} channel.
 * 
 * @author Chris
 *
 */
class BrightnessConverter implements StateChannelConverter {

    @Override
    public State convert(UPBMessage message) {
        Byte level = null;
        State newState = null;

        switch (message.getCommand()) {
            case GOTO:
            case DEVICE_STATE:
            case ACTIVATE:

                if (message.getArguments() != null && message.getArguments().length > 0) {
                    level = message.getArguments()[0];
                } else {
                    level = (byte) (message.getCommand() == UPBMessage.Command.ACTIVATE ? 100 : 0);
                }

                // Links will send FF (-1) for their level.
                if (level == -1 || level > 100) {
                    level = 100;
                }

                break;
            case DEACTIVATE:
                level = 0;
                break;
            default:
                break;
        }

        if (level != null) {
            newState = new PercentType(level);
        }

        return newState;
    }

}
