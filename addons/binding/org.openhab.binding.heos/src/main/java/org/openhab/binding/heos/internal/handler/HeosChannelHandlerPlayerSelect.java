/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.PID;

import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerPlayerSelect extends HeosChannelHandler {

    /**
     * @param bridge
     * @param api
     */
    public HeosChannelHandlerPlayerSelect(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandPlayer(org.eclipse.smarthome.
     * core.types.Command)
     */
    @Override
    protected void handleCommandPlayer() {
        // not used on player
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandGroup(org.eclipse.smarthome.core
     * .types.Command)
     */
    @Override
    protected void handleCommandGroup() {
        // not used on group

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.internal.channelHandler.HeosChannelHandler#handleCommandBridge(org.eclipse.smarthome.
     * core.types.Command, org.eclipse.smarthome.core.thing.ChannelUID)
     */
    @Override
    protected void handleCommandBridge() {
        ArrayList<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
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
        } else {
            if (!selectedPlayerList.isEmpty()) {
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

}
