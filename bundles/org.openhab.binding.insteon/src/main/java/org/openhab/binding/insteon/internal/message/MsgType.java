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
package org.openhab.binding.insteon.internal.message;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents insteon message type flags
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public enum MsgType {
    /*
     * From the official Insteon docs: the message flags are as follows:
     *
     * Bit 0 max hops low bit
     * Bit 1 max hops high bit
     * Bit 2 hops left low bit
     * Bit 3 hops left high bit
     * Bit 4 0: is standard message, 1: is extended message
     * Bit 5 ACK
     * Bit 6 0: not link related, 1: is ALL-Link message
     * Bit 7 Broadcast/NAK
     */
    BROADCAST(0x80),
    DIRECT(0x00),
    ACK_OF_DIRECT(0x20),
    NACK_OF_DIRECT(0xa0),
    ALL_LINK_BROADCAST(0xc0),
    ALL_LINK_CLEANUP(0x40),
    ALL_LINK_CLEANUP_ACK(0x60),
    ALL_LINK_CLEANUP_NACK(0xe0),
    INVALID(0xff); // should never happen

    private static Map<Integer, MsgType> hash = new HashMap<>();

    private byte byteValue = 0;

    /**
     * Constructor
     *
     * @param b byte with insteon message type flags set
     */
    MsgType(int b) {
        this.byteValue = (byte) b;
    }

    static {
        for (MsgType t : MsgType.values()) {
            int i = t.getByteValue() & 0xff;
            hash.put(i, t);
        }
    }

    private int getByteValue() {
        return byteValue;
    }

    public static MsgType fromValue(byte b) throws IllegalArgumentException {
        int i = b & 0xe0;
        MsgType mt = hash.get(i);
        if (mt == null) {
            throw new IllegalArgumentException("msg type of byte value " + i + " not found");
        }
        return mt;
    }
}
