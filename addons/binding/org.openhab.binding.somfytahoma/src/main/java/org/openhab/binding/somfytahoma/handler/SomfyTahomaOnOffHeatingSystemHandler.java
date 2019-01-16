/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaOnOffHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the ON/OFF heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaOnOffHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaOnOffHeatingSystemHandler.class);

    public SomfyTahomaOnOffHeatingSystemHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {
            {
                put(TARGET_HEATING_LEVEL, "io:TargetHeatingLevelState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        } else {
            if (TARGET_HEATING_LEVEL.equals(channelUID.getId())) {
                String param = "[\"" + command.toString() + "\"]";
                sendCommand(COMMAND_SET_HEATINGLEVEL, param);
            }
        }
    }
}
