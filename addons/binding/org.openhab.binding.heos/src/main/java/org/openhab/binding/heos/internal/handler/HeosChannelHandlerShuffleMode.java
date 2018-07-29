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
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.resources.HeosConstants;

/**
 * The {@link HeosChannelHandlerShuffleMode} handles the SchuffelModechannel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerShuffleMode extends HeosChannelHandler {

    public HeosChannelHandlerShuffleMode(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandPlayer()
     */
    @Override
    protected void handleCommandPlayer() {
        setShuffleMode();
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandGroup()
     */
    @Override
    protected void handleCommandGroup() {
        setShuffleMode();
    }

    /*
     * @see
     * org.openhab.binding.heos.internal.handler.HeosChannelHandler#handleCommandBridge()
     */
    @Override
    protected void handleCommandBridge() {
        // Do nothing
    }

    private void setShuffleMode() {
        if (command.equals(OnOffType.ON)) {
            api.setShuffleMode(id, HeosConstants.HEOS_ON);
        } else if (command.equals(OnOffType.OFF)) {
            api.setShuffleMode(id, HeosConstants.HEOS_OFF);
        }
    }
}
