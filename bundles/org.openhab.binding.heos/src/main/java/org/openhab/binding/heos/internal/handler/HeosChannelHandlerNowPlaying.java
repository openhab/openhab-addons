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
package org.openhab.binding.heos.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerNowPlaying} handles the refresh commands
 * coming from the implementing thing
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerNowPlaying extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerNowPlaying(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id);
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }

        handleCommand(command, id);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // No such channel on bridge
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            // TODO consider caching this somehow, this method is triggered from a lot of channels for the same player
            @Nullable
            Media payload = getApi().getNowPlayingMedia(id).payload;
            if (payload != null) {
                eventListener.playerMediaChangeEvent(id, payload);
            }
        }
    }
}
