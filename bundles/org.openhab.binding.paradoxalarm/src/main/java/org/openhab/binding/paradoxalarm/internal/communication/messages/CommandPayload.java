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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CommandPayload} Class that structures the payload for partition commands.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class CommandPayload implements IPayload {

    private static final int BYTES_LENGTH = 15;

    private final byte MESSAGE_START = 0x40;
    private final byte PAYLOAD_SIZE = 0x0f;
    private final byte[] EMPTY_FOUR_BYTES = { 0, 0, 0, 0 };
    private final byte CHECKSUM = 0;

    private final int partitionNumber;
    private final PartitionCommand command;

    public CommandPayload(int partitionNumber, PartitionCommand command) {
        this.partitionNumber = partitionNumber;
        this.command = command;
    }

    @Override
    public byte[] getBytes() {
        byte[] bufferArray = new byte[BYTES_LENGTH];
        ByteBuffer buf = ByteBuffer.wrap(bufferArray);
        buf.put(MESSAGE_START);
        buf.put(PAYLOAD_SIZE);
        buf.put(EMPTY_FOUR_BYTES);
        buf.put(calculateMessageBytes());
        buf.put(EMPTY_FOUR_BYTES);
        buf.put(CHECKSUM);
        return bufferArray;
    }

    /*
     * The message bytes contain nibbles of command information. First byte, first nibble is partition 1, first byte,
     * second nibble is partition 2, second byte, first nibble is partition 3, etc...
     *
     * For command values that are set in byte nibbles, see PartitionCommand enum
     */
    private byte[] calculateMessageBytes() {
        byte[] result = { 0, 0, 0, 0 };
        int index = (partitionNumber - 1) / 2;
        result[index] = (byte) (calculateNibbleToSet() & 0xff);
        return result;
    }

    private int calculateNibbleToSet() {
        if ((partitionNumber - 1) % 2 == 0) {
            return (command.getCommand() << 4) & 0xF0;
        } else {
            return command.getCommand() & 0x0F;
        }
    }
}
