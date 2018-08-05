/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.resources.HeosConstants;

/**
 * The {@link HeosChannelHandlerRepeatMode} handles the RepeatMode channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerRepeatMode extends HeosChannelHandler {

    public HeosChannelHandlerRepeatMode(HeosBridgeHandler bridge, HeosFacade api) {
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
        // Do nothing
    }

    private void handleCommand() {
        if (HeosConstants.HEOS_UI_ALL.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.HEOS_REPEAT_ALL);
        } else if (HeosConstants.HEOS_UI_ONE.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.HEOS_REPEAT_ONE);
        } else if (HeosConstants.HEOS_UI_OFF.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.HEOS_OFF);
        }
    }
}
