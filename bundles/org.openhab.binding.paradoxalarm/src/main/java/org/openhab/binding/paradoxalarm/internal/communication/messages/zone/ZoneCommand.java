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
package org.openhab.binding.paradoxalarm.internal.communication.messages.zone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.paradoxalarm.internal.communication.IRequest;
import org.openhab.binding.paradoxalarm.internal.communication.RequestType;
import org.openhab.binding.paradoxalarm.internal.communication.ZoneCommandRequest;
import org.openhab.binding.paradoxalarm.internal.communication.messages.Command;
import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public enum ZoneCommand implements Command {
    CLEAR_BYPASS(0),
    BYPASS(8);

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneCommand.class);

    private byte command;

    ZoneCommand(int command) {
        this.command = (byte) command;
    }

    public byte getCommand() {
        return command;
    }

    public static @Nullable ZoneCommand parse(@Nullable String command) {
        if (command == null) {
            return null;
        }

        try {
            return ZoneCommand.valueOf(command);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unable to parse command={}. Fallback to UNKNOWN.", command);
            return null;
        }
    }

    @Override
    public IRequest getRequest(int zoneId) {
        ZoneCommandPayload payload = new ZoneCommandPayload(zoneId, this);
        ParadoxIPPacket packet = new ParadoxIPPacket(payload.getBytes(), false)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST);
        return new ZoneCommandRequest(RequestType.ZONE_COMMAND, packet, null);
    }
}
