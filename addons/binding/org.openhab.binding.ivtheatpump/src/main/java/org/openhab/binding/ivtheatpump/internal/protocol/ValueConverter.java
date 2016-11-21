package org.openhab.binding.ivtheatpump.internal.protocol;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

public class ValueConverter {
    public static Short ToShort(byte[] buffer) {
        if (buffer == null || buffer.length != 3) {
            return null;
        }

        return (short) (buffer[0] << 14 | buffer[1] << 7 | buffer[2]);
    }

    public static Double ToDouble(byte[] buffer) {
        Short value = ToShort(buffer);
        return value == null ? null : value / 10.0;
    }

    public static State ToDoubleState(byte[] buffer) {
        Double value = ToDouble(buffer);
        return value == null ? UnDefType.UNDEF : new DecimalType(value);
    }
}
