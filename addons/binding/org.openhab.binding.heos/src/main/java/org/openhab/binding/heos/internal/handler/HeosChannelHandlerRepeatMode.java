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
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerRepeatMode extends HeosChannelHandler {

    /**
     * @param bridge
     * @param api
     */
    public HeosChannelHandlerRepeatMode(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandPlayer()
     */
    @Override
    protected void handleCommandPlayer() {
        setRepeatMode();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandGroup()
     */
    @Override
    protected void handleCommandGroup() {
        setRepeatMode();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandBridge()
     */
    @Override
    protected void handleCommandBridge() {
        // Do nothing
    }

    private void setRepeatMode() {
        if (command.toString().equalsIgnoreCase(HeosConstants.HEOS_UI_ALL)) {
            api.setRepeatMode(id, HeosConstants.HEOS_REPEAT_ALL);
        } else if (command.toString().equalsIgnoreCase(HeosConstants.HEOS_UI_ONE)) {
            api.setRepeatMode(id, HeosConstants.HEOS_REPEAT_ONE);
        } else if (command.toString().equalsIgnoreCase(HeosConstants.HEOS_UI_OFF)) {
            api.setRepeatMode(id, HeosConstants.HEOS_OFF);
        }
    }
}
