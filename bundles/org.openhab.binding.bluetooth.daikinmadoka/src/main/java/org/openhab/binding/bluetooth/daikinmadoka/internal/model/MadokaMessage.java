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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.BRC1HCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
@NonNullByDefault
public class MadokaMessage {

    private static final Logger logger = LoggerFactory.getLogger(MadokaMessage.class);

    /**
     * Maximum number of bytes per message chunk, including headers
     */
    public static final int MAX_CHUNK_SIZE = 20;

    private int messageId;
    private Map<Integer, MadokaValue> values;

    private MadokaMessage() {
        values = new HashMap<>();
    }

    public static byte[] createRequest(BRC1HCommand command, MadokaValue... parameters) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutputStream request = new DataOutputStream(output);

            // Message Length - Computed in the end
            request.writeByte(0);
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
            byte[] ret = output.toByteArray();
            ret[1] = (byte) (ret.length - 1);

            return ret;
        } catch (Exception e) {
            logger.info("Error while building request", e);
            return new byte[] {};
        }
    }

    public static MadokaMessage parse(byte[] msg) throws MadokaParsingException {
        if (msg.length < 1) {
            throw new MadokaParsingException("Message received is too short to be parsed.");
        }
        if (msg[0] != msg.length) {
            throw new MadokaParsingException("Message size is not valid (different from byte[0]).");
        }

        MadokaMessage m = new MadokaMessage();
        // m.messageId = ByteBuffer.wrap(msg, 2, 2).getShort();
        m.messageId = ((msg[0] & 0xff) << 8) | (msg[1] & 0xff);

        MadokaValue mv = null;

        for (int i = 4; i < msg.length;) {

            mv = new MadokaValue();
            mv.setId(msg[i]);
            mv.setSize(msg[i + 1]);

            if (mv.getSize() < 0) {
                logger.info("*** TO BE TRACKED *** NEGATIVE VALUE ***");
                mv.setSize(mv.getSize() + 128);
            }

            mv.setRawValue(Arrays.copyOfRange(msg, i + 2, i + 2 + mv.getSize()));

            i += 2 + mv.getSize();

            m.values.put(mv.getId(), mv);
        }

        return m;
    }

    public int getMessageId() {
        return messageId;
    }

    public Map<Integer, MadokaValue> getValues() {
        return values;
    }

    public static int expectedMessageChunks(byte[] firstMessage) {
        if (firstMessage.length < 2) {
            return -1;
        }

        if (firstMessage[0] != 0) {
            // This is not the first message so cannot be used
            return -1;
        }

        int expectedTotalBytes = firstMessage[1];

        return ((expectedTotalBytes / (MAX_CHUNK_SIZE - 1))
                + ((expectedTotalBytes % (MAX_CHUNK_SIZE - 1)) > 0 ? 1 : 0));

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
