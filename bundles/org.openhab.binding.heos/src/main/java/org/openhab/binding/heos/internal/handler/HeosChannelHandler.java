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
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;

/**
 * The {@link HeosChannelHandler} handles the base class for the different
 * channel handler which handles the command from the channels of the things
 * to the HEOS system
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public interface HeosChannelHandler {
    /**
     * Handle a command received from a channel. Requires the class which
     * wants to handle the command to decide which subclass has to be used
     * 
     * @param command the command to handle
     * @param id of the group or player
     * @param uid
     */
    void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException;

    void handleGroupCommand(Command command, @Nullable String id, ThingUID uid, HeosGroupHandler heosGroupHandler)
            throws IOException, ReadException;

    /**
     * Handles a command for classes without an id. Used
     * for BridgeHandler
     *
     * @param command the command to handle
     * @param uid
     */
    void handleBridgeCommand(Command command, ThingUID uid) throws IOException, ReadException;
}
