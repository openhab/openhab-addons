/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Type;

/**
 * Utility class for converting values to and from Matter types.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ValueUtils {
    private static final BigDecimal TEMPERATURE_MULTIPLIER = new BigDecimal(100);

    /**
     * Converts a ZigBee 8 bit level as used in Level Control cluster and others to a percentage
     *
     * @param level an integer between 0 and 254
     * @return the scaled {@link PercentType}
     */
    public static PercentType levelToPercent(int level) {
        int result = (int) Math.round(level * 100.0 / 254.0);
        return level == 0 ? PercentType.ZERO : new PercentType(Math.max(result, 1));
    }

    /**
     * Converts a {@link PercentType} to an 8 bit level scaled between 0 and 254
     *
     * @param percent the {@link PercentType} to convert
     * @return a scaled value between 0 and 254
     */

    public static int percentToLevel(PercentType percent) {
        return (int) (percent.floatValue() * 254.0f / 100.0f + 0.5f);
    }

    /**
     * Converts a {@link Command} to a ZigBee / Matter temperature integer
     *
     * @param type the {@link Type} to convert
     * @return the {@link Type} or null if the conversion was not possible
     */
    public static @Nullable Integer temperatureToValue(Type type) {
        BigDecimal value = null;
        if (type instanceof QuantityType<?> quantity) {
            if (quantity.getUnit() == SIUnits.CELSIUS) {
                value = quantity.toBigDecimal();
            } else if (quantity.getUnit() == ImperialUnits.FAHRENHEIT) {
                QuantityType<?> celsius = quantity.toUnit(SIUnits.CELSIUS);
                if (celsius != null) {
                    value = celsius.toBigDecimal();
                }
            }
        } else if (type instanceof Number number) {
            // No scale, so assumed to be Celsius
            value = BigDecimal.valueOf(number.doubleValue());
        }
        if (value == null) {
            return null;
        }
        // originally this used RoundingMode.CEILING, if there are accuracy problems, we may want to revisit that
        return value.setScale(2, RoundingMode.HALF_UP).multiply(TEMPERATURE_MULTIPLIER).intValue();
    }

    /**
     * Converts an integer value into a {@link QuantityType}. The temperature as an integer is assumed to be multiplied
     * by 100 as per the ZigBee / Matter standard format.
     *
     * @param value the integer value to convert
     * @return the {@link QuantityType}
     */
    public static QuantityType<Temperature> valueToTemperature(int value) {
        return new QuantityType<>(BigDecimal.valueOf(value, 2), SIUnits.CELSIUS);
    }
}
