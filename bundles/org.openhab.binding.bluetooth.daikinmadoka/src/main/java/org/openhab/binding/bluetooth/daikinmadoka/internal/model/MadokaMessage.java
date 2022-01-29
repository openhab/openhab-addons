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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.BRC1HCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a message transmitted or received from the BRC1H controller as a serial protocol
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class MadokaMessage {

    private static final Logger logger = LoggerFactory.getLogger(MadokaMessage.class);

    private int messageId;
    private final Map<Integer, MadokaValue> values;

    private byte @Nullable [] rawMessage;

    private MadokaMessage() {
        values = new HashMap<>();
    }

    public static byte[][] createRequest(BRC1HCommand command, MadokaValue... parameters) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutputStream request = new DataOutputStream(output);

            // Chunk ID
            // request.writeByte(0);

            // Message Length - Computed in the end - left at 0 for now
            request.writeByte(0);

            // Command ID, coded on 3 bytes
            request.writeByte(0);
            request.writeShort(command.getCommandId());

            if (parameters.length == 0) {
                request.writeByte(0);
                request.writeByte(0);
            } else {
                for (MadokaValue mv : parameters) {
                    request.writeByte(mv.getId());
                    request.writeByte(mv.getSize());
                    request.write(mv.getRawValue());
                }
            }

            // Finally, compute array size
            byte[] payload = output.toByteArray();
            payload[0] = (byte) (payload.length);

            // Now, split in chunks
            byte[][] chunks = new byte[(int) Math.ceil(payload.length / 19.)][0];

            ByteArrayInputStream left = new ByteArrayInputStream(payload);
            int chunkId = 0;
            while (left.available() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream chunk = new DataOutputStream(bos);
                chunk.writeByte(chunkId);
                chunk.write(left.readNBytes(19));

                chunk.flush();
                chunks[chunkId++] = bos.toByteArray();
            }

            return chunks;
        } catch (IOException e) {
            logger.info("Error while building request", e);
            throw new RuntimeException(e);
        }
    }

    public static MadokaMessage parse(byte[] msg) throws MadokaParsingException {
        // Msg format (bytes):
        // <Msg Length> <msg id> <msg id> <msg id> ...
        // So MINIMAL length is 4, to cover the message length + message ID
        if (msg.length < 4) {
            throw new MadokaParsingException("Message received is too short to be parsed.");
        }
        if (msg[0] != msg.length) {
            throw new MadokaParsingException("Message size is not valid (different from byte[0]).");
        }

        MadokaMessage m = new MadokaMessage();
        m.setRawMessage(msg);
        m.messageId = ByteBuffer.wrap(msg, 2, 2).getShort();

        MadokaValue mv = null;

        // Starting here, we are not on the safe side with previous msg.length check
        for (int i = 4; i < msg.length;) {
            if ((i + 1) >= msg.length) {
                throw new MadokaParsingException("Truncated message detected while parsing response value header");
            }

            mv = new MadokaValue();
            mv.setId(msg[i]);

            if (Byte.toUnsignedInt(msg[i + 1]) == 0xff) {
                // Specific case - msg length 0xFF. See GetOperationHousCommand
                mv.setSize(0);
            } else {
                mv.setSize(Byte.toUnsignedInt(msg[i + 1]));
            }

            if ((i + 1 + mv.getSize()) >= msg.length) {
                throw new MadokaParsingException("Truncated message detected while parsing response value content");
            }

            mv.setRawValue(Arrays.copyOfRange(msg, i + 2, i + 2 + mv.getSize()));

            i += 2 + mv.getSize();

            m.values.put(mv.getId(), mv);
        }

        return m;
    }

    private void setRawMessage(byte[] rawMessage) {
        this.rawMessage = rawMessage;
    }

    public byte @Nullable [] getRawMessage() {
        return this.rawMessage;
    }

    public int getMessageId() {
        return messageId;
    }

    public Map<Integer, MadokaValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("{ messageId: %d, values: [", this.messageId));

        for (Map.Entry<Integer, MadokaValue> entry : values.entrySet()) {
            sb.append(String.format(" { valueId: %d, valueSize: %d },", entry.getKey(), entry.getValue().getSize()));
        }

        sb.append("] }");
        return sb.toString();
    }
}
