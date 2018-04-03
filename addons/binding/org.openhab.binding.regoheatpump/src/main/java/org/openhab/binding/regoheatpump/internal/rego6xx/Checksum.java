/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

import java.util.Arrays;

/**
 * The {@link Checksum} is responsible for calculating checksum of given data.
 *
 * @author Boris Krivonog - Initial contribution
 */
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
        count += offset;
        for (; offset < count; ++offset) {
            checksum = (byte) (checksum ^ buffer[offset]);
        }

        return checksum;
    }
}
