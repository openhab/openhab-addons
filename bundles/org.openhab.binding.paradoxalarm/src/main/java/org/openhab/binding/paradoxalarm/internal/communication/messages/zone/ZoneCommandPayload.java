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
package org.openhab.binding.paradoxalarm.internal.communication.messages.zone;

import java.nio.ByteBuffer;

import org.openhab.binding.paradoxalarm.internal.communication.messages.IPayload;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * @author Konstantin Polihronov - Initial contribution
 */
public class ZoneCommandPayload implements IPayload {

    private static final int BYTES_LENGTH = 31;

    private static final byte MESSAGE_START = (byte) 0xD0;
    private static final byte PAYLOAD_SIZE = 0x1f;
    private static final byte ZONE_FLAG = 0x08; // "bypassed" flag (5th bit)
    private static final byte[] EMPTY_TWO_BYTES = { 0, 0 };
    private static final byte CHECKSUM = 0;

    private int zoneNumber;
    private ZoneCommand command;

    public ZoneCommandPayload(int zoneNumber, ZoneCommand command) {
        this.zoneNumber = zoneNumber;
        this.command = command;
    }

    @Override
    public byte[] getBytes() {
        byte[] bufferArray = new byte[BYTES_LENGTH];
        ByteBuffer buf = ByteBuffer.wrap(bufferArray);
        buf.put(MESSAGE_START);
        buf.put(PAYLOAD_SIZE);
        buf.put(ZONE_FLAG);
        buf.put(command.getCommand());
        buf.put(EMPTY_TWO_BYTES);
        buf.put(calculateMessageBytes());
        buf.put(ParadoxUtil.calculateChecksum(bufferArray));
        return bufferArray;
    }

    /**
     * The total zone message consists of 24 bytes (8 bits each) which results of 192 bits for each zone in case of
     * Evo192 (i.e. 24x8).
     * The low nible of each byte represents the first 4 zones of each group as a bit, the high nible is
     * the second 4 zones. The zone groups are considered every 8 zones represented by a byte in this array (1-8,9-16,
     * 17-24, etc)<br>
     *
     * Example: So if we address zone 1 for example the value of first byte will be 0x01, for zone 2 - 0x02, for zone 3
     * - 0x04
     * (third bit set to 1), for zone 4 - 0x08(fourth bit set to 1),<br>
     * for zone 5 - 0x10, for zone 6 - 0x20, for zone 7 - 0x40, for zone 8 - 0x80.<br>
     * For examples see TestGetBytes.java
     *
     * @return 24 bytes array with the needed zone to be set to bypass/clear bypass
     */
    private byte[] calculateMessageBytes() {
        byte[] zoneMessage = new byte[24];
        int byteIndex = (zoneNumber - 1) / 8;
        byte zoneByteGroup = zoneMessage[byteIndex];
        int bitNumber = calculateBitNumber();
        zoneMessage[byteIndex] = ParadoxUtil.setBit(zoneByteGroup, bitNumber - 1, 1);
        return zoneMessage;
    }

    private int calculateBitNumber() {
        int residual = zoneNumber % 8;
        return residual != 0 ? residual : 8;
    }
}
