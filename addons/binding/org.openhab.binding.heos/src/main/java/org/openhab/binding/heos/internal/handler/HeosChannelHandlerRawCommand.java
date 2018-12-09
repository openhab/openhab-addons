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
 *
 * The {@link HeosChannelHandlerRawCommand} handles the RawCommand channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerRawCommand extends HeosChannelHandler {

    public HeosChannelHandlerRawCommand(HeosBridgeHandler bridge, HeosFacade api) {
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
        api.sendRawCommand(command.toString());
    }
}
