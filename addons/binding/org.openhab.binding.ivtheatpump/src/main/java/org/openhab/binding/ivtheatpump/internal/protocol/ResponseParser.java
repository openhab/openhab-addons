package org.openhab.binding.ivtheatpump.internal.protocol;

import java.util.function.Function;

public class ResponseParser {
    public final static int StandardFormLength = 5;
    public final static int LongFormLength = 42;
    public final static byte ComputerAddress = (byte) 0x01;

    public static Short standardForm(byte[] buffer) {
        return parse(buffer, StandardFormLength, b -> ValueConverter.sevenBitFormatToShort(b, 1));
    }

    public static String longForm(byte[] buffer) {
        return parse(buffer, LongFormLength, b -> ValueConverter.stringFromBytes(b, 1));
    }

    private static <T> T parse(byte[] buffer, int responseLength, Function<byte[], T> provider) {
        if (buffer == null || buffer.length != responseLength) {
            return null;
        }

        if (buffer[0] != ComputerAddress) {
            return null;
        }

        if (Checksum.calculate(buffer, 1, responseLength - 2) != buffer[responseLength - 1]) {
            return null;
        }

        return provider.apply(buffer);
    }
}
