/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandlerPlayerSelect} handles the player selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerPlayerSelect extends BaseHeosChannelHandler {
    protected final Logger logger = LoggerFactory.getLogger(HeosChannelHandlerPlayerSelect.class);

    private final ChannelUID channelUID;

    public HeosChannelHandlerPlayerSelect(ChannelUID channelUID, HeosBridgeHandler bridge) {
        super(bridge);
        this.channelUID = channelUID;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) {
        // not used on player
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) {
        // not used on group
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        if (command instanceof RefreshType) {
            return;
        }
        Channel channel = bridge.getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("Channel {} not found", channelUID);
            return;
        }

        List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();

        if (command.equals(OnOffType.ON)) {
            String[] selectedPlayerInfo = new String[2];
            selectedPlayerInfo[0] = channel.getProperties().get(PID);
            selectedPlayerInfo[1] = channelUID.getId();
            selectedPlayerList.add(selectedPlayerInfo);
        } else if (!selectedPlayerList.isEmpty()) {
            int indexPlayerChannel = -1;
            for (int i = 0; i < selectedPlayerList.size(); i++) {
                String localPID = selectedPlayerList.get(i)[0];
                if (localPID.equals(channel.getProperties().get(PID))) {
                    indexPlayerChannel = i;
                }
            }
            selectedPlayerList.remove(indexPlayerChannel);
            bridge.setSelectedPlayerList(selectedPlayerList);
        }
    }
}
