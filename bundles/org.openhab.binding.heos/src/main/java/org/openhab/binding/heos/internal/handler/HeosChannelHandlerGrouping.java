/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link HeosChannelHandlerGrouping} handles the grouping channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerGrouping extends BaseHeosChannelHandler {
    public HeosChannelHandlerGrouping(HeosBridgeHandler bridge) {
        super(bridge);
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) {
        // No such channel on player
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            HeosGroupHandler heosGroupHandler) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            return;
        }
        if (OnOffType.OFF == command) {
            if (id == null) {
                throw new HeosNotFoundException();
            }
            getApi().ungroupGroup(id);
        } else if (OnOffType.ON == command) {
            getApi().groupPlayer(heosGroupHandler.getGroupMemberPidList());
        }
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // No such channel on Bridge
    }
}
