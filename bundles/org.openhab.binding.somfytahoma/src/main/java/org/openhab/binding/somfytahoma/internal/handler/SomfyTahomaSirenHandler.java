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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaSirenHandler} is responsible for handling commands,
 * which are sent to one of the channels of the siren thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSirenHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaSirenHandler(Thing thing) {
        super(thing);
        stateNames.put(BATTERY, "core:BatteryState");
        stateNames.put(ONOFF_STATE, "core:OnOffState");
        stateNames.put(MEMORIZED_VOLUME, "io:MemorizedSimpleVolumeState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        }

        // it is possible only to disable the siren
        if (ONOFF_STATE.equals(channelUID.getId()) && command.equals(OnOffType.OFF)) {
            sendCommand(COMMAND_OFF);
        }

        // highest or normal memorized volume
        if (MEMORIZED_VOLUME.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand("setMemorizedSimpleVolume", "[\"" + command.toString().toLowerCase() + "\"]");
        }
    }
}
