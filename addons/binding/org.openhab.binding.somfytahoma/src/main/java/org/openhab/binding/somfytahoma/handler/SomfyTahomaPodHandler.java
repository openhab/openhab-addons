/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

/**
 * The {@link SomfyTahomaPodHandler} is responsible for handling commands,
 * which are sent to one of the channels of the pod thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaPodHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaPodHandler.class);

    public SomfyTahomaPodHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {
            {
                put("cyclic_button_state", "core:CyclicButtonState");
                put("battery_status_state", "internal:BatteryStatusState");
                put("lighting_led_pod_mod_state", "internal:LightingLedPodModeState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }
    }
}
