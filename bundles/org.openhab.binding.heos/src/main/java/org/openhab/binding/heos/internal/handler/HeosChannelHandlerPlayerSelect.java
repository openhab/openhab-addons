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

import static org.openhab.binding.heos.internal.resources.HeosConstants.PID;

import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerPlayerSelect} handles the player selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosChannelHandlerPlayerSelect extends HeosChannelHandler {

    public HeosChannelHandlerPlayerSelect(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    @Override
    protected void handleCommandPlayer() {
        // not used on player
    }

    @Override
    protected void handleCommandGroup() {
        // not used on group
    }

    @Override
    protected void handleCommandBridge() {
        if (command instanceof RefreshType) {
            return;
        }
        List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
        Channel channel = bridge.getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("Channel {} not found", channelUID.toString());
            return;
        }

        if (command.equals(OnOffType.ON)) {
            String[] selectedPlayerInfo = new String[2];
            selectedPlayerInfo[0] = channel.getProperties().get(PID);
            selectedPlayerInfo[1] = channelUID.getId();
            selectedPlayerList.add(selectedPlayerInfo);
        } else if (!selectedPlayerList.isEmpty()) {
            int indexPlayerChannel = -1;
            for (int i = 0; i < selectedPlayerList.size(); i++) {
                String localPID = selectedPlayerList.get(i)[0];
                if (localPID == channel.getProperties().get(PID)) {
                    indexPlayerChannel = i;
                }
            }
            selectedPlayerList.remove(indexPlayerChannel);
            bridge.setSelectedPlayerList(selectedPlayerList);
        }
    }
}
