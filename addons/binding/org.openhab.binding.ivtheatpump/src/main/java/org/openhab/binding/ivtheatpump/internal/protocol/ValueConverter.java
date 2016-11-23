package org.openhab.binding.ivtheatpump.internal.protocol;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

public class ValueConverter {
    public static Double ToDouble(Short value) {
        return value == null ? null : value / 10.0;
    }

    public static State ToDoubleState(Short value) {
        Double doubleValue = ToDouble(value);
        return doubleValue == null ? UnDefType.UNDEF : new DecimalType(doubleValue);
    }

    public static byte[] shortToSevenBitFormat(short value) {
        final byte b1 = (byte) ((value & 0xC000) >> 14);
        final byte b2 = (byte) ((value & 0x3F80) >> 7);
        final byte b3 = (byte) (value & 0x007F);

        return new byte[] { b1, b2, b3 };
    }

    public static short sevenBitFormatToShort(byte[] buffer, int offset) {
        return (short) (buffer[offset] << 14 | buffer[offset + 1] << 7 | buffer[offset + 2]);
    }

    public static String stringFromBytes(byte[] buffer, int offset) {
        StringBuilder builder = new StringBuilder();

        int length = offset + 40;
        for (int i = offset; i < length; i += 2) {
            builder.append((char) ((buffer[i] << 4) + buffer[i + 1]));
        }

        return builder.toString();
    }

}
