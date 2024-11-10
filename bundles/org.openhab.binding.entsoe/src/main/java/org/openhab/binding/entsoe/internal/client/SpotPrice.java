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
package org.openhab.binding.entsoe.internal.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.format.DateTimeParseException;

import javax.measure.Unit;
import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * @author Jørgen Melhus - Initial contribution
 */
@NonNullByDefault
public class SpotPrice {
    private String currency;
    private Unit<Energy> unit;
    private Double price;
    private Instant time;

    public SpotPrice(String currency, String unit, Double price, Instant start, int iteration, String resolution)
            throws EntsoeResponseException {
        this.currency = currency;
        this.unit = convertEntsoeUnit(unit);
        this.price = price;
        this.time = calculateDateTime(start, iteration, resolution);
    }

    private Instant calculateDateTime(Instant start, int iteration, String resolution) throws EntsoeResponseException {
        try {
            if (resolution.toUpperCase().startsWith("PT")) {
                Duration d = Duration.parse(resolution).multipliedBy(iteration);
                return start.plus(d);
            } else if (resolution.toUpperCase().startsWith("P1")) {
                return start.plus(Period.parse(resolution).multipliedBy(iteration));
            }
            throw new EntsoeResponseException("Unknown resolution: " + resolution);
        } catch (DateTimeParseException e) {
            throw new EntsoeResponseException(
                    "DateTimeParseException (ENTSOE resolution: " + resolution + "): " + e.getMessage(), e);
        }
    }

    private Unit<Energy> convertEntsoeUnit(String unit) throws EntsoeResponseException {
        if ("MWh".equalsIgnoreCase(unit)) {
            return Units.MEGAWATT_HOUR;
        }
        if ("kWh".equalsIgnoreCase(unit)) {
            return Units.KILOWATT_HOUR;
        }
        if ("Wh".equalsIgnoreCase(unit)) {
            return Units.WATT_HOUR;
        }

        throw new EntsoeResponseException("Unit from ENTSO-E is unknown: " + unit);
    }

    private BigDecimal getPrice(Unit<Energy> toUnit) {
        return new DecimalType(toUnit.getConverterTo(this.unit).convert(this.price)).toBigDecimal();
    }

    public State getState(Unit<Energy> toUnit) {
        try {
            return new QuantityType<>(getPrice(toUnit) + " " + this.currency + "/" + toUnit.toString());
        } catch (IllegalArgumentException e) {
            return new DecimalType(getPrice(toUnit));
        }
    }

    public Instant getInstant() {
        return this.time;
    }
}
