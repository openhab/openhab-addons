/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;

/**
 * The {@link SomfyTahomaInternalAlarmHandler} is responsible for handling commands,
 * which are sent to one of the channels of the alarm thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaInternalAlarmHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaInternalAlarmHandler.class);

    public SomfyTahomaInternalAlarmHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {
            {
                put(ALARM_STATE, "internal:CurrentAlarmModeState");
                put(TARGET_ALARM_STATE, "internal:TargetAlarmModeState");
                put(INTRUSION_STATE, "internal:IntrusionDetectedState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (ALARM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand(command.toString(), "[]");
        }
        if (INTRUSION_CONTROL.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand("setIntrusionDetected", "[\"" + command.toString() + "\"]");
        }
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }
    }
}
