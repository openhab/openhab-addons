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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The four-byte GENI transport header: start delimiter, length, destination address and source address.
 * Application data units follow this header; their class identifiers are not part of it.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GeniFrameHeader {

    /**
     * Header length including {@link GeniStartDelimiter} and size byte.
     */
    public static final int LENGTH = 4;

    private static final byte OFFSET_START_DELIMITER = 0;
    private static final byte OFFSET_LENGTH = 1;
    private static final byte OFFSET_DESTINATION_ADDRESS = 2;
    private static final byte OFFSET_SOURCE_ADDRESS = 3;

    /**
     * Address of the controller/client (openHAB).
     */
    private static final byte CONTROLLER_ADDRESS = (byte) 0xf8;

    /**
     * Address of the peripheral/server (pump).
     */
    private static final byte PERIPHERAL_ADDRESS = (byte) 0xe7;

    /**
     * Fill in header for a request.
     *
     * @param request Request buffer
     * @param messageLength The request size excluding {@link GeniStartDelimiter}, size byte and CRC-16 checksum
     */
    public static void setRequestHeader(byte[] request, int messageLength) {
        if (request.length < LENGTH) {
            throw new IllegalArgumentException("Buffer is too small for header");
        }
        if (messageLength < LENGTH - 2 || messageLength > 0xff) {
            throw new IllegalArgumentException("Invalid GENI message length");
        }

        request[OFFSET_START_DELIMITER] = GeniStartDelimiter.Request.value();
        request[OFFSET_LENGTH] = (byte) messageLength;
        request[OFFSET_DESTINATION_ADDRESS] = PERIPHERAL_ADDRESS;
        request[OFFSET_SOURCE_ADDRESS] = CONTROLLER_ADDRESS;
    }

    /**
     * Check if this packet is the first packet in a response payload.
     *
     * @param packet The packet to inspect
     * @return true if determined to be first packet, otherwise false
     */
    public static boolean isInitialResponsePacket(byte[] packet) {
        return packet.length >= LENGTH && packet[OFFSET_START_DELIMITER] == GeniStartDelimiter.Reply.value()
                && packet[OFFSET_DESTINATION_ADDRESS] == CONTROLLER_ADDRESS
                && packet[OFFSET_SOURCE_ADDRESS] == PERIPHERAL_ADDRESS;
    }

    /**
     * Get total size of message including {@link GeniStartDelimiter}, size byte and CRC-16 checksum.
     *
     * @param header Header bytes (at least)
     * @return total size
     */
    public static int getTotalSize(byte[] header) {
        if (header.length < LENGTH) {
            throw new IllegalArgumentException("Buffer is too small for header");
        }
        return Byte.toUnsignedInt(header[OFFSET_LENGTH]) + 4;
    }
}
