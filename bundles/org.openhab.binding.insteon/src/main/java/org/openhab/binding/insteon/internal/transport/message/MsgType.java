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
package org.openhab.binding.insteon.internal.transport.message;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents insteon message type flags
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
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
    NACK_OF_DIRECT(0xA0),
    ALL_LINK_BROADCAST(0xC0),
    ALL_LINK_CLEANUP(0x40),
    ALL_LINK_CLEANUP_ACK(0x60),
    ALL_LINK_CLEANUP_NACK(0xE0),
    INVALID(0xFF);

    private static final int FLAGS_MASK = 0xE0;

    private static final Map<Integer, MsgType> FLAGS_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(type -> type.flags, Function.identity()));

    private final int flags;

    private MsgType(int flags) {
        this.flags = flags;
    }

    public static MsgType valueOf(int flags) throws IllegalArgumentException {
        MsgType type = FLAGS_MAP.get(flags & FLAGS_MASK);
        if (type == null) {
            throw new IllegalArgumentException("unexpected msg flags value");
        }
        return type;
    }
}
