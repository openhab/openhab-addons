package org.openhab.binding.ivtheatpump.internal.protocol;

import java.util.function.Function;

public class ResponseParser {
    public final static int StandardFormLength = 5;
    public final static int LongFormLength = 42;
    public final static byte ComputerAddress = (byte) 0x01;

    public static short standardForm(byte[] buffer) {
        return parse(buffer, StandardFormLength, b -> ValueConverter.sevenBitFormatToShort(b, 1));
    }

    public static String longForm(byte[] buffer) {
        return parse(buffer, LongFormLength, b -> ValueConverter.stringFromBytes(b, 1));
    }

    private static <T> T parse(byte[] buffer, int responseLength, Function<byte[], T> provider) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        if (buffer.length != responseLength) {
            throw new IllegalStateException("Expected size does not match: " + buffer.length + "!=" + responseLength);
        }

        if (buffer[0] != ComputerAddress) {
            throw new IllegalStateException("Invalid header received " + buffer[0]);
        }

        if (Checksum.calculate(buffer, 1, responseLength - 2) != buffer[responseLength - 1]) {
            throw new IllegalStateException("Invalid crc received.");
        }

        return provider.apply(buffer);
    }
}
