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
     * Liefert QuantityType mit originalem numerischen Wert und (ggf.) präfixierter Unit.
     * Gibt null zurück, wenn value nicht numerisch ist.
     */
    @Nullable
    public State resolveState(String key, JsonElement value) {
        if (value.isJsonNull() || !value.isJsonPrimitive())
            return null;
        JsonPrimitive prim = value.getAsJsonPrimitive();
        if (prim.isNumber()) {
            double raw = prim.getAsDouble();
            Unit<?> base = determineBaseUnitFromKey(key);
            if (key.contains("odometer") || key.contains("range") || key.contains("capacity")) {
                return new QuantityType<>(raw, MetricPrefix.KILO(base));
            }
            return new QuantityType<>(raw, base);
        } else if (prim.isString()) {
            if (key.contains("timestamp")) {
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

    /* ----- Heuristik zur Basiseinheit ----- */
    private Unit<?> determineBaseUnitFromKey(String key) {
        String lower = key.toLowerCase();

        if (lower.contains("soc") || lower.contains("percentage"))
            return Units.PERCENT;
        if (lower.contains("power") || lower.contains("threshold"))
            return Units.WATT;
        if (lower.contains("energy") || lower.contains("capacity"))
            return Units.WATT_HOUR;
        if (lower.contains("temp") || lower.contains("temperature") || lower.contains("heating"))
            return SIUnits.CELSIUS;
        if (lower.contains("voltage"))
            return Units.VOLT;
        if (lower.contains("current"))
            return Units.AMPERE;
        if (lower.contains("duration") || lower.contains("time") || lower.contains("delay")
                || lower.contains("remaining"))
            return Units.SECOND;
        if (lower.contains("odometer") || lower.contains("distance"))
            return SIUnits.METRE;
        if (lower.contains("co2") || lower.contains("co2perkwh"))
            return Units.GRAM_PER_KILOWATT_HOUR;
        return Units.ONE;
    }
}
