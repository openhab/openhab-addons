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
 * The {@link SomfyTahomaHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaHeatingSystemHandler(Thing thing) {
        super(thing);
        stateNames.put(TARGET_TEMPERATURE, TARGET_TEMPERATURE_STATE);
        stateNames.put(CURRENT_TEMPERATURE, "zwave:SetPointHeatingValueState");
        stateNames.put(BATTERY_LEVEL, BATTERY_LEVEL_STATE);
        stateNames.put(CURRENT_STATE, "zwave:SetPointTypeState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else {
            if (TARGET_TEMPERATURE.equals(channelUID.getId())) {
                String param = "[" + command.toString() + "]";
                sendCommand("setTargetTemperature", param);
            }
        }
    }
}
