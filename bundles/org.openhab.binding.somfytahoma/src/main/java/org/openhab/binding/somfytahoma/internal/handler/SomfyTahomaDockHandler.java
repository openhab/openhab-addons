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
 * The {@link SomfyTahomaDockHandler} is responsible for handling commands,
 * which are sent to one of the channels of the dock thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDockHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaDockHandler(Thing thing) {
        super(thing);
        stateNames.put(BATTERY_STATUS, BATTERY_STATUS_STATE);
        stateNames.put(BATTERY_LEVEL, BATTERY_LEVEL_STATE);
        stateNames.put(SIREN_STATUS, SIREN_STATUS_STATE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
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
