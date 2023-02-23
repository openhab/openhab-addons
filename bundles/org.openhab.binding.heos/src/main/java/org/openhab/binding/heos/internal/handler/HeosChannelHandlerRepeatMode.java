/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerRepeatMode} handles the RepeatMode channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerRepeatMode extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerRepeatMode(HeosEventListener eventListener, HeosBridgeHandler bridge) {
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
        // Do nothing
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getPlayMode(id));
            return;
        }

        if (HeosConstants.HEOS_UI_ALL.equalsIgnoreCase(command.toString())) {
            getApi().setRepeatMode(id, HeosConstants.REPEAT_ALL);
        } else if (HeosConstants.HEOS_UI_ONE.equalsIgnoreCase(command.toString())) {
            getApi().setRepeatMode(id, HeosConstants.REPEAT_ONE);
        } else if (HeosConstants.HEOS_UI_OFF.equalsIgnoreCase(command.toString())) {
            getApi().setRepeatMode(id, HeosConstants.OFF);
        }
    }
}
