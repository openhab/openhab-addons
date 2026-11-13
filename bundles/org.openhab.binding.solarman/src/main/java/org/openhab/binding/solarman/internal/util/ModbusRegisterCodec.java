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
package org.openhab.binding.solarman.internal.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Converts big-endian wire words in explicitly ordered low-word-first register lists.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class ModbusRegisterCodec {
    private ModbusRegisterCodec() {
    }

    public static BigInteger decode(List<Integer> addresses, Map<Integer, byte[]> registers, boolean signed) {
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("No registers supplied");
        }
        BigInteger value = BigInteger.ZERO;
        for (int i = addresses.size() - 1; i >= 0; i--) {
            byte[] word = Objects.requireNonNull(registers.get(addresses.get(i)), "Missing register");
            if (word.length != 2) {
                throw new IllegalArgumentException("A register must contain two bytes");
            }
            value = value.shiftLeft(16).or(BigInteger.valueOf(Short.toUnsignedInt(ByteBuffer.wrap(word).getShort())));
        }
        int bits = addresses.size() * 16;
        return signed && value.testBit(bits - 1) ? value.subtract(BigInteger.ONE.shiftLeft(bits)) : value;
    }

    public static byte[] encode(BigInteger value, int count, boolean signed) {
        if (count < 1 || count > 123) {
            throw new IllegalArgumentException("Invalid write register count");
        }
        int bits = count * 16;
        BigInteger limit = BigInteger.ONE.shiftLeft(signed ? bits - 1 : bits);
        BigInteger minimum = signed ? limit.negate() : BigInteger.ZERO;
        if (value.compareTo(minimum) < 0 || value.compareTo(limit) >= 0) {
            throw new IllegalArgumentException("Value is outside the register range");
        }
        ByteBuffer buffer = ByteBuffer.allocate(count * 2);
        for (int i = 0; i < count; i++) {
            buffer.putShort(value.shiftRight(i * 16).shortValue());
        }
        return buffer.array();
    }
}
