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

import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerPlayURL} handles the PlayURL channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerPlayURL extends HeosChannelHandler {

    public HeosChannelHandlerPlayURL(HeosBridgeHandler bridge, HeosFacade api) {
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
        // not used on bridge
    }

    private void handleCommand() {
        try {
            URL url = new URL(command.toString());
            api.playURL(id, url);
        } catch (MalformedURLException e) {
            logger.debug("Command '{}' is not a propper URL. Error: {}", command.toString(), e.getMessage());
        }
    }
}
