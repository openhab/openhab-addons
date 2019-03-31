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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;

/**
 * The {@link SomfyTahomaDoorLockHandler} is responsible for handling commands,
 * which are sent to one of the channels of the door lock thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDoorLockHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaDoorLockHandler.class);

    public SomfyTahomaDoorLockHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
            put(OPEN, "core:OpenClosedState");
            put(LOCK, "core:LockedUnlockedState");
        }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("DoorLock channel: {} received command: {}", channelUID.getId(),command.toString());
        if (OPEN.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendCommand(command.equals(OnOffType.ON) ? "open" : "close", "[]");
        }
        if (LOCK.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendCommand(command.equals(OnOffType.ON) ? "lock" : "unlock", "[]");
        }
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }
    }
}
