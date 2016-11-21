package org.openhab.binding.ivtheatpump.internal.protocol;

public class ResponseParser {
    public final static int StandardFormLength = 5;
    public final static byte ComputerAddress = (byte) 0x01;

    public static byte[] standardForm(byte[] buffer) {
        if (buffer == null || buffer.length != 5) {
            return null;
        }

        if (buffer[0] != ComputerAddress) {
            return null;
        }

        byte[] dataBytes = new byte[] { buffer[1], buffer[2], buffer[3] };
        if (Checksum.calculate(dataBytes) != buffer[4]) {
            return null;
        }

        return dataBytes;
    }
}
