/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;

/**
 * The {@link HeosChannelHandlerControl} handles the control commands
 * coming from the implementing thing
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerControl extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerControl(HeosEventListener eventListener, HeosBridgeHandler bridge) {
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
        // No such channel within bridge
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getPlayState(id));
            return;
        }
        switch (command.toString()) {
            case "PLAY":
            case "ON":
                getApi().play(id);
                break;
            case "PAUSE":
            case "OFF":
                getApi().pause(id);
                break;
            case "NEXT":
                getApi().next(id);
                break;
            case "PREVIOUS":
                getApi().previous(id);
                break;
        }
    }
}
