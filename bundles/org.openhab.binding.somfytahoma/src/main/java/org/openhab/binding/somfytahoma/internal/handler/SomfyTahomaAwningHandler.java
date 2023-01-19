/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link SomfyTahomaAwningHandler} is responsible for handling commands,
 * which are sent to one of the channels of the awning thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaAwningHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaAwningHandler(Thing thing) {
        super(thing);
        stateNames.put(CONTROL, "core:DeploymentState");
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
            if (COMMAND_MY.equals(cmd)) {
                sendCommand(COMMAND_MY);
            } else if (COMMAND_STOP.equals(cmd)) {
                String executionId = getCurrentExecutions();
                if (executionId != null) {
                    // Check if the awning is moving and STOP is sent => STOP it
                    cancelExecution(executionId);
                } else {
                    sendCommand(COMMAND_MY);
                }
            } else {
                String param = COMMAND_SET_DEPLOYMENT.equals(cmd) ? "[" + toInteger(command) + "]" : "[]";
                sendCommand(cmd, param);
            }
        }
    }

    private String getTahomaCommand(String command) {
        switch (command) {
            case "OFF":
            case "DOWN":
                return COMMAND_DEPLOY;
            case "ON":
            case "UP":
                return COMMAND_UNDEPLOY;
            case "MOVE":
            case "MY":
                return COMMAND_MY;
            case "STOP":
                return COMMAND_STOP;
            default:
                return COMMAND_SET_DEPLOYMENT;
        }
    }
}
