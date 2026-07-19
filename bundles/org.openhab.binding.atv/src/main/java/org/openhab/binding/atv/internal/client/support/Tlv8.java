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
package org.openhab.binding.atv.internal.client.support;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of TLV8 used by the HomeKit pairing process.
 *
 * <p>
 * Only one level of value is supported, i.e. no dicts in dicts. Values larger than 255
 * bytes are split into multiple consecutive entries with the same tag on write and merged
 * back together on read.
 *
 * <p>
 * Enum constant names deliberately match HAP naming (PascalCase).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Tlv8 {

    private Tlv8() {
    }

    /** An enum constant backed by an integer value. */
    private interface Valued {
        /**
         * Integer value of this constant.
         *
         * @return the value
         */
        int value();
    }

    /** Correspond to TLV values in HAP specification. */
    public enum TlvValue implements Valued {
        // Standardized keys
        Method(0x00),
        Identifier(0x01),
        Salt(0x02),
        PublicKey(0x03),
        Proof(0x04),
        EncryptedData(0x05),
        SeqNo(0x06),
        Error(0x07),
        BackOff(0x08),
        Certificate(0x09),
        Signature(0x0A),
        Permissions(0x0B),
        FragmentData(0x0C),
        FragmentLast(0x0D),

        // Apple internal(?)
        Name(0x11),
        Flags(0x13);

        private final int value;

        TlvValue(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }

        /**
         * Look up a tag constant by its integer value.
         *
         * @param value tag value
         * @return matching constant, or empty if unknown
         */
        public static Optional<TlvValue> fromValue(int value) {
            for (TlvValue tlvValue : values()) {
                if (tlvValue.value == value) {
                    return Optional.of(tlvValue);
                }
            }
            return Optional.empty();
        }
    }

    /** Flags used with {@link TlvValue#Flags}. */
    public enum Flags implements Valued {
        TransientPairing(0x10);

        private final int value;

        Flags(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    /** Correspond to error codes in HAP specification. */
    public enum ErrorCode implements Valued {
        Unknown(0x01),
        Authentication(0x02),
        BackOff(0x03),
        MaxPeers(0x04),
        MaxTries(0x05),
        Unavailable(0x06),
        Busy(0x07);

        private final int value;

        ErrorCode(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    /** Correspond to methods in HAP specification. */
    public enum Method implements Valued {
        PairSetup(0x00),
        PairSetupWithAuth(0x01),
        PairVerify(0x02),
        AddPairing(0x03),
        RemovePairing(0x04),
        ListPairing(0x05);

        private final int value;

        Method(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    /** Correspond to states in HAP specification. */
    public enum State implements Valued {
        M1(0x01),
        M2(0x02),
        M3(0x03),
        M4(0x04),
        M5(0x05),
        M6(0x06);

        private final int value;

        State(int value) {
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    /**
     * Parse TLV8 bytes into a map.
     *
     * <p>
     * If a value is larger than 255 bytes, it is split up in multiple chunks, so the
     * same tag might occur several times; such chunks are concatenated back into one
     * value. The returned map preserves encounter order.
     *
     * @param data TLV8 encoded bytes
     * @return map from tag to (merged) value
     */
    public static Map<Integer, byte[]> read(byte[] data) {
        Map<Integer, byte[]> result = new LinkedHashMap<>();
        int pos = 0;
        while (pos < data.length) {
            int tag = data[pos] & 0xFF;
            int length = data[pos + 1] & 0xFF;
            byte[] value = Arrays.copyOfRange(data, pos + 2, Math.min(pos + 2 + length, data.length));
            result.merge(tag, value, Tlv8::concat);
            pos += 2 + length;
        }
        return result;
    }

    /**
     * Convert a map to TLV8 bytes.
     *
     * <p>
     * Entries are serialized in the map's iteration order, so pass an ordered map (e.g.
     * {@link LinkedHashMap}) when the output byte order matters. A tag with a value
     * longer than 255 bytes is added multiple times in chunks of at most 255 bytes and
     * concatenated into one buffer when reading the TLV again.
     *
     * @param data map from tag to value
     * @return TLV8 encoded bytes
     */
    public static byte[] write(Map<Integer, byte[]> data) {
        ByteArrayOutputStream tlv = new ByteArrayOutputStream();
        for (Map.Entry<Integer, byte[]> entry : data.entrySet()) {
            int tag = entry.getKey();
            byte[] value = entry.getValue();
            int pos = 0;
            while (pos < value.length) {
                int size = Math.min(value.length - pos, 255);
                tlv.write(tag);
                tlv.write(size);
                tlv.write(value, pos, size);
                pos += size;
            }
        }
        return tlv.toByteArray();
    }

    /**
     * Create simplified string of TLV8 data.
     *
     * <p>
     * Method, sequence number, error and backoff time are parsed while the rest are
     * just summarized with value byte length.
     *
     * @param data map from tag to value
     * @return human readable summary
     */
    public static String stringify(Map<Integer, byte[]> data) {
        List<String> output = new ArrayList<>();
        for (Map.Entry<Integer, byte[]> entry : data.entrySet()) {
            int key = entry.getKey();
            byte[] value = entry.getValue();
            TlvValue keyType = TlvValue.fromValue(key).orElse(null);
            if (keyType == null) {
                output.add(hex(key) + "=" + value.length + "bytes");
            } else if (keyType == TlvValue.Method) {
                output.add(keyType.name() + "=" + enumValueName(intFromBytesLittleEndian(value), Method.class));
            } else if (keyType == TlvValue.SeqNo) {
                output.add(keyType.name() + "=" + enumValueName(intFromBytesLittleEndian(value), State.class));
            } else if (keyType == TlvValue.Error) {
                output.add(keyType.name() + "=" + enumValueName(intFromBytesLittleEndian(value), ErrorCode.class));
            } else if (keyType == TlvValue.BackOff) {
                output.add(keyType.name() + "=" + intFromBytesLittleEndian(value) + "s");
            } else {
                output.add(keyType.name() + "=" + value.length + "bytes");
            }
        }
        return String.join(", ", output);
    }

    private static <E extends Enum<E> & Valued> String enumValueName(long value, Class<E> enumType) {
        for (E constant : enumType.getEnumConstants()) {
            if (constant.value() == value) {
                return constant.name();
            }
        }
        return hex(value);
    }

    private static String hex(long value) {
        return "0x" + Long.toHexString(value);
    }

    private static long intFromBytesLittleEndian(byte[] value) {
        long result = 0;
        for (int i = value.length - 1; i >= 0; i--) {
            result = (result << 8) | (value[i] & 0xFF);
        }
        return result;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] merged = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
