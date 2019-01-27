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

/**
 * The {@link HeosChannelHandlerInputs} handles the Input channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerInputs extends HeosChannelHandler {

    public HeosChannelHandlerInputs(HeosBridgeHandler bridge, HeosFacade api) {
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
        if (command instanceof RefreshType) {
            api.getNowPlayingMedia(id);
            return;
        }
        if (bridge.getSelectedPlayer().isEmpty()) {
            api.playInputSource(id, command.toString());
        } else if (bridge.getSelectedPlayer().size() > 1) {
            logger.debug("Only one source can be selected for HEOS Input. Selected amount of sources: {} ",
                    bridge.getSelectedPlayer().size());
        } else {
            for (String sourcePid : bridge.getSelectedPlayer().keySet()) {
                api.playInputSource(id, sourcePid, command.toString());
            }
        }
        bridge.getSelectedPlayer().clear();
    }
}
