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
package org.openhab.binding.bluelink.internal.dto.ca;

import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.util.Arrays;
import java.util.stream.IntStream;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluelink.internal.dto.TemperatureValue;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Air temperature value for the Kia/Genesis Connect and Hyundai Bluelink API
 * in Canada.
 *
 * @author Marcus Better - Initial contribution
 */
public record AirTemperature(@Override String value, @Override int unit, int hvacTempType) implements TemperatureValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirTemperature.class);
    private static final int UNIT_CELSIUS = 0;
    private static final double[] TEMP_RANGE_C_OLD = IntStream.range(32, 64).mapToDouble(x -> (double) x / 2).toArray();
    private static final double[] TEMP_RANGE_C_NEW = IntStream.range(28, 64).mapToDouble(x -> (double) x / 2).toArray();
    private static final int TEMP_MODEL_YEAR_CUTOFF = 2020;

    private static AirTemperature ofCelsius(final @NonNull IVehicle vehicle, final double value) {
        // API only allows half-integer values, so round to nearest one
        final double v = Math.round(value * 2) / 2.0;
        final var tempRange = getTempRange(vehicle);
        int idx = Arrays.binarySearch(tempRange, v);
        if (idx < 0) {
            // exact temperature value was not found, use the nearest one
            idx = Math.min(-idx - 1, tempRange.length - 1);
        }
        final String hex = "%02xH".formatted(idx);
        return new AirTemperature(hex, UNIT_CELSIUS, vehicle.isElectric() ? 1 : 0);
    }

    public static AirTemperature of(final @NonNull IVehicle v, final @NonNull QuantityType<@NonNull Temperature> temp) {
        if (CELSIUS.equals(temp.getUnit())) {
            return ofCelsius(v, temp.doubleValue());
        } else {
            final var tempC = temp.toUnit(CELSIUS);
            if (tempC != null) {
                return ofCelsius(v, tempC.doubleValue());
            } else {
                throw new IllegalArgumentException("cannot convert temperature");
            }
        }
    }

    @Override
    public State getTemperature(final @NonNull IVehicle vehicle) {
        // Fahrenheit not handled yet
        if (unit != UNIT_CELSIUS || value == null || !value.endsWith("H")) {
            return UnDefType.UNDEF;
        }
        final int idx;
        try {
            idx = Integer.parseInt(value.replace("H", ""), 16);
        } catch (final NumberFormatException e) {
            LOGGER.debug("unexpected temperature value {}", value);
            return UnDefType.UNDEF;
        }
        final var tempRange = getTempRange(vehicle);
        if (idx < 0 || idx >= tempRange.length) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<>(tempRange[idx], CELSIUS);
    }

    private static double[] getTempRange(final @NonNull IVehicle vehicle) {
        return vehicle.modelYear() < TEMP_MODEL_YEAR_CUTOFF ? TEMP_RANGE_C_OLD : TEMP_RANGE_C_NEW;
    }
}
