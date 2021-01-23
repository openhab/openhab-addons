package org.openhab.binding.mikrotik.internal.util;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.joda.time.DateTime;

@NonNullByDefault
public class StateUtil {

    public static State stringOrNull(@Nullable String value) {
        return value == null ? UnDefType.NULL : new StringType(value);
    }

    public static State intOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new DecimalType(value.floatValue());
    }

    public static State bigIntOrNull(@Nullable BigInteger value) {
        return value == null ? UnDefType.NULL : DecimalType.valueOf(value.toString());
    }

    public static State floatOrNull(@Nullable Float value) {
        return value == null ? UnDefType.NULL : new DecimalType(value);
    }

    public static State boolOrNull(@Nullable Boolean value) {
        if (value == null)
            return UnDefType.NULL;
        return value ? OnOffType.ON : OnOffType.OFF;
    }

    public static State timeOrNull(@Nullable DateTime value) {
        return value == null ? UnDefType.NULL : new DateTimeType(value.toGregorianCalendar().toZonedDateTime());
    }
}
