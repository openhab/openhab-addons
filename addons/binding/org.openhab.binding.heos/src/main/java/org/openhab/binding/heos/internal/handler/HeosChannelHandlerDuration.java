/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerDuration} handles the refresh commands
 * coming from the implementing thing
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerDuration extends HeosChannelHandler {

    public HeosChannelHandlerDuration(HeosBridgeHandler bridge, HeosFacade api) {
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
        // No such channel on bridge
    }

    private void handleCommand() {
        if (command instanceof RefreshType) {
            // Channel is refreshed automatically if song is played
            return;
        }
    }
}
