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
import java.time.ZonedDateTime;

import javax.measure.Unit;
import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * @author Jørgen Melhus
 */
@NonNullByDefault
public class EntsoeTimeSerie {
    private String currency;
    private Unit<Energy> unit;
    private Double price;
    private ZonedDateTime time;

    public EntsoeTimeSerie(String currency, String unit, Double price, ZonedDateTime time)
            throws EntsoeResponseException {
        this.currency = currency;
        this.unit = convertEntsoeUnit(unit);
        this.price = price;
        this.time = time;
    }

    private Unit<Energy> convertEntsoeUnit(String unit) throws EntsoeResponseException {
        if (unit.equalsIgnoreCase("MWh"))
            return Units.MEGAWATT_HOUR;
        if (unit.equalsIgnoreCase("kWh"))
            return Units.KILOWATT_HOUR;
        if (unit.equalsIgnoreCase("Wh"))
            return Units.WATT_HOUR;

        throw new EntsoeResponseException("Unit from ENTSO-E is unknown: " + unit);
    }

    public String getCurrency() {
        return currency;
    }

    public Unit<Energy> getUnit() {
        return unit;
    }

    private BigDecimal getPrice(Unit<Energy> toUnit) {
        return new DecimalType(toUnit.getConverterTo(this.unit).convert(this.price)).toBigDecimal();
    }

    public State getState(Unit<Energy> toUnit) {
        try {
            return new QuantityType<>(getPrice(toUnit) + " " + getCurrency() + "/" + toUnit.toString());
        } catch (IllegalArgumentException e) {
            return new DecimalType(getPrice(toUnit));
        }
    }

    public ZonedDateTime getTime() {
        return time;
    }
}
