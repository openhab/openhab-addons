/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaGateHandler} is responsible for handling commands,
 * which are sent to one of the channels of the gate thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaGateHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaGateHandler.class);

    public SomfyTahomaGateHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {
            {
                put(GATE_STATE, "core:OpenClosedPedestrianState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        } else {
            if (GATE_COMMAND.equals(channelUID.getId())) {
                sendCommand(getGateCommand(command.toString().toLowerCase()), "[]");
            }
        }
    }

    private String getGateCommand(String command) {
        return "pedestrian".equals(command) ? COMMAND_SET_PEDESTRIANPOSITION : command;
    }
}
