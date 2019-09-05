/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.types.RefreshType;
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
        if (command instanceof RefreshType) {
            api.getHeosPlayerVolume(id);
            return;
        }
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
        if (command instanceof RefreshType) {
            api.getHeosGroupVolume(id);
            return;
        }
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
