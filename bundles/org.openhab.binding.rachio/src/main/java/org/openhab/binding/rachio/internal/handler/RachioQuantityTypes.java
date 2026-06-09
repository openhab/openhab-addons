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
package org.openhab.binding.rachio.internal.handler;

import java.math.BigDecimal;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Converts Rachio raw API values and channel commands to typed openHAB quantities.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
final class RachioQuantityTypes {
    private static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(SIUnits.METRE);

    private RachioQuantityTypes() {
    }

    static OptionalInt durationSeconds(Command command) {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> seconds = quantityCommand.toUnit(Units.SECOND);
            return seconds != null ? OptionalInt.of(seconds.intValue()) : OptionalInt.empty();
        }
        if (command instanceof DecimalType decimalCommand) {
            return OptionalInt.of(decimalCommand.intValue());
        }
        return OptionalInt.empty();
    }

    static OptionalDouble lengthMillimeters(Command command) {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> millimeters = quantityCommand.toUnit(MILLIMETRE);
            return millimeters != null ? OptionalDouble.of(millimeters.doubleValue()) : OptionalDouble.empty();
        }
        if (command instanceof DecimalType decimalCommand) {
            return OptionalDouble.of(decimalCommand.doubleValue());
        }
        return OptionalDouble.empty();
    }

    static OptionalDouble dimensionless(Command command) {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> unitless = quantityCommand.toUnit(Units.ONE);
            return unitless != null ? OptionalDouble.of(unitless.doubleValue()) : OptionalDouble.empty();
        }
        if (command instanceof DecimalType decimalCommand) {
            return OptionalDouble.of(decimalCommand.doubleValue());
        }
        return OptionalDouble.empty();
    }

    static State seconds(int seconds) {
        return new QuantityType<>(seconds, Units.SECOND);
    }

    static State days(int days) {
        return new QuantityType<Time>(days, Units.DAY);
    }

    static State inchesOrNull(double value) {
        return quantityOr(value, ImperialUnits.INCH, UnDefType.NULL);
    }

    static State millimetersOrUndef(double value) {
        return quantityOr(value, MILLIMETRE, UnDefType.UNDEF);
    }

    static State squareFeet(int value) {
        return new QuantityType<Area>(value, ImperialUnits.SQUARE_FOOT);
    }

    static State fractionOrNull(double value) {
        return quantityOr(value, Units.ONE, UnDefType.NULL);
    }

    static State fractionOrUndef(double value) {
        return quantityOr(value, Units.ONE, UnDefType.UNDEF);
    }

    static State percentOrUndef(double value) {
        return quantityOr(value, Units.PERCENT, UnDefType.UNDEF);
    }

    static State temperatureOrUndef(double value, String forecastUnits) {
        Unit<Temperature> unit = isUsForecast(forecastUnits) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
        return quantityOr(value, unit, UnDefType.UNDEF);
    }

    static State precipitationOrUndef(double value, String forecastUnits) {
        Unit<Length> unit = isUsForecast(forecastUnits) ? ImperialUnits.INCH : MILLIMETRE;
        return quantityOr(value, unit, UnDefType.UNDEF);
    }

    static State windSpeedOrUndef(double value, String forecastUnits) {
        Unit<Speed> unit = isUsForecast(forecastUnits) ? ImperialUnits.MILES_PER_HOUR : Units.METRE_PER_SECOND;
        return quantityOr(value, unit, UnDefType.UNDEF);
    }

    private static boolean isUsForecast(String forecastUnits) {
        return "US".equalsIgnoreCase(forecastUnits);
    }

    private static <Q extends javax.measure.Quantity<Q>> State quantityOr(double value, Unit<Q> unit,
            State undefinedState) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return undefinedState;
        }
        return new QuantityType<>(BigDecimal.valueOf(value), unit);
    }
}
