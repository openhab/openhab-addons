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
package org.openhab.binding.lifx.internal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lifx.internal.LifxProduct.TemperatureRange;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;

/**
 * Utility class for sharing message utility methods between objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public final class LifxMessageUtil {

    private static final BigDecimal INCREASE_DECREASE_STEP = new BigDecimal(10);

    private static final BigDecimal ZERO = PercentType.ZERO.toBigDecimal();
    private static final BigDecimal HUNDRED = PercentType.HUNDRED.toBigDecimal();

    private LifxMessageUtil() {
        // hidden utility class constructor
    }

    public static PercentType increaseDecreasePercentType(IncreaseDecreaseType increaseDecreaseType, PercentType old) {
        BigDecimal delta = ZERO;
        if (increaseDecreaseType == IncreaseDecreaseType.INCREASE) {
            delta = INCREASE_DECREASE_STEP;
        } else if (increaseDecreaseType == IncreaseDecreaseType.DECREASE) {
            delta = INCREASE_DECREASE_STEP.negate();
        }

        if (!ZERO.equals(delta)) {
            BigDecimal newValue = old.toBigDecimal().add(delta);
            newValue = newValue.setScale(0, RoundingMode.HALF_UP);
            newValue = newValue.min(HUNDRED);
            newValue = newValue.max(ZERO);
            return new PercentType(newValue);
        } else {
            return old;
        }
    }

    private static PercentType intToPercentType(int i) {
        return new PercentType(Math.round((i / 65535.0f) * 100));
    }

    private static int percentTypeToInt(PercentType percentType) {
        return (int) (percentType.floatValue() / 100 * 65535.0f);
    }

    public static DecimalType hueToDecimalType(int hue) {
        return new DecimalType(hue * 360 / 65535.0f);
    }

    public static int decimalTypeToHue(DecimalType hue) {
        return (int) (hue.floatValue() / 360 * 65535.0f);
    }

    public static PercentType saturationToPercentType(int saturation) {
        return intToPercentType(saturation);
    }

    public static int percentTypeToSaturation(PercentType saturation) {
        return percentTypeToInt(saturation);
    }

    public static PercentType brightnessToPercentType(int brightness) {
        return intToPercentType(brightness);
    }

    public static int percentTypeToBrightness(PercentType brightness) {
        return percentTypeToInt(brightness);
    }

    public static PercentType kelvinToPercentType(int kelvin, TemperatureRange temperatureRange) {
        if (temperatureRange.getRange() == 0) {
            return PercentType.HUNDRED;
        }
        BigDecimal value = BigDecimal
                .valueOf((kelvin - temperatureRange.getMaximum()) / (temperatureRange.getRange() / -100));
        value = value.min(HUNDRED);
        value = value.max(ZERO);
        return new PercentType(value);
    }

    public static int commandToKelvin(Command temperature, TemperatureRange temperatureRange) {
        if (temperature instanceof PercentType percentValue) {
            return percentTypeToKelvin(percentValue, temperatureRange);
        } else if (temperature instanceof QuantityType quantityValue) {
            return quantityTypeToKelvin(quantityValue, temperatureRange);
        } else if (temperature instanceof DecimalType decimalValue) {
            return decimalTypeToKelvin(decimalValue, temperatureRange);
        } else {
            throw new IllegalStateException(
                    "Unexpected command type " + temperature.getClass().getName() + " for color temperature command.");
        }
    }

    public static int decimalTypeToKelvin(DecimalType temperature, TemperatureRange temperatureRange) {
        return Math.round(Math.min(Math.max(temperature.intValue(), temperatureRange.getMinimum()),
                temperatureRange.getMaximum()));
    }

    public static int percentTypeToKelvin(PercentType temperature, TemperatureRange temperatureRange) {
        return Math.round(
                temperatureRange.getMaximum() - (temperature.floatValue() * (temperatureRange.getRange() / 100)));
    }

    public static int quantityTypeToKelvin(QuantityType temperature, TemperatureRange temperatureRange) {
        QuantityType<?> asKelvin = temperature.toInvertibleUnit(Units.KELVIN);
        if (asKelvin == null) {
            throw new IllegalStateException(
                    "Cannot convert color temperature " + temperature.toString() + " to Kelvin");
        }

        return asKelvin.intValue();
    }

    public static PercentType infraredToPercentType(int infrared) {
        return intToPercentType(infrared);
    }

    public static int percentTypeToInfrared(PercentType infrared) {
        return percentTypeToInt(infrared);
    }

    public static boolean sameColors(HSBK... colors) {
        if (colors.length <= 1) {
            return true;
        }

        for (int i = 1; i < colors.length; i++) {
            if (!colors[0].equals(colors[i])) {
                return false;
            }
        }
        return true;
    }

    public static long randomSourceId() {
        return UUID.randomUUID().getLeastSignificantBits() & (-1L >>> 32);
    }
}
