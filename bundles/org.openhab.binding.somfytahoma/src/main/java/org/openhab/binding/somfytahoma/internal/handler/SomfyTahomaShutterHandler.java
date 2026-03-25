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
        // Octa22: Koppel de status (ClosureState) direct aan het bestaande CONTROL kanaal
        stateNames.put(CONTROL, "core:ClosureState");
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
                String executionId = getCurrentExecutions();
                if (executionId != null) {
                    cancelExecution(executionId);
                } else {
                    sendCommand(COMMAND_STOP);
                }
            } else {
                String param = COMMAND_SET_CLOSURE.equals(cmd) ? "[" + toInteger(command) + "]" : "[]";
                sendCommand(cmd, param);
            }
        }
    }

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