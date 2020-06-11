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

import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerBuildGroup} handles the BuidlGroup channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution *
 */
public class HeosChannelHandlerBuildGroup extends HeosChannelHandler {

    public HeosChannelHandlerBuildGroup(HeosBridgeHandler bridge, HeosFacade api) {
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
            bridge.resetPlayerList(channelUID);
            return;
        }
        if (command.equals(OnOffType.ON)) {
            List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
            if (!selectedPlayerList.isEmpty()) {
                String[] player = new String[selectedPlayerList.size()];
                for (int i = 0; i < selectedPlayerList.size(); i++) {
                    player[i] = selectedPlayerList.get(i)[0];
                }
                api.groupPlayer(player);
                bridge.resetPlayerList(channelUID);
            }
        }
    }
}
