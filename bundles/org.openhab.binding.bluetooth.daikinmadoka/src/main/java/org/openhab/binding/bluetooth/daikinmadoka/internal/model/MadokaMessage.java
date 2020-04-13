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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.BRC1HCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class MadokaMessage {

    private static final Logger logger = LoggerFactory.getLogger(MadokaMessage.class);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Maximum number of bytes per message chunk, including headers
     */
    public static final int MAX_CHUNK_SIZE = 20;

    private int messageId;
    private Map<Integer, MadokaValue> values;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private MadokaMessage() {
        values = new HashMap<>();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static byte[] createRequest(BRC1HCommand command, MadokaValue... parameters) {

        byte[] request = ArrayUtils.EMPTY_BYTE_ARRAY;

        // Message Length - Computed in the end
        request = ArrayUtils.add(request, (byte) 0);
        request = ArrayUtils.add(request, (byte) 0);

        // Command ID, coded on 3 bytes
        request = ArrayUtils.add(request, (byte) 0);
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort((short) command.getCommandId());
        request = ArrayUtils.addAll(request, bb.array());

        if (parameters == null || parameters.length == 0) {
            request = ArrayUtils.add(request, (byte) 0);
            request = ArrayUtils.add(request, (byte) 0);
        } else {

            for (MadokaValue mv : parameters) {
                request = ArrayUtils.add(request, (byte) mv.getId());
                request = ArrayUtils.add(request, (byte) mv.getSize());
                request = ArrayUtils.addAll(request, mv.getRawValue());
            }

        }

        // Finally, compute array size
        request[1] = (byte) (request.length - 1);

        return request;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static MadokaMessage parse(byte[] msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length < 1) {
            return null;
        }
        if (msg[0] != msg.length) {
            return null;
        }

        MadokaMessage m = new MadokaMessage();
        m.messageId = ByteBuffer.wrap(msg, 2, 2).getShort();

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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public int getMessageId() {
        return messageId;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public Map<Integer, MadokaValue> getValues() {
        return values;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static int expectedMessageChunks(byte[] firstMessage) {
        if (firstMessage == null || firstMessage.length < 2) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
