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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PartitionCommand} Enum representing the possible commands for a partition with the respective integer
 * values that are sent as nibbles in the packet.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public enum PartitionCommand {
    UNKNOWN(0),
    ARM(2),
    STAY_ARM(3),
    INSTANT_ARM(4),
    FORCE_ARM(5),
    DISARM(6),
    BEEP(8);

    private static final Logger logger = LoggerFactory.getLogger(PartitionCommand.class);

    private int command;

    PartitionCommand(int command) {
        this.command = command;
    }

    public int getCommand() {
        return command;
    }

    public static PartitionCommand parse(String command) {
        try {
            return PartitionCommand.valueOf(command);
        } catch (IllegalArgumentException e) {
            logger.debug("Unable to parse command={}. Fallback to UNKNOWN.", command);
            return PartitionCommand.UNKNOWN;
        }
    }
}
