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
package org.openhab.binding.homekit.internal.camera.tlv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Little-endian value helpers for HomeKit camera TLV8 payloads.
 * Numeric TLV values are little-endian per the HAP specification.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class TlvHelpers {

    private TlvHelpers() {
    }

    public static byte[] u8(int value) {
        return new byte[] { (byte) value };
    }

    public static byte[] u16Le(int value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) value).array();
    }

    public static byte[] u32Le(long value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) value).array();
    }

    public static byte[] f32Le(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    public static int readU8(byte[] value) {
        requireLength(value, 1);
        return value[0] & 0xFF;
    }

    public static long readU32Le(byte[] value) {
        requireLength(value, 4);
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    public static byte[] required(Map<Integer, byte[]> tlv, int type) {
        byte[] value = tlv.get(type);
        if (value == null) {
            throw new IllegalArgumentException("Missing TLV type 0x" + Integer.toHexString(type));
        }
        return value;
    }

    public static void requireLength(byte[] value, int length) {
        if (value.length != length) {
            throw new IllegalArgumentException("Expected " + length + " bytes but got " + value.length);
        }
    }
}
