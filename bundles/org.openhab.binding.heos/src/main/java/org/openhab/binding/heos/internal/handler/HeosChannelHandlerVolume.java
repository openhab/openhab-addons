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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerVolume} handles the Volume channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerVolume extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerVolume(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getPlayerVolume(id));
            return;
        }
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE == command) {
                getApi().increaseVolume(id);
            } else {
                getApi().decreaseVolume(id);
            }
        } else {
            getApi().setVolume(command.toString(), id);
        }
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }

        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getGroupVolume(id));
            return;
        }
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE == command) {
                getApi().increaseGroupVolume(id);
            } else {
                getApi().decreaseGroupVolume(id);
            }
        } else {
            getApi().volumeGroup(command.toString(), id);
        }
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }
}
