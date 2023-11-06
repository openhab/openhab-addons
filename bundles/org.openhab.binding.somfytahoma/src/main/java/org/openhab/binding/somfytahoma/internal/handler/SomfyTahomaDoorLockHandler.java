/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.LOCK;
import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.OPEN;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link SomfyTahomaDoorLockHandler} is responsible for handling commands,
 * which are sent to one of the channels of the door lock thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDoorLockHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaDoorLockHandler(Thing thing) {
        super(thing);
        stateNames.put(OPEN, "core:OpenClosedState");
        stateNames.put(LOCK, "core:LockedUnlockedState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (OPEN.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendCommand(command.equals(OnOffType.ON) ? "open" : "close");
        }
        if (LOCK.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendCommand(command.equals(OnOffType.ON) ? "lock" : "unlock");
        }
    }
}
