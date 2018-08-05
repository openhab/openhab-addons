/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.PLAYLISTS_SID;

import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerPlaylist} handles the playlist selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerPlaylist extends HeosChannelHandler {

    public HeosChannelHandlerPlaylist(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    @Override
    protected void handleCommandPlayer() {
        handleCommand();
    }

    @Override
    protected void handleCommandGroup() {
        handleCommand();
    }

    @Override
    protected void handleCommandBridge() {
        // not used on bridge
    }

    private void handleCommand() {
        String cid = bridge.getHeosPlaylists().get(Integer.valueOf(command.toString()));
        api.addContainerToQueuePlayNow(id, PLAYLISTS_SID, cid);
    }
}
