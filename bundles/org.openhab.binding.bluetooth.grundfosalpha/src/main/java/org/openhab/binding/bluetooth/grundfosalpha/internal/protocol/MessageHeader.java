/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * This defines the protocol header.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MessageHeader {

    /**
     * Header length including {@link MessageStartDelimiter} and size byte.
     */
    public static final int LENGTH = 5;

    private static final byte OFFSET_START_DELIMITER = 0;
    private static final byte OFFSET_LENGTH = 1;
    private static final byte OFFSET_SOURCE_ADDRESS = 2;
    private static final byte OFFSET_DESTINATION_ADDRESS = 3;
    private static final byte OFFSET_HEADER4 = 4;

    /**
     * Address of the controller/client (openHAB).
     */
    private static final byte CONTROLLER_ADDRESS = (byte) 0xe7;

    /**
     * Address of the peripheral/server (pump).
     */
    private static final byte PERIPHERAL_ADDRESS = (byte) 0xf8;

    /**
     * Last byte in header used for flowhead/power requests/responses.
     * Not sure about meaning.
     */
    private static final byte HEADER4_VALUE = (byte) 0x0a;

    /**
     * Fill in header for a request.
     *
     * @param request Request buffer
     * @param messageLength The request size excluding {@link MessageStartDelimiter}, size byte and CRC-16 checksum
     */
    public static void setRequestHeader(byte[] request, int messageLength) {
        if (request.length < LENGTH) {
            throw new IllegalArgumentException("Buffer is too small for header");
        }

        request[OFFSET_START_DELIMITER] = MessageStartDelimiter.Request.value();
        request[OFFSET_LENGTH] = (byte) messageLength;
        request[OFFSET_SOURCE_ADDRESS] = CONTROLLER_ADDRESS;
        request[OFFSET_DESTINATION_ADDRESS] = PERIPHERAL_ADDRESS;
        request[OFFSET_HEADER4] = HEADER4_VALUE;
    }

    /**
     * Check if this packet is the first packet in a response payload.
     *
     * @param packet The packet to inspect
     * @return true if determined to be first packet, otherwise false
     */
    public static boolean isInitialResponsePacket(byte[] packet) {
        return packet.length >= LENGTH && packet[OFFSET_START_DELIMITER] == MessageStartDelimiter.Reply.value()
                && packet[OFFSET_SOURCE_ADDRESS] == PERIPHERAL_ADDRESS
                && packet[OFFSET_DESTINATION_ADDRESS] == CONTROLLER_ADDRESS && packet[OFFSET_HEADER4] == HEADER4_VALUE;
    }

    /**
     * Get total size of message including {@link MessageStartDelimiter}, size byte and CRC-16 checksum.
     *
     * @param header Header bytes (at least)
     * @return total size
     */
    public static int getTotalSize(byte[] header) {
        return ((byte) header[MessageHeader.OFFSET_LENGTH]) + 4;
    }
}
