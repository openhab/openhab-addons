package org.openhab.binding.regoheatpump.internal.protocol;

import java.util.Arrays;

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
