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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaSirenHandler} is responsible for handling commands,
 * which are sent to one of the channels of the siren thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSirenHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaSirenHandler.class);

    public SomfyTahomaSirenHandler(Thing thing) {
        super(thing);
        stateNames.put(BATTERY, "core:BatteryState");
        stateNames.put(ONOFF_STATE, "core:OnOffState");
        stateNames.put(MEMORIZED_VOLUME, "io:MemorizedSimpleVolumeState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }

        //it is possible only to disable the siren
        if (ONOFF_STATE.equals(channelUID.getId()) && command.equals(OnOffType.OFF)) {
            sendCommand(COMMAND_OFF, "[]");
        }

        // highest or normal memorized volume
        if (MEMORIZED_VOLUME.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand("setMemorizedSimpleVolume", "[\"" + command.toString().toLowerCase() + "\"]");
        }

    }
}
