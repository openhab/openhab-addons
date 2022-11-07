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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Checksum} is responsible for calculating checksum of given data.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
class Checksum {
    static byte calculate(byte[]... lists) {
        return Arrays.stream(lists).reduce((byte) 0, Checksum::calculate, (a, b) -> b);
    }

    static byte calculate(byte[] buffer, int offset, int count) {
        return calculate((byte) 0, buffer, offset, count);
    }

    private static byte calculate(byte checksum, byte[] buffer) {
        return calculate(checksum, buffer, 0, buffer.length);
    }

    private static byte calculate(byte checksum, byte[] buffer, int offset, int count) {
        byte result = checksum;
        int end = count + offset;
        for (int index = offset; index < end; ++index) {
            result = (byte) (result ^ buffer[index]);
        }
        return result;
    }
}
