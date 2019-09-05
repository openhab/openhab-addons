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

import org.eclipse.smarthome.core.types.RefreshType;
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
        if (command instanceof RefreshType) {
            api.getHeosPlayerRepeatMode(id);
            return;
        }
        if (HeosConstants.HEOS_UI_ALL.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.REPEAT_ALL);
        } else if (HeosConstants.HEOS_UI_ONE.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.REPEAT_ONE);
        } else if (HeosConstants.HEOS_UI_OFF.equalsIgnoreCase(command.toString())) {
            api.setRepeatMode(id, HeosConstants.OFF);
        }
    }
}
