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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private byte @Nullable [] rawMessage;

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
        } catch (IOException e) {
            logger.info("Error while building request", e);
            throw new RuntimeException(e);
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
        m.setRawMessage(msg);
        // m.messageId = ByteBuffer.wrap(msg, 2, 2).getShort();
        m.messageId = ((msg[0] & 0xff) << 8) | (msg[1] & 0xff);

        MadokaValue mv = null;

        for (int i = 4; i < msg.length;) {
            mv = new MadokaValue();
            mv.setId(msg[i]);
            mv.setSize(Byte.toUnsignedInt(msg[i + 1]));
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
