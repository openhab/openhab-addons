/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            if (GATE_COMMAND.equals(channelUID.getId())) {
                sendCommand(getGateCommand(command.toString().toLowerCase()));
            }
        }
    }

    private String getGateCommand(String command) {
        return "pedestrian".equals(command) ? COMMAND_SET_PEDESTRIANPOSITION : command;
    }
}
