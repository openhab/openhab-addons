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
package org.openhab.binding.heos.internal.json.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tuple to contain a command group and command enum, this represents the full command send to / received by the HEOS
 * cli
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosCommandTuple {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeosCommandTuple.class);

    public final HeosCommandGroup commandGroup;
    public final HeosCommand command;

    public HeosCommandTuple(HeosCommandGroup commandGroup, HeosCommand command) {
        this.commandGroup = commandGroup;
        this.command = command;
    }

    @Nullable
    public static HeosCommandTuple valueOf(String commandString) {
        String[] split = commandString.split("/");

        if (split.length != 2) {
            return null;
        }

        try {
            HeosCommandGroup group = HeosCommandGroup.valueOf(split[0].toUpperCase());
            HeosCommand cmd = HeosCommand.valueOf(split[1].toUpperCase());
            return new HeosCommandTuple(group, cmd);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unsupported command {}", commandString);
            return null;
        }
    }

    @Override
    public String toString() {
        return commandGroup + "/" + command;
    }
}
