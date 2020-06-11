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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.handler.HeosGroupHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.resources.HeosGroup;

/**
 * The {@link HeosChannelHandlerGrouping} handles the grouping channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosChannelHandlerGrouping extends HeosChannelHandler {

    public HeosChannelHandlerGrouping(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    @Override
    protected void handleCommandPlayer() {
        // No such channel on player
    }

    @Override
    protected void handleCommandGroup() {
        if (command instanceof RefreshType) {
            HeosGroupHandler heosGroupHandler = (HeosGroupHandler) handler;
            heosGroupHandler.initialize();
            return;
        }
        if (command.equals(OnOffType.OFF)) {
            api.ungroupGroup(id);
        } else if (command.equals(OnOffType.ON)) {
            HeosGroupHandler heosGroupHandler = (HeosGroupHandler) handler;
            HeosGroup heosGroup = heosGroupHandler.getHeosGroup();
            String[] playerArray = heosGroup.getGroupMemberPidList().toArray(new String[0]);
            api.groupPlayer(playerArray);
        }
    }

    @Override
    protected void handleCommandBridge() {
        // No such channel on Bridge
    }
}
