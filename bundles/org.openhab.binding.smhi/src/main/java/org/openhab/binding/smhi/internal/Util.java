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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * A class with static utility methods to get correct data depending on the parameter
 *
 * @author Anders Alfredsson - Initial contribution
 */

@NonNullByDefault
public class Util {

    public static BigDecimal getMissingValue(String parameter) {
        return switch (parameter) {
            case TEMPERATURE, TEMPERATURE_MIN, TEMPERATURE_MAX -> DEFAULT_MISSING_VALUE;
            default -> BigDecimal.valueOf(-1);
        };
    }

    public static State getParameterAsState(String parameter, BigDecimal value) {
        // TODO: Remove for 6.0 release
        if (parameter.equals(PMP3G_PRECIPITATION_CATEGORY)) {
            return new DecimalType(PMP3G_PCAT_BACKWARD_COMP.getOrDefault(value.intValue(), value.intValue()));
        }
        parameter = PMP3G_BACKWARD_COMP.getOrDefault(parameter, parameter);
        // TODO: end

        if (DEFAULT_MISSING_VALUE.equals(value)) {
            value = getMissingValue(parameter);
        }

        return switch (parameter) {
            case PRESSURE -> new QuantityType<>(value, MetricPrefix.HECTO(SIUnits.PASCAL));
            case TEMPERATURE -> new QuantityType<>(value, SIUnits.CELSIUS);
            case VISIBILITY -> new QuantityType<>(value, MetricPrefix.KILO(SIUnits.METRE));
            case WIND_DIRECTION -> new QuantityType<>(value, Units.DEGREE_ANGLE);
            case WIND_SPEED, GUST -> new QuantityType<>(value, Units.METRE_PER_SECOND);
            case RELATIVE_HUMIDITY, PRECIPITATION_PROBABILITY -> new QuantityType<>(value, Units.PERCENT);
            case FROZEN_PROBABILITY, THUNDER_PROBABILITY ->
                new QuantityType<>(value.multiply(FRACTION_TO_PERCENT), Units.PERCENT);
            // Smhi returns -9 for precipitation_frozen_part if there's no precipitation, replace with -1
            case PERCENT_FROZEN -> value.intValue() == -9 ? new QuantityType<>(-1, Units.PERCENT)
                    : new QuantityType<>(value.multiply(FRACTION_TO_PERCENT), Units.PERCENT);
            case HIGH_CLOUD_COVER, MEDIUM_CLOUD_COVER, LOW_CLOUD_COVER, TOTAL_CLOUD_COVER ->
                new QuantityType<>(value.multiply(OCTAS_TO_PERCENT), Units.PERCENT);
            case CLOUD_BASE_ALTITUDE, CLOUD_TOP_ALTITUDE -> new QuantityType<>(value, SIUnits.METRE);
            case PRECIPITATION_MAX, PRECIPITATION_MEAN, PRECIPITATION_MEDIAN, PRECIPITATION_MIN ->
                new QuantityType<>(value, Units.MILLIMETRE_PER_HOUR);
            case PRECIPITATION_TOTAL -> new QuantityType<>(value, MetricPrefix.MILLI(SIUnits.METRE));
            default -> new DecimalType(value.stripTrailingZeros());
        };
    }
}
