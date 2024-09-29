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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.ALARM_COMMAND;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link SomfyTahomaExternalAlarmHandler} is responsible for handling commands,
 * which are sent to one of the channels of the alarm thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaExternalAlarmHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaExternalAlarmHandler(Thing thing) {
        super(thing);
        stateNames.put("active_zones_state", "core:ActiveZonesState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (ALARM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand(command.toString());
        }
    }
}
