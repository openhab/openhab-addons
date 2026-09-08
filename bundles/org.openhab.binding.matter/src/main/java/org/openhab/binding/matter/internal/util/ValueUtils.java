/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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
     * A Level Control cluster with the Lighting feature does not permit level 0, so its usable range starts at 1 and
     * percentages are scaled across that range. This matches how other Matter controllers map their sliders, so a
     * level set by one controller reads back as the same percentage everywhere.
     */
    private static final int LIGHTING_MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 254;
    /** Decimal places kept on a saturation percentage, enough that every saturation converts back to itself. */
    private static final int SATURATION_SCALE = 4;

    /**
     * Converts a Level Control level to a percentage. Only the minimum level is off, so the levels just above it are
     * reported as one percent rather than rounding down to zero, which a Dimmer or Color item would read as off.
     *
     * @param level an integer between minLevel and 254
     * @param minLevel the lowest level the device accepts, 1 with the Lighting feature and otherwise 0
     * @return the scaled {@link PercentType}, zero only for the minimum level
     */
    public static PercentType levelToPercent(int level, int minLevel) {
        if (level <= minLevel) {
            return PercentType.ZERO;
        }
        PercentType percent = new PercentType((int) Math.round((level - minLevel) * 100.0 / (MAX_LEVEL - minLevel)));
        return percent.intValue() == 0 ? new PercentType(1) : percent;
    }

    /**
     * Converts a Level Control level of a light with the Lighting feature to a percentage. Such a light is always on,
     * since the feature does not permit level 0, so the minimum level is reported as one percent rather than as off.
     *
     * @param level an integer between 1 and 254
     * @return the scaled {@link PercentType}, never zero
     */
    public static PercentType levelToPercentWhenOn(int level) {
        return level <= LIGHTING_MIN_LEVEL ? new PercentType(1) : levelToPercent(level, LIGHTING_MIN_LEVEL);
    }

    /**
     * Converts a {@link PercentType} to a Level Control level of a device with the Lighting feature.
     *
     * @param percent the {@link PercentType} to convert
     * @return a scaled value between 1 and 254
     */
    public static int percentToLevel(PercentType percent) {
        return percentToLevel(percent, LIGHTING_MIN_LEVEL);
    }

    /**
     * Converts a {@link PercentType} to a Level Control level.
     *
     * @param percent the {@link PercentType} to convert
     * @param minLevel the lowest level the device accepts, 1 with the Lighting feature and otherwise 0
     * @return a scaled value between minLevel and 254
     */
    public static int percentToLevel(PercentType percent, int minLevel) {
        return minLevel + (int) Math.round(percent.doubleValue() * (MAX_LEVEL - minLevel) / 100.0);
    }

    /**
     * Converts a Color Control saturation to a percentage. Unlike a level, zero saturation is valid and means fully
     * desaturated, so the range is 0 to 254. The result keeps enough decimal places that every saturation converts
     * back to itself.
     *
     * @param saturation an integer between 0 and 254
     * @return the scaled {@link PercentType}
     */
    public static PercentType saturationToPercent(int saturation) {
        return new PercentType(
                BigDecimal.valueOf(saturation * 100.0 / MAX_LEVEL).setScale(SATURATION_SCALE, RoundingMode.HALF_UP));
    }

    /**
     * Converts a {@link PercentType} to a Color Control saturation.
     *
     * @param percent the {@link PercentType} to convert
     * @return a scaled value between 0 and 254
     */
    public static int percentToSaturation(PercentType percent) {
        return (int) Math.round(percent.doubleValue() * MAX_LEVEL / 100.0);
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

    /**
     * Converts a Matter illuminance measurement value to a {@link QuantityType} in lux.
     * The Matter spec encodes illuminance as: MeasuredValue = 10,000 x log10(illuminance) + 1
     * A value of 0 indicates illuminance too low to be measured.
     *
     * @param value the Matter measured value (0 = too low, 1-0xFFFE = encoded illuminance)
     * @return the {@link QuantityType} in lux
     */
    public static @Nullable QuantityType<Illuminance> valueToIlluminance(int value) {
        if (value <= 0) {
            return new QuantityType<>(0, Units.LUX);
        }
        if (value > 0xFFFE) {
            return null;
        }
        double illuminance = Math.pow(10, (value - 1) / 10000.0);
        return new QuantityType<>(illuminance, Units.LUX);
    }
}
