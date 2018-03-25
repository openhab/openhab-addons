/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * @author Johannes Einig - initial contributor
 *
 */
public class HeosChannelHandlerPlayURL extends HeosChannelHandler {

    /**
     * @param bridge
     * @param api
     */
    public HeosChannelHandlerPlayURL(HeosBridgeHandler bridge, HeosFacade api) {
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
        handleCommand(command);
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
        handleCommand(command);
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
        if (!bridge.getSelectedPlayerList().isEmpty()) {
            for (int i = 0; i < bridge.getSelectedPlayerList().size(); i++) {
                this.id = bridge.getSelectedPlayerList().get(i)[0];
                handleCommand(command);
            }
        }
        bridge.resetPlayerList(channelUID);
    }

    private void handleCommand(Command command) {
        try {
            URL url = new URL(command.toString());
            api.playURL(id, url);
        } catch (MalformedURLException e) {
            logger.debug("Command '{}' is not a propper URL. Error: {}", command.toString(), e.getMessage());
        }
    }

}
