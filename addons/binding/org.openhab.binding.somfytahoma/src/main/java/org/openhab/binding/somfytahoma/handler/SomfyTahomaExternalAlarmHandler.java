/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.ALARM_COMMAND;

/**
 * The {@link SomfyTahomaExternalAlarmHandler} is responsible for handling commands,
 * which are sent to one of the channels of the alarm thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaExternalAlarmHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaExternalAlarmHandler.class);

    public SomfyTahomaExternalAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return new Hashtable<String, String>() {
            {
                put("active_zones_state", "core:ActiveZonesState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().equals(ALARM_COMMAND) && command instanceof StringType) {
            sendCommand(command.toString(), "[]");
        }
        if (command.equals(RefreshType.REFRESH)) {
            sendCommand("refreshState", "[]");
            updateChannelState(channelUID);
        }
    }

}
