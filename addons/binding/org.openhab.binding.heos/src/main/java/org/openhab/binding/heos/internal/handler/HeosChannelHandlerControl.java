/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerControl} handles the control commands
 * coming from the implementing thing
 *
 * @author Johannes Einig - Initial contribution
 *
 */

public class HeosChannelHandlerControl extends HeosChannelHandler {

    public HeosChannelHandlerControl(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    /*
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandPlayer(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandPlayer() {
        handleCommand(command);
    }

    /*
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandGroup(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandGroup() {
        handleCommand(command);
    }

    /*
     * @see
     * org.openhab.binding.heos.handler.factory.HeosChannelHandler#handleCommandBridge(org.eclipse.smarthome.core.types.
     * Command)
     */
    @Override
    protected void handleCommandBridge() {
        // No such channel within bridge
    }

    private void handleCommand(Command command) {
        switch (command.toString()) {
            case "PLAY":
                api.play(id);
                break;
            case "PAUSE":
                api.pause(id);
                break;
            case "NEXT":
                api.next(id);
                break;
            case "PREVIOUS":
                api.previous(id);
                break;
            case "ON":
                api.play(id);
                break;
            case "OFF":
                api.pause(id);
                break;
        }
    }
}
