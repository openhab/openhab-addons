/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SyncTimePayload} builds the 13-byte EVO serial command to set the panel clock (command 0x30).
 * Seconds are not supported by the protocol.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SyncTimePayload implements IPayload {

    private static final int LENGTH = 13;
    private static final byte COMMAND = 0x30;
    private static final byte PAYLOAD_LENGTH = 0x0D;

    private final ZonedDateTime time;

    public SyncTimePayload(ZonedDateTime time) {
        this.time = time;
    }

    @Override
    public byte[] getBytes() {
        byte[] buf = new byte[LENGTH];
        buf[0] = COMMAND;
        buf[1] = PAYLOAD_LENGTH;
        // bytes 2-5: padding (zero)
        buf[6] = (byte) (time.getYear() / 100);
        buf[7] = (byte) (time.getYear() % 100);
        buf[8] = (byte) time.getMonthValue();
        buf[9] = (byte) time.getDayOfMonth();
        buf[10] = (byte) time.getHour();
        buf[11] = (byte) time.getMinute();
        // buf[12] left as 0x00 — ParadoxIPPacket(byte[]) overwrites the last byte with the checksum
        return buf;
    }
}
