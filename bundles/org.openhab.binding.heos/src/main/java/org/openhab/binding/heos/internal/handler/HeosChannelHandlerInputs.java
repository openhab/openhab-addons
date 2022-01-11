/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandlerInputs} handles the Input channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerInputs extends BaseHeosChannelHandler {
    protected final Logger logger = LoggerFactory.getLogger(HeosChannelHandlerInputs.class);

    private final HeosEventListener eventListener;

    public HeosChannelHandlerInputs(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id);
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }
        handleCommand(command, id);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            @Nullable
            Media payload = getApi().getNowPlayingMedia(id).payload;
            if (payload != null) {
                eventListener.playerMediaChangeEvent(id, payload);
            }
            return;
        }

        Map<String, String> selectedPlayers = bridge.getSelectedPlayer();
        if (selectedPlayers.isEmpty()) {
            // no selected player, just play it from the player itself
            getApi().playInputSource(id, command.toString());
        } else if (selectedPlayers.size() > 1) {
            logger.debug("Only one source can be selected for HEOS Input. Selected amount of sources: {} ",
                    selectedPlayers.size());
        } else {
            for (String sourcePid : selectedPlayers.keySet()) {
                getApi().playInputSource(id, sourcePid, command.toString());
            }
        }
    }
}
