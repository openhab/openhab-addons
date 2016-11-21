package org.openhab.binding.ivtheatpump.internal.protocol;

public class CommandFactory {
    private final static byte DeviceAddress = (byte) 0x81;

    enum Source {
        FrontPanel((byte) 0),
        SystemRegister((byte) 2),
        RegoVersion((byte) 0x7f);

        private final byte command;

        Source(byte command) {
            this.command = command;
        }
    }

    static byte[] createReadCommand(Source source, short address, short data) {
        final byte[] addressBytes = shortToSevenBitFormat(address);
        final byte[] dataBytes = shortToSevenBitFormat(data);
        final byte[] commandBytes = new byte[] { DeviceAddress, source.command, addressBytes[0], addressBytes[1],
                addressBytes[2], dataBytes[0], dataBytes[1], dataBytes[2],
                Checksum.calculate(addressBytes, dataBytes) };
        return commandBytes;
    }

    public static byte[] createReadRegoVersionCommand() {
        return createReadCommand(Source.RegoVersion, (short) 0, (short) 0);
    }

    public static byte[] createReadFromSystemRegisterCmd(short address) {
        return createReadCommand(Source.SystemRegister, address, (short) 0);
    }

    private static byte[] shortToSevenBitFormat(short value) {
        final byte b1 = (byte) ((value & 0xC000) >> 14);
        final byte b2 = (byte) ((value & 0x3F80) >> 7);
        final byte b3 = (byte) (value & 0x007F);

        return new byte[] { b1, b2, b3 };
    }
}
