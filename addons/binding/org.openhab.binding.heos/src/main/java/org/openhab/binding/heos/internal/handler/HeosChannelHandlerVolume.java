/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;

/**
 * The {@link HeosChannelHandlerVolume} handles the Volume channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */

public class HeosChannelHandlerVolume extends HeosChannelHandler {

    public HeosChannelHandlerVolume(HeosBridgeHandler bridge, HeosFacade api) {
        super(bridge, api);
    }

    @Override
    protected void handleCommandPlayer() {
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                api.increaseVolume(id);
            } else {
                api.decreaseVolume(id);
            }
        } else {
            api.setVolume(command.toString(), id);
        }
    }

    @Override
    protected void handleCommandGroup() {
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE.equals(command)) {
                api.increaseGroupVolume(id);
            } else {
                api.decreaseGroupVolume(id);
            }
        } else {
            api.volumeGroup(command.toString(), id);
        }
    }

    @Override
    protected void handleCommandBridge() {
        // not used on bridge
    }
}
