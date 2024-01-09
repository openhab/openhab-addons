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
 * The {@link SomfyTahomaSmokeSensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the smoke sensor thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSmokeSensorHandler extends SomfyTahomaContactSensorHandler {

    public SomfyTahomaSmokeSensorHandler(Thing thing) {
        super(thing);
        stateNames.put(CONTACT, SMOKE_STATE);
        stateNames.put(SENSOR_DEFECT, SENSOR_DEFECT_STATE);
        stateNames.put(SENSOR_BATTERY, SENSOR_PART_BATTERY_STATE);
        stateNames.put(RADIO_BATTERY, RADIO_PART_BATTERY_STATE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof RefreshType) {
            return;
        } else if (ALARM_CHECK.equals(channelUID.getId())) {
            sendCommand(COMMAND_CHECK_TRIGGER, "[\"" + command.toString().toLowerCase() + "\"]");
        }
    }
}
