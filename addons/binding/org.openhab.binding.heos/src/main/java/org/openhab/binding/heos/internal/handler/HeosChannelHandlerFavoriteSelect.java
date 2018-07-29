/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.FAVORIT_SID;

import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerFavoriteSelect} handles the favorite selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerFavoriteSelect extends HeosChannelHandler {

    public HeosChannelHandlerFavoriteSelect(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandPlayer(org.eclipse.smarthome.
     * core.types.Command)
     */
    @Override
    protected void handleCommandPlayer() {
        // not used on player
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandGroup(org.eclipse.smarthome.core
     * .types.Command)
     */
    @Override
    protected void handleCommandGroup() {
        // not used on group
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandBridge(org.eclipse.smarthome.
     * core.types.Command, org.eclipse.smarthome.core.thing.ChannelUID)
     */
    @Override
    protected void handleCommandBridge() {
        if (command.equals(OnOffType.ON)) {
            List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
            if (!selectedPlayerList.isEmpty()) {
                for (int i = 0; i < selectedPlayerList.size(); i++) {
                    String pid = selectedPlayerList.get(i)[0];
                    String mid = channelUID.getId(); // the channel ID represents the MID of the favorite
                    api.playStation(pid, FAVORIT_SID, null, mid, null);
                }
            }
            bridge.resetPlayerList(channelUID);
        }
    }
}
