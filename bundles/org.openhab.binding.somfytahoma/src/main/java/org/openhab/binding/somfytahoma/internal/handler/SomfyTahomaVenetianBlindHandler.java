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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;

/**
 * The {@link SomfyTahomaVenetianBlindHandler} is responsible for handling commands,
 * which are sent to one of the channels of the venetian blind thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaVenetianBlindHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaVenetianBlindHandler.class);

    public SomfyTahomaVenetianBlindHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
            put(CONTROL, "core:ClosureState");
            put(ORIENTATION, "core:SlateOrientationState");
        }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!CONTROL.equals(channelUID.getId()) && !ORIENTATION.equals(channelUID.getId())) {
            return;
        }

        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        } else {
            String cmd = getTahomaCommand(command.toString(), channelUID.getId());
            if (COMMAND_MY.equals(cmd)) {
                String executionId = getCurrentExecutions();
                if (executionId != null) {
                    //Check if the venetian blind is moving and MY is sent => STOP it
                    cancelExecution(executionId);
                } else {
                    sendCommand(COMMAND_MY, "[]");
                }
            } else {
                String param = (COMMAND_SET_CLOSURE.equals(cmd) || COMMAND_SET_ORIENTATION.equals(cmd)) ? "[" + command.toString() + "]" : "[]";
                sendCommand(cmd, param);
            }
        }

    }

    private String getTahomaCommand(String command, String channelId) {
        switch (command) {
            case "OFF":
            case "DOWN":
                return COMMAND_DOWN;
            case "ON":
            case "UP":
                return COMMAND_UP;
            case "STOP":
                return COMMAND_MY;
            default:
                if (CONTROL.equals(channelId)) {
                    return COMMAND_SET_CLOSURE;
                } else {
                    return COMMAND_SET_ORIENTATION;
                }
        }
    }
}
