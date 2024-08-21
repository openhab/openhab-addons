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
package org.openhab.binding.max.internal.message;

import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The MaxTokenizer parses a L message into the MAX! devices encoded within. The L message contains
 * real time information for multiple devices. Each device starts with the length n bytes.
 * The MaxTokenzier starts with the first device and chops off one device after another from the byte stream.
 *
 * The tokens returned consist of the payload solely, and do not contain the first byte holding the
 * tokens length.
 *
 * @author Andreas Heil - Initial contribution
 */
@NonNullByDefault
public final class MaxTokenizer implements Enumeration<byte[]> {

    private int offset;

    private byte[] decodedRawMessage;

    /**
     * Creates a new MaxTokenizer.
     *
     * @param decodedRawMessage
     *            The Base64 decoded MAX! Cube protocol L message as byte array
     */
    public MaxTokenizer(byte[] decodedRawMessage) {
        this.decodedRawMessage = decodedRawMessage;
    }

    @Override
    public boolean hasMoreElements() {
        return offset < decodedRawMessage.length;
    }

    @Override
    public byte[] nextElement() {
        byte length = decodedRawMessage[offset++];

        // make sure to get the correct length in case > 127
        byte[] token = new byte[length & 0xFF];

        for (int i = 0; i < (length & 0xFF); i++) {
            token[i] = decodedRawMessage[offset++];
        }

        return token;
    }
}
