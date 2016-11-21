package org.openhab.binding.ivtheatpump.internal.protocol;

public class CommandFactory {
    public static byte[] createReadFromSystemRegisterCmd(short registerAddress) {
        byte[] address = shortToSevenBit(registerAddress);
        return new byte[] { (byte) 0x81, // Device address
                0x02, // Command (read)
                address[0], address[1], address[2], // Register address
                0x00, 0x00, 0x00, // Data (0 for read)
                checksum(address) };
    }

    private static byte[] shortToSevenBit(short value) {
        byte b1 = (byte) ((value & 0xC000) >> 14);
        byte b2 = (byte) ((value & 0x3F80) >> 7);
        byte b3 = (byte) (value & 0x007F);

        return new byte[] { b1, b2, b3 };
    }

    private static byte checksum(byte[] data) {
        byte checksum = 0;

        for (byte val : data) {
            checksum = (byte) (checksum ^ val);
        }

        return checksum;
    }
}
