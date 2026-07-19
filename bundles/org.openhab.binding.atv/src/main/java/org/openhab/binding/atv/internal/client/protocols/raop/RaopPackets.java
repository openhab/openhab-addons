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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Packet formats used by RAOP.
 *
 * <p>
 * All packets are big-endian. Unsigned 8/16-bit fields are modelled as {@code int} and
 * unsigned 32-bit fields as {@code long}; values are masked on encode and read unsigned on
 * decode.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopPackets {

    private RaopPackets() {
    }

    private static ByteBuffer buffer(byte[] data, int length, boolean allowExcessive) {
        if (data.length < length || (!allowExcessive && data.length != length)) {
            throw new IllegalArgumentException("expected " + length + " bytes, got " + data.length);
        }
        return ByteBuffer.wrap(data, 0, length).order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * RTP header common to all RAOP packets.
     */
    public record RtpHeader(int proto, int type, int seqno) {

        /** Encoded size in bytes. */
        public static final int LENGTH = 4;

        /**
         * Encodes this header to its 4-byte wire format.
         */
        public byte[] encode() {
            ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.BIG_ENDIAN);
            putHeader(buf, proto, type, seqno);
            return buf.array();
        }

        /**
         * Decodes a header, requiring an exact-length input.
         */
        public static RtpHeader decode(byte[] data) {
            return decode(data, false);
        }

        /**
         * Decodes a header, optionally allowing trailing bytes.
         */
        public static RtpHeader decode(byte[] data, boolean allowExcessive) {
            ByteBuffer buf = buffer(data, LENGTH, allowExcessive);
            return new RtpHeader(u8(buf), u8(buf), u16(buf));
        }
    }

    /**
     * Timing request/response packet.
     */
    public record TimingPacket(int proto, int type, int seqno, long padding, long reftimeSec, long reftimeFrac,
            long recvtimeSec, long recvtimeFrac, long sendtimeSec, long sendtimeFrac) {

        /** Encoded size in bytes. */
        public static final int LENGTH = 32;

        /**
         * Encodes this packet to its 32-byte wire format.
         */
        public byte[] encode() {
            ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.BIG_ENDIAN);
            putHeader(buf, proto, type, seqno);
            putU32(buf, padding);
            putU32(buf, reftimeSec);
            putU32(buf, reftimeFrac);
            putU32(buf, recvtimeSec);
            putU32(buf, recvtimeFrac);
            putU32(buf, sendtimeSec);
            putU32(buf, sendtimeFrac);
            return buf.array();
        }

        /**
         * Decodes a packet, requiring an exact-length input.
         */
        public static TimingPacket decode(byte[] data) {
            return decode(data, false);
        }

        /**
         * Decodes a packet, optionally allowing trailing bytes.
         */
        public static TimingPacket decode(byte[] data, boolean allowExcessive) {
            ByteBuffer buf = buffer(data, LENGTH, allowExcessive);
            return new TimingPacket(u8(buf), u8(buf), u16(buf), u32(buf), u32(buf), u32(buf), u32(buf), u32(buf),
                    u32(buf), u32(buf));
        }
    }

    /**
     * Sync packet sent on the control channel.
     */
    public record SyncPacket(int proto, int type, int seqno, long nowWithoutLatency, long lastSyncSec,
            long lastSyncFrac, long now) {

        /** Encoded size in bytes. */
        public static final int LENGTH = 20;

        /**
         * Encodes this packet to its 20-byte wire format.
         */
        public byte[] encode() {
            ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.BIG_ENDIAN);
            putHeader(buf, proto, type, seqno);
            putU32(buf, nowWithoutLatency);
            putU32(buf, lastSyncSec);
            putU32(buf, lastSyncFrac);
            putU32(buf, now);
            return buf.array();
        }

        /**
         * Decodes a packet, requiring an exact-length input.
         */
        public static SyncPacket decode(byte[] data) {
            return decode(data, false);
        }

        /**
         * Decodes a packet, optionally allowing trailing bytes.
         */
        public static SyncPacket decode(byte[] data, boolean allowExcessive) {
            ByteBuffer buf = buffer(data, LENGTH, allowExcessive);
            return new SyncPacket(u8(buf), u8(buf), u16(buf), u32(buf), u32(buf), u32(buf), u32(buf));
        }
    }

    /**
     * Header of an audio packet.
     *
     * <p>
     * NB: the audio payload is not included here and shall be appended manually.
     */
    public record AudioPacketHeader(int proto, int type, int seqno, long timestamp, long ssrc) {

        /** Encoded size in bytes (payload excluded). */
        public static final int LENGTH = 12;

        /**
         * Encodes this header to its 12-byte wire format.
         */
        public byte[] encode() {
            ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.BIG_ENDIAN);
            putHeader(buf, proto, type, seqno);
            putU32(buf, timestamp);
            putU32(buf, ssrc);
            return buf.array();
        }

        /**
         * Decodes a header, requiring an exact-length input.
         */
        public static AudioPacketHeader decode(byte[] data) {
            return decode(data, false);
        }

        /**
         * Decodes a header, optionally allowing trailing bytes (the audio payload).
         */
        public static AudioPacketHeader decode(byte[] data, boolean allowExcessive) {
            ByteBuffer buf = buffer(data, LENGTH, allowExcessive);
            return new AudioPacketHeader(u8(buf), u8(buf), u16(buf), u32(buf), u32(buf));
        }
    }

    /**
     * Retransmit request received on the control channel.
     */
    public record RetransmitRequest(int proto, int type, int seqno, int lostSeqno, int lostPackets) {

        /** Encoded size in bytes. */
        public static final int LENGTH = 8;

        /**
         * Encodes this packet to its 8-byte wire format.
         */
        public byte[] encode() {
            ByteBuffer buf = ByteBuffer.allocate(LENGTH).order(ByteOrder.BIG_ENDIAN);
            putHeader(buf, proto, type, seqno);
            buf.putShort((short) lostSeqno);
            buf.putShort((short) lostPackets);
            return buf.array();
        }

        /**
         * Decodes a packet, requiring an exact-length input.
         */
        public static RetransmitRequest decode(byte[] data) {
            return decode(data, false);
        }

        /**
         * Decodes a packet, optionally allowing trailing bytes.
         */
        public static RetransmitRequest decode(byte[] data, boolean allowExcessive) {
            ByteBuffer buf = buffer(data, LENGTH, allowExcessive);
            return new RetransmitRequest(u8(buf), u8(buf), u16(buf), u16(buf), u16(buf));
        }
    }

    private static void putHeader(ByteBuffer buf, int proto, int type, int seqno) {
        buf.put((byte) proto);
        buf.put((byte) type);
        buf.putShort((short) seqno);
    }

    private static void putU32(ByteBuffer buf, long value) {
        buf.putInt((int) value);
    }

    private static int u8(ByteBuffer buf) {
        return buf.get() & 0xFF;
    }

    private static int u16(ByteBuffer buf) {
        return buf.getShort() & 0xFFFF;
    }

    private static long u32(ByteBuffer buf) {
        return buf.getInt() & 0xFFFFFFFFL;
    }
}
