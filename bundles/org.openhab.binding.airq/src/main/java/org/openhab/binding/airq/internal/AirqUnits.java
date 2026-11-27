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
package org.openhab.binding.airq.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The units the air-Q reports its measurements in, and the conversion between volume fractions and mass densities.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
final class AirqUnits {
    static final Unit<?> GRAM_PER_CUBICMETRE = SIUnits.GRAM.divide(SIUnits.CUBIC_METRE);
    static final Unit<?> MILLIGRAM_PER_CUBICMETRE = MetricPrefix.MILLI(SIUnits.GRAM).divide(SIUnits.CUBIC_METRE);
    static final Unit<?> HECTOPASCAL = MetricPrefix.HECTO(SIUnits.PASCAL);
    static final Unit<?> MICROMETRE = MetricPrefix.MICRO(SIUnits.METRE);

    // measurement.md: the unit names used in the "units" setting
    private static final Map<String, Unit<?>> DEVICE_UNITS = Map.ofEntries(Map.entry("degC", SIUnits.CELSIUS),
            Map.entry("degF", ImperialUnits.FAHRENHEIT), Map.entry("K", Units.KELVIN), Map.entry("hPa", HECTOPASCAL),
            Map.entry("kPa", MetricPrefix.KILO(SIUnits.PASCAL)), Map.entry("mbar", Units.MILLIBAR),
            Map.entry("bar", Units.BAR), Map.entry("psi", ImperialUnits.POUND_FORCE_SQUARE_INCH),
            Map.entry("%", Units.PERCENT), Map.entry("ppm", Units.PARTS_PER_MILLION),
            Map.entry("ppb", Units.PARTS_PER_BILLION), Map.entry("g/m^3", GRAM_PER_CUBICMETRE),
            Map.entry("mg/m^3", MILLIGRAM_PER_CUBICMETRE), Map.entry("ug/m^3", Units.MICROGRAM_PER_CUBICMETRE),
            Map.entry("Bq/m^3", Units.BECQUEREL_PER_CUBIC_METRE),
            Map.entry("pCi/L", Units.PICO_CURIE.divide(Units.LITRE)));

    private AirqUnits() {
    }

    static @Nullable Unit<?> deviceUnit(String name) {
        return DEVICE_UNITS.get(name);
    }

    /**
     * Converts a gas concentration between a volume fraction (%, ppm, ppb) and a mass density (g/m³, mg/m³, µg/m³)
     * as described in measurement.md: c[mg/m³] = c[ppm] * M / 22.4 * 273.15 / (273.15 + T) * P / 1013.25
     *
     * @param concentration the concentration to convert
     * @param target the unit to convert to
     * @param molarMass M, the molar mass of the gas in g/mol
     * @param temperature T, the air temperature in °C
     * @param pressure P, the air pressure in hPa
     * @return the converted concentration, or null if it is not a volume fraction or a mass density, cannot be
     *         converted to the target, or the conditions are not physical
     */
    static @Nullable QuantityType<?> convertConcentration(QuantityType<?> concentration, Unit<?> target,
            double molarMass, double temperature, double pressure) {
        // mg/m³ per ppm
        double perPpm = molarMass / 22.4 * 273.15 / (273.15 + temperature) * pressure / 1013.25;
        if (!Double.isFinite(perPpm) || perPpm <= 0) {
            return null;
        }
        BigDecimal factor = BigDecimal.valueOf(perPpm);
        QuantityType<?> fraction = concentration.toUnit(Units.PARTS_PER_MILLION);
        if (fraction != null) {
            return new QuantityType<>(fraction.toBigDecimal().multiply(factor), MILLIGRAM_PER_CUBICMETRE)
                    .toUnit(target);
        }
        QuantityType<?> density = concentration.toUnit(MILLIGRAM_PER_CUBICMETRE);
        if (density != null) {
            return new QuantityType<>(density.toBigDecimal().divide(factor, MathContext.DECIMAL64),
                    Units.PARTS_PER_MILLION).toUnit(target);
        }
        return null;
    }
}
