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
package org.openhab.binding.heos.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerMute} handles the Mute channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerMute extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerMute(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getPlayerMuteState(id));
            return;
        }
        if (command.equals(OnOffType.ON)) {
            getApi().muteON(id);
        } else if (command.equals(OnOffType.OFF)) {
            getApi().muteOFF(id);
        }
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }

        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getGroupMuteState(id));
            return;
        }
        if (command.equals(OnOffType.ON)) {
            getApi().muteGroupON(id);
        } else if (command.equals(OnOffType.OFF)) {
            getApi().muteGroupOFF(id);
        }
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // No such channel on bridge
    }
}
