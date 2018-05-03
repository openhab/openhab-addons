/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

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
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return new Hashtable<String, String>() {
            {
                put(SWITCH, "core:OnOffState");
                put(TARGET_HEATING_LEVEL, "io:TargetHeatingLevelState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command.equals(RefreshType.REFRESH)) {
            updateChannelState(channelUID);
        } else {
            if (channelUID.getId().equals(SWITCH) && command.equals(OnOffType.OFF)) {
                //this thing accepts only off command
                sendCommand(COMMAND_OFF, "[]");
            }
            if (channelUID.getId().equals(TARGET_HEATING_LEVEL)) {
                String param = "[\"" + command.toString() + "\"]";
                sendCommand(COMMAND_SET_HEATINGLEVEL, param);
            }
        }

    }
}
