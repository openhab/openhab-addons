/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.handler.HeosGroupHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.resources.HeosGroup;

/**
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerGrouping extends HeosChannelHandler {

    /**
     * @param bridge
     * @param api
     */
    public HeosChannelHandlerGrouping(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandPlayer(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandPlayer() {
        // No such channel on player

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandGroup(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandGroup() {
        if (command.equals(OnOffType.OFF)) {
            api.ungroupGroup(id);
        } else if (command.equals(OnOffType.ON)) {
            HeosGroupHandler heosGroupHandler = HeosGroupHandler.class.cast(handler);
            HeosGroup heosGroup = heosGroupHandler.getHeosGroup();
            String[] playerArray = heosGroup.getGroupMemberPidList().toArray(new String[0]);
            api.groupPlayer(playerArray);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandBridge(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandBridge() {
        // No such channel on Bridge

    }

}
