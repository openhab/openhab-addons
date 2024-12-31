/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link SomfyTahomaUpDownVenetianBlindHandler} is responsible for handling commands,
 * which are sent to one of the channels of the up/down venetian blind thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaUpDownVenetianBlindHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaUpDownVenetianBlindHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        switch (channelUID.getId()) {
            case CONTROL:
            case TILT:
                String cmd = getTahomaCommand(command.toString(), channelUID.getId());
                if (COMMAND_MY.equals(cmd)) {
                    sendCommand(COMMAND_MY);
                } else if (COMMAND_STOP.equals(cmd)) {
                    String executionId = getCurrentExecutions();
                    if (executionId != null) {
                        // Check if the venetian blind is moving and STOP is sent => STOP it
                        cancelExecution(executionId);
                    } else {
                        sendCommand(COMMAND_MY);
                    }
                } else {
                    String param = (COMMAND_TILT_POSITIVE.equals(cmd) || COMMAND_TILT_NEGATIVE.equals(cmd))
                            ? "[" + normalizeTilt(toInteger(command)) + ",0]"
                            : "[]";
                    sendCommand(cmd, param);
                }
                break;
            default:
                return;
        }
    }

    private int normalizeTilt(int i) {
        i = Math.abs(i);
        return (i == 0 || i > 5) ? 1 : i;
    }

    private String getTahomaCommand(String command, String channelId) {
        switch (command) {
            case "OFF":
            case "DOWN":
            case "CLOSE":
                return COMMAND_CLOSE;
            case "ON":
            case "UP":
            case "OPEN":
                return COMMAND_OPEN;
            case "MOVE":
            case "MY":
                return COMMAND_MY;
            case "STOP":
                return COMMAND_STOP;
            default:
                if (TILT.equals(channelId) && !"0".equals(command)) {
                    return (command.startsWith("-")) ? COMMAND_TILT_NEGATIVE : COMMAND_TILT_POSITIVE;
                } else {
                    return COMMAND_REST;
                }
        }
    }
}
