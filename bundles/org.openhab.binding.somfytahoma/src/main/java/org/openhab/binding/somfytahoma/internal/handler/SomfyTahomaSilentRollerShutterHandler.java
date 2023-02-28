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
 * The {@link SomfyTahomaSilentRollerShutterHandler} is responsible for handling commands,
 * which are sent to one of the channels of the silent roller shutter thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSilentRollerShutterHandler extends SomfyTahomaRollerShutterHandler {

    public SomfyTahomaSilentRollerShutterHandler(Thing thing) {
        super(thing);
        stateNames.put(CONTROL_SILENT, "core:ClosureState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!CONTROL.equals(channelUID.getId()) && !channelUID.getId().equals(CONTROL_SILENT)) {
            return;
        }

        if (command instanceof RefreshType) {
            return;
        } else {
            String cmd = getTahomaCommand(command.toString());
            switch (cmd) {
                case COMMAND_STOP:
                    String executionId = getCurrentExecutions();
                    if (executionId != null) {
                        // Check if the roller shutter is moving and STOP is sent => STOP it
                        cancelExecution(executionId);
                        break;
                    }
                    // fall through
                case COMMAND_MY:
                    sendCommand(COMMAND_MY);
                    break;
                case COMMAND_SET_CLOSURE:
                    if (CONTROL_SILENT.equals(channelUID.getId())) {
                        // move the roller shutter to the specific position at low speed
                        String param = "[" + toInteger(command) + ", \"lowspeed\"]";
                        sendCommand(COMMAND_SET_CLOSURESPEED, param);
                    } else {
                        String param = "[" + toInteger(command) + "]";
                        sendCommand(cmd, param);
                    }
                    break;
                case COMMAND_UP:
                case COMMAND_DOWN:
                    if (CONTROL_SILENT.equals(channelUID.getId())) {
                        // move the roller shutter to the specific position at low speed
                        String param = "[" + (COMMAND_UP.equals(cmd) ? 0 : 100) + ", \"lowspeed\"]";
                        sendCommand(COMMAND_SET_CLOSURESPEED, param);
                        break;
                    }
                    // fall through
                default:
                    sendCommand(cmd);
            }
        }
    }
}
