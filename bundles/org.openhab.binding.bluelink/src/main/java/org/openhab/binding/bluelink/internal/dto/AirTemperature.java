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
package org.openhab.binding.bluelink.internal.dto;

import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Air temperature value for the Bluelink API.
 * Unit values: 0 = Celsius, 1 = Fahrenheit.
 *
 * @author Marcus Better - Initial contribution
 */
public record AirTemperature(String value, int unit) {

    private static final int UNIT_CELSIUS = 0;
    private static final int UNIT_FAHRENHEIT = 1;

    private static final QuantityType<@NonNull Temperature> LO_TEMP = new QuantityType<>(62, FAHRENHEIT);
    private static final QuantityType<@NonNull Temperature> HI_TEMP = new QuantityType<>(82, FAHRENHEIT);

    private static AirTemperature ofCelsius(final int value) {
        return new AirTemperature(String.valueOf(value), UNIT_CELSIUS);
    }

    private static AirTemperature ofFahrenheit(final int value) {
        return new AirTemperature(String.valueOf(value), UNIT_FAHRENHEIT);
    }

    public static AirTemperature of(final @NonNull QuantityType<@NonNull Temperature> temp) {
        if (CELSIUS.equals(temp.getUnit())) {
            return ofCelsius(temp.intValue());
        } else {
            final var tempF = temp.toUnit(FAHRENHEIT);
            if (tempF != null) {
                return ofFahrenheit(tempF.intValue());
            } else {
                throw new IllegalArgumentException("cannot convert temperature");
            }
        }
    }

    public State getTemperature() {
        final String value = this.value;
        try {
            return switch (value) {
                case "LO" -> LO_TEMP;
                case "HI" -> HI_TEMP;
                case null -> UnDefType.NULL;
                default -> switch (unit) {
                    case UNIT_CELSIUS -> new QuantityType<>(Double.parseDouble(value), CELSIUS);
                    case UNIT_FAHRENHEIT -> new QuantityType<>(Double.parseDouble(value), FAHRENHEIT);
                    default -> UnDefType.UNDEF;
                };
            };
        } catch (final NumberFormatException e) {
            return UnDefType.UNDEF;
        }
    }
}
