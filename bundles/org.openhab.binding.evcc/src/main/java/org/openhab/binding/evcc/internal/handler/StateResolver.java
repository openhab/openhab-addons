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
package org.openhab.binding.evcc.internal.handler;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link StateResolver} provides a resolver to determine the appropriate State type based on the JSON value and key
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public final class StateResolver {

    private static final StateResolver INSTANCE = new StateResolver();

    public static StateResolver getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves a {@link State} object from a given JSON element based on the provided key.
     * This method interprets the JSON value heuristically, mapping numeric values to
     * {@link QuantityType} or {@link DecimalType}, strings to {@link StringType} or {@link DateTimeType},
     * and booleans to {@link OnOffType}. The key is used to infer the appropriate unit of measurement
     * or semantic meaning.
     *
     * @param key the semantic key associated with the value (e.g. "Odometer", "Timestamp", "Price")
     * @param value the JSON element to be converted into an openHAB {@link State}
     * @return the resolved {@link State}, or {@code null} if the value is not a supported primitive
     */
    @Nullable
    public State resolveState(String key, JsonElement value) {
        if (value.isJsonNull() || !value.isJsonPrimitive())
            return null;
        JsonPrimitive prim = value.getAsJsonPrimitive();
        if (prim.isNumber()) {
            double raw = prim.getAsDouble();
            Unit<?> base = determineBaseUnitFromKey(key);
            if (key.contains("Odometer") || key.contains("Range") || key.contains("Capacity")
                    || key.contains("limitEnergy")) {
                return new QuantityType<>(raw, MetricPrefix.KILO(base));
            } else if (key.contains("Price") || key.contains("Tariff") || key.contains("tariff")) {
                return new DecimalType(raw);
            }
            return new QuantityType<>(raw, base);
        } else if (prim.isString()) {
            if (key.contains("Timestamp")) {
                return new DateTimeType(value.getAsString());
            } else {
                return new StringType(value.getAsString());
            }
        } else if (prim.isBoolean()) {
            return value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF;
        } else {
            return null;
        }
    }

    /**
     * Determines the most appropriate base {@link Units} for a given key using keyword heuristics.
     * This method performs a case-insensitive analysis of the key to infer the expected unit
     * (e.g. "temperature" → °C, "energy" → Wh). It is used to assign meaningful units to numeric values
     * when constructing {@link QuantityType} instances.
     *
     * @param key the semantic key to analyze (e.g. "chargingPower", "batterySoc", "sessionEnergy")
     * @return the inferred {@link Units}/{@link SIUnits}, or {@link Units#ONE} if no specific unit is matched
     */
    private Unit<?> determineBaseUnitFromKey(String key) {
        String lower = key.toLowerCase();

        if (lower.contains("soc") || lower.contains("percentage"))
            return Units.PERCENT;
        if (lower.contains("power") || lower.contains("threshold") || lower.contains("tariffsolar"))
            return Units.WATT;
        if (lower.contains("energy") || lower.contains("capacity") || lower.contains("import"))
            return Units.WATT_HOUR;
        if (lower.contains("temp") || lower.contains("temperature") || lower.contains("heating"))
            return SIUnits.CELSIUS;
        if (lower.contains("voltage"))
            return Units.VOLT;
        if (lower.contains("current"))
            return Units.AMPERE;
        if (lower.contains("duration") || lower.contains("time") || lower.contains("delay")
                || lower.contains("remaining") || lower.contains("overrun") || lower.contains("precondition"))
            return Units.SECOND;
        if (lower.contains("odometer") || lower.contains("distance") || lower.contains("range"))
            return SIUnits.METRE;
        if (lower.contains("co2"))
            return Units.GRAM_PER_KILOWATT_HOUR;
        return Units.ONE;
    }
}
