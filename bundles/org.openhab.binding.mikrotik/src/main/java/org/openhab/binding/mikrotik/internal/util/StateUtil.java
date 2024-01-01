/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mikrotik.internal.util;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link StateUtil} class holds static methods to cast Java native/class types to OpenHAB values
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class StateUtil {

    public static State stringOrNull(@Nullable String value) {
        return value == null ? UnDefType.NULL : new StringType(value);
    }

    public static State qtyMegabitPerSecOrNull(@Nullable Float value) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, Units.MEGABIT_PER_SECOND);
    }

    public static State qtyPercentOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, Units.PERCENT);
    }

    public static State qtyBytesOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, Units.BYTE);
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

    public static State boolSwitchOrNull(@Nullable Boolean value) {
        if (value == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(value);
    }

    public static State boolContactOrNull(@Nullable Boolean value) {
        if (value == null) {
            return UnDefType.NULL;
        }
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public static State timeOrNull(@Nullable LocalDateTime value) {
        return value == null ? UnDefType.NULL : new DateTimeType(value.atZone(ZoneId.systemDefault()));
    }
}
