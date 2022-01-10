/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaWindowHandler} is responsible for handling commands,
 * which are sent to one of the channels of the window thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaWindowHandler extends SomfyTahomaRollerShutterHandler {

    public SomfyTahomaWindowHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!CONTROL.equals(channelUID.getId())) {
            return;
        }

        if (command instanceof RefreshType) {
            return;
        } else {
            String cmd = getTahomaCommand(command.toString());
            if (COMMAND_STOP.equals(cmd)) {
                // Check if the window is not moving
                String executionId = getCurrentExecutions();
                if (executionId != null) {
                    // STOP command should be interpreted if window is moving
                    // otherwise do nothing
                    cancelExecution(executionId);
                }
            } else {
                String param = COMMAND_SET_CLOSURE.equals(cmd) ? "[" + toInteger(command) + "]" : "[]";
                sendCommand(cmd, param);
            }
        }
    }

    @Override
    protected String getTahomaCommand(String command) {
        switch (command) {
            case "OFF":
            case "DOWN":
            case "CLOSE":
                return COMMAND_CLOSE;
            case "ON":
            case "UP":
            case "OPEN":
                return COMMAND_OPEN;
            case "STOP":
                return COMMAND_STOP;
            default:
                return COMMAND_SET_CLOSURE;
        }
    }
}
