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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link SomfyTahomaMyfoxAlarmHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Myfox alarm thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaMyfoxAlarmHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaMyfoxAlarmHandler(Thing thing) {
        super(thing);
        stateNames.put(ALARM_STATE, "myfox:AlarmStatusState");
        stateNames.put(INTRUSION_STATE, "core:IntrusionState");
        stateNames.put(CLOUD_STATUS, "core:CloudDeviceStatusState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (MYFOX_ALARM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand(command.toString());
        }
    }
}
