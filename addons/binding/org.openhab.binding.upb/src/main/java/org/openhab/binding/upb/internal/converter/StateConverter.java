/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.UPBBindingConstants;
import org.openhab.binding.upb.internal.UPBMessage;

/**
 * Converts between a {@link UPBMessage} and a {@link State} for a particular channel.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 */
public class StateConverter {

    private Map<String, StateChannelConverter> converters = new HashMap<>();

    /**
     * Instantiates a new {@link StateConverter}.
     */
    public StateConverter() {
        converters.put(UPBBindingConstants.CHANNEL_SWITCH, new SwitchConverter());
        converters.put(UPBBindingConstants.CHANNEL_BRIGHTNESS, new BrightnessConverter());
    }

    /**
     * Converts a {@link UPBMessage} to a {@link State}.
     *
     * @param message the message to convert
     * @param channel the channel to convert for
     * @return a new equivalent {@link State} or null if one does not exist
     */
    public State convert(UPBMessage message, String channel) {

        State state = null;

        if (converters.containsKey(channel)) {
            state = converters.get(channel).convert(message);
        }

        return state;
    }
}
