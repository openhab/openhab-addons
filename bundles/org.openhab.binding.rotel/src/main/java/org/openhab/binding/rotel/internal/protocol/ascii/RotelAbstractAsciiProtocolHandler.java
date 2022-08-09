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
package org.openhab.binding.rotel.internal.protocol.ascii;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for handling a Rotel ASCII protocol (build of command messages, decoding of incoming data)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public abstract class RotelAbstractAsciiProtocolHandler extends RotelAbstractProtocolHandler {

    /** Special characters that can be found in the feedback messages for several devices using the ASCII protocol */
    public static final byte[][] SPECIAL_CHARACTERS = { { (byte) 0xEE, (byte) 0x82, (byte) 0x85 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x84 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x92 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x87 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8E },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x89 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x93 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8C }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8F },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8A }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8B },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x81 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x82 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x83 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x94 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x97 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x98 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x80 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x99 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x9A }, { (byte) 0xEE, (byte) 0x82, (byte) 0x88 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x95 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x96 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x90 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x91 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8D }, { (byte) 0xEE, (byte) 0x80, (byte) 0x80, (byte) 0xEE,
                    (byte) 0x80, (byte) 0x81, (byte) 0xEE, (byte) 0x80, (byte) 0x82 } };

    /** Special characters that can be found in the feedback messages for the RCD-1572 */
    public static final byte[][] SPECIAL_CHARACTERS_RCD1572 = { { (byte) 0xC2, (byte) 0x8C },
            { (byte) 0xC2, (byte) 0x54 }, { (byte) 0xC2, (byte) 0x81 }, { (byte) 0xC2, (byte) 0x82 },
            { (byte) 0xC2, (byte) 0x83 } };

    /** Empty table of special characters */
    public static final byte[][] NO_SPECIAL_CHARACTERS = {};

    private static final int MAX_SIZE_RESPONSE = 128;

    private final Logger logger = LoggerFactory.getLogger(RotelAbstractAsciiProtocolHandler.class);

    private final byte[] dataBuffer;

    private int index;

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     */
    public RotelAbstractAsciiProtocolHandler(RotelModel model) {
        super(model);
        this.dataBuffer = new byte[MAX_SIZE_RESPONSE];
        this.index = 0;
    }

    protected boolean fillDataBuffer(byte data) {
        if (index < MAX_SIZE_RESPONSE) {
            dataBuffer[index++] = data;
            return true;
        }
        return false;
    }

    protected byte[] getDataBuffer() {
        return Arrays.copyOf(dataBuffer, index);
    }

    protected void resetDataBuffer() {
        index = 0;
    }

    protected int getRemainingSizeInDataBuffer() {
        return MAX_SIZE_RESPONSE - index;
    }

    /**
     * Validate the content of a feedback message
     *
     * @param responseMessage the buffer containing the feedback message
     *
     * @throws RotelException - If the message has unexpected content
     */
    @Override
    protected void validateResponse(byte[] responseMessage) throws RotelException {
        // Check minimum message length
        if (responseMessage.length < 1) {
            logger.debug("Unexpected message length: {}", responseMessage.length);
            throw new RotelException("Unexpected message length");
        }
    }

    /**
     * Analyze a valid ASCII message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    @Override
    protected void handleValidMessage(byte[] incomingMessage) {
        byte[] message = filterMessage(incomingMessage, model.getSpecialCharacters());

        // Replace characters with code < 32 by a space before converting to a string
        for (int i = 0; i < message.length; i++) {
            if (message[i] < 0x20) {
                message[i] = 0x20;
            }
        }

        String value = new String(message, 0, message.length, StandardCharsets.US_ASCII);
        logger.debug("handleValidAsciiMessage: chars *{}*", value);
        value = value.trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            int idxSeparator = value.indexOf("=");
            if (idxSeparator < 0) {
                logger.debug("handleValidAsciiMessage: ignored message {}", value);
            } else {
                dispatchKeyValue(value.substring(0, idxSeparator).trim().toLowerCase(),
                        value.substring(idxSeparator + 1));
            }
        } catch (PatternSyntaxException e) {
            logger.debug("handleValidAsciiMessage: ignored message {}", value);
        }
    }

    /**
     * Suppress certain sequences of bytes from a message
     *
     * @param message the message as a table of bytes
     * @param bytesSequences the table containing the sequence of bytes to be ignored
     *
     * @return the message without the unexpected sequence of bytes
     */
    private byte[] filterMessage(byte[] message, byte[][] bytesSequences) {
        if (bytesSequences.length == 0) {
            return message;
        }
        byte[] filteredMsg = new byte[message.length];
        int srcIdx = 0;
        int dstIdx = 0;
        while (srcIdx < message.length) {
            int ignoredLength = 0;
            for (int i = 0; i < bytesSequences.length; i++) {
                int size = bytesSequences[i].length;
                if ((message.length - srcIdx) >= size) {
                    boolean match = true;
                    for (int j = 0; j < size; j++) {
                        if (message[srcIdx + j] != bytesSequences[i][j]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        ignoredLength = size;
                        break;
                    }
                }
            }
            if (ignoredLength > 0) {
                srcIdx += ignoredLength;
            } else {
                filteredMsg[dstIdx++] = message[srcIdx++];
            }
        }
        return Arrays.copyOf(filteredMsg, dstIdx);
    }
}
