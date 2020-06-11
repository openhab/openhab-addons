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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.FAVORIT_SID;

import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.RefreshType;
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
        if (command instanceof RefreshType) {
            bridge.resetPlayerList(channelUID);
            return;
        }
        if (command.equals(OnOffType.ON)) {
            List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
            if (!selectedPlayerList.isEmpty()) {
                for (int i = 0; i < selectedPlayerList.size(); i++) {
                    String pid = selectedPlayerList.get(i)[0];
                    String mid = channelUID.getId(); // the channel ID represents the MID of the favorite
                    api.playStation(pid, FAVORIT_SID, mid);
                }
            }
            bridge.resetPlayerList(channelUID);
        }
    }

    private void handleCommand() {
        if (OnOffType.ON.equals(command)) {
            api.playStation(id, FAVORIT_SID, channelUID.getId());
        }
    }
}
