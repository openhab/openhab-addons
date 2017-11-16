/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;
import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.COMMAND_SET_CLOSURE;

/**
 * The {@link SomfyTahomaContactSensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the contact sensors.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaContactSensorHandler extends SomfyTahomaBaseThingHandler  {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaContactSensorHandler.class);

    public SomfyTahomaContactSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Hashtable<String, String> getStateNames() {
        return new Hashtable<String, String>() {{
            put(CONTACT, "core:ContactState");
        }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!channelUID.getId().equals(CONTACT)) {
            return;
        }

        String url = getURL();
        if (command.equals(RefreshType.REFRESH)) {
            //sometimes refresh is sent sooner than bridge initialized...
            if (getBridgeHandler() != null) {
                getBridgeHandler().updateChannelState(this, channelUID, url);
            }
        }
    }
}
