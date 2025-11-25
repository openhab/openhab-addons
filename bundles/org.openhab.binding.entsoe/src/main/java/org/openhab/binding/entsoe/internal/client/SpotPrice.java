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
package org.openhab.binding.entsoe.internal.client;

import java.math.BigDecimal;
import java.util.Map;

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
 * @author Bernd Weymann - Remove timestamp from SpotPrice
 */
@NonNullByDefault
public class SpotPrice {
    private static final Map<String, Unit<Energy>> ENERGY_UNIT_MAP = Map.ofEntries(
            Map.entry("mwh", Units.MEGAWATT_HOUR), Map.entry("kwh", Units.KILOWATT_HOUR),
            Map.entry("wh", Units.WATT_HOUR));
    private static final Unit<Energy> DEFAULT_ENERGY_UNIT = Units.KILOWATT_HOUR;
    private Unit<Energy> entsoeUnit;
    private String currency;
    private Double price;

    public SpotPrice(String currency, String unit, Double price) throws EntsoeResponseException {
        this.currency = currency;
        this.price = price;
        this.entsoeUnit = convertEntsoeUnit(unit);
    }

    public State getState() {
        return getState(DEFAULT_ENERGY_UNIT);
    }

    public State getState(Unit<Energy> toUnit) {
        try {
            return new QuantityType<>(getPrice(toUnit) + " " + this.currency + "/" + toUnit.toString());
        } catch (IllegalArgumentException e) {
            return new DecimalType(getPrice(toUnit));
        }
    }

    private Unit<Energy> convertEntsoeUnit(String unit) throws EntsoeResponseException {
        Unit<Energy> decodedUnit = ENERGY_UNIT_MAP.get(unit.toLowerCase());
        if (decodedUnit != null) {
            return decodedUnit;
        }
        throw new EntsoeResponseException("Unit from ENTSO-E is unknown: " + unit);
    }

    private BigDecimal getPrice(Unit<Energy> toUnit) {
        return new DecimalType(toUnit.getConverterTo(this.entsoeUnit).convert(this.price)).toBigDecimal();
    }
}
