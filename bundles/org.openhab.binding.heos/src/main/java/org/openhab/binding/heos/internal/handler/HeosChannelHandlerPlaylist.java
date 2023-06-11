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

import static org.openhab.binding.heos.internal.HeosBindingConstants.CH_ID_PLAYLISTS;
import static org.openhab.binding.heos.internal.resources.HeosConstants.PLAYLISTS_SID;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerPlaylist} handles the playlist selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerPlaylist extends BaseHeosChannelHandler {
    private final HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;

    public HeosChannelHandlerPlaylist(HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider,
            HeosBridgeHandler bridge) {
        super(bridge);
        this.heosDynamicStateDescriptionProvider = heosDynamicStateDescriptionProvider;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id, uid);
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }

        handleCommand(command, id, uid);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }

    private void handleCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        ChannelUID channelUID = new ChannelUID(uid, CH_ID_PLAYLISTS);
        if (command instanceof RefreshType) {
            heosDynamicStateDescriptionProvider.setPlaylists(channelUID, getApi().getPlaylists());
            return;
        }

        String idCommand = heosDynamicStateDescriptionProvider.getValueByLabel(channelUID, command.toString());
        getApi().addContainerToQueuePlayNow(id, PLAYLISTS_SID, idCommand);
    }
}
