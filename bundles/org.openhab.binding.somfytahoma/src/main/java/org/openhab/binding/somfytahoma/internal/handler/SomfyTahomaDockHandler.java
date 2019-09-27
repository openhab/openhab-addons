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
 * The {@link SomfyTahomaDockHandler} is responsible for handling commands,
 * which are sent to one of the channels of the dock thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDockHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaDockHandler.class);

    public SomfyTahomaDockHandler(Thing thing) {
        super(thing);
        stateNames.put(BATTERY_STATUS, BATTERY_STATUS_STATE);
        stateNames.put(BATTERY_LEVEL, "core:BatteryLevelState");
        stateNames.put(SIREN_STATUS, "internal:SirenStatusState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }

        if (SIREN_STATUS.equals(channelUID.getId()) && command instanceof StringType) {
            sendCommand("siren", "[\"" + command.toString().toLowerCase() + "\"]");
            return;
        }
        if (SHORT_BIP.equals(channelUID.getId()) && OnOffType.ON.equals(command)) {
            sendCommand("shortBip", "[3]");
            return;
        }
        if (LONG_BIP.equals(channelUID.getId()) && OnOffType.ON.equals(command)) {
            sendCommand("longBip", "[3]");
            return;
        }
    }
}
