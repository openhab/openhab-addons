/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaShutterHandler} is responsible for handling commands,
 * which are sent to one of the channels of the shutter thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaShutterHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaShutterHandler(Thing thing) {
        super(thing);
        // Map the closure state directly to the existing CONTROL channel
        stateNames.put(CONTROL, "core:ClosureState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!CONTROL.equals(channelUID.getId()) || command instanceof RefreshType) {
            return;
        }

        if (command instanceof DecimalType) {
            // Send percentage values directly
            sendCommand(COMMAND_SET_CLOSURE, "[" + toInteger(command) + "]");
        } else {
            // Handle string commands (UP, DOWN, STOP)
            String cmd = getTahomaCommand(command.toString());
            if (COMMAND_STOP.equals(cmd)) {
                String executionId = getCurrentExecutions();
                if (executionId != null) {
                    cancelExecution(executionId);
                } else {
                    sendCommand(COMMAND_STOP);
                }
            } else if (cmd != null) {
                // Only send if the command is recognized
                sendCommand(cmd, "[]");
            }
        }
    }

    protected @Nullable String getTahomaCommand(String command) {
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
                // Return null to ignore unsupported commands like MOVE
                return null;
        }
    }
}
