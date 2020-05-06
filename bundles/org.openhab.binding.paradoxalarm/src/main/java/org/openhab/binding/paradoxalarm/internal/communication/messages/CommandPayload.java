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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;

/**
 * The {@link CommandPayload} Class that structures the payload for partition commands.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class CommandPayload implements IPayload {

    private final byte MESSAGE_START = 0x40;
    private final byte PAYLOAD_SIZE = 0x0f;
    private final byte[] EMPTY_FOUR_BYTES = { 0, 0, 0, 0 };
    private final byte CHECKSUM = 0;

    private int partitionNumber;
    private PartitionCommand command;

    public CommandPayload(int partitionNumber, PartitionCommand command) {
        this.partitionNumber = partitionNumber;
        this.command = command;
    }

    @Override
    public byte[] getBytes() {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            os.write(MESSAGE_START);
            os.write(PAYLOAD_SIZE);
            os.write(EMPTY_FOUR_BYTES);
            os.write(calculateMessageBytes());
            os.write(EMPTY_FOUR_BYTES);
            os.write(CHECKSUM);
            return os.toByteArray();
        } catch (IOException e) {
            throw new ParadoxRuntimeException("Unable to create byte array stream.", e);
        }
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
