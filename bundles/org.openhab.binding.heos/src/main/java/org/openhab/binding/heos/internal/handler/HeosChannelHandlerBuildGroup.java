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

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerBuildGroup} handles the BuidlGroup channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution *
 */
@NonNullByDefault
public class HeosChannelHandlerBuildGroup extends BaseHeosChannelHandler {
    private final ChannelUID channelUID;

    public HeosChannelHandlerBuildGroup(ChannelUID channelUID, HeosBridgeHandler bridge) {
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
    public void handleBridgeCommand(Command command, ThingUID uid) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            bridge.resetPlayerList(channelUID);
            return;
        }

        if (command == OnOffType.ON) {
            List<String[]> selectedPlayerList = bridge.getSelectedPlayerList();
            if (!selectedPlayerList.isEmpty()) {
                getApi().groupPlayer(selectedPlayerList.stream().map(a -> a[0]).toArray(String[]::new));
                bridge.resetPlayerList(channelUID);
            }
        }
    }
}
