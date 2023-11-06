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
 * The {@link SomfyTahomaGateHandler} is responsible for handling commands,
 * which are sent to one of the channels of the gate thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaGateHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaGateHandler(Thing thing) {
        super(thing);
        stateNames.put(GATE_STATE, "core:OpenClosedPedestrianState");
        stateNames.put(GATE_POSITION, "core:ClosureState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            if (GATE_COMMAND.equals(channelUID.getId())) {
                sendCommand(getGateCommand(command.toString().toLowerCase()));
            } else if (GATE_POSITION.equals(channelUID.getId())) {
                sendCommand(COMMAND_SET_CLOSURE, "[" + toInteger(command) + "]");
            }
        }
    }

    private String getGateCommand(String command) {
        return "pedestrian".equals(command) ? COMMAND_SET_PEDESTRIANPOSITION : command;
    }
}
