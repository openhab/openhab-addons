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
package org.openhab.binding.evcc.internal.handler;

import java.util.regex.Pattern;

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

    // Regex patterns for key matching (case-insensitive)
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("(?i)(price|tariff)");
    private static final Pattern KILO_EQ_PATTERN = Pattern
            .compile("(?i)^(energy|grid-energy|charged-energy|pv-energy|charged-kwh)$");
    private static final Pattern KILO_CONTAINS_PATTERN = Pattern
            .compile("(?i)(limit-energy|import|capacity|odometer|range)");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(?i)(timestamp)");

    /**
     * Resolves an openHAB {@link State} from a given JSON element and semantic key.
     * <p>
     * This method determines the appropriate {@link State} type based on:
     * <ul>
     * <li>Numeric values → {@link QuantityType} or {@link DecimalType}</li>
     * <li>String values → {@link StringType} or {@link DateTimeType}</li>
     * <li>Boolean values → {@link OnOffType}</li>
     * </ul>
     * The key is analyzed using regex-based heuristics to infer semantic meaning
     * (e.g., energy → Wh, odometer → m, price → DecimalType).
     *
     * @param key the semantic key associated with the value (e.g., "Odometer", "Price", "Timestamp")
     * @param value the JSON element to convert into an openHAB {@link State}
     * @return the resolved {@link State}, or {@code null} if the value is not a supported primitive
     */

    @Nullable
    public State resolveState(String key, JsonElement value) {
        if (value.isJsonNull() || !value.isJsonPrimitive()) {
            return null;
        }

        final JsonPrimitive prim = value.getAsJsonPrimitive();

        if (prim.isNumber()) {
            return resolveNumberState(key, prim.getAsNumber());
        }
        if (prim.isString()) {
            return resolveStringState(key, prim.getAsString());
        }
        if (prim.isBoolean()) {
            return prim.getAsBoolean() ? OnOffType.ON : OnOffType.OFF;
        }
        return null;
    }

    /**
     * Resolves a numeric JSON value into an openHAB {@link State}.
     * <p>
     * The resolution process:
     * <ol>
     * <li>If the key matches decimal patterns (e.g., price, tariff), returns {@link DecimalType}.</li>
     * <li>If the key matches energy or capacity patterns, applies {@link MetricPrefix#KILO} to the base unit.</li>
     * <li>Otherwise, uses the inferred base unit from {@link #determineBaseUnitFromKey(String)}.</li>
     * </ol>
     *
     * @param key the semantic key (case-insensitive)
     * @param raw the numeric value to convert
     * @return the resolved {@link State} as {@link QuantityType} or {@link DecimalType}
     */
    private State resolveNumberState(String key, Number raw) {
        // Decimal overrides first
        if (isDecimalKey(key)) {
            return new DecimalType(raw);
        }

        final Unit<?> base = determineBaseUnitFromKey(key);

        // Exact-match KILO keys
        if (KILO_EQ_PATTERN.matcher(key).matches()) {
            return new QuantityType<>(raw, MetricPrefix.KILO(base));
        }

        // Contains-match KILO keys
        if (KILO_CONTAINS_PATTERN.matcher(key).find()) {
            return new QuantityType<>(raw, MetricPrefix.KILO(base));
        }

        // Default: base unit
        return new QuantityType<>(raw, base);
    }

    /**
     * Checks if the given key represents a decimal-like value (e.g., price or tariff).
     * <p>
     * Uses a precompiled regex pattern for case-insensitive matching.
     *
     * @param key the semantic key to check
     * @return {@code true} if the key matches decimal patterns, {@code false} otherwise
     */

    private boolean isDecimalKey(String key) {
        return DECIMAL_PATTERN.matcher(key).find();
    }

    /**
     * Resolves a string JSON value into an openHAB {@link State}.
     * <p>
     * If the key matches timestamp patterns, returns {@link DateTimeType}.
     * Otherwise, returns {@link StringType}.
     *
     * @param key the semantic key (case-insensitive)
     * @param raw the string value to convert
     * @return the resolved {@link State} as {@link DateTimeType} or {@link StringType}
     */

    private State resolveStringState(String key, String raw) {
        if (TIMESTAMP_PATTERN.matcher(key).find()) {
            return new DateTimeType(raw);
        }
        return new StringType(raw);
    }

    /**
     * Infers the most appropriate base {@link Unit} for a given semantic key.
     * <p>
     * Performs case-insensitive keyword analysis to map keys to units:
     * <ul>
     * <li>"soc", "percentage" → %</li>
     * <li>"power" → W</li>
     * <li>"energy", "capacity" → Wh</li>
     * <li>"temperature" → °C</li>
     * <li>"voltage" → V</li>
     * <li>"current" → A</li>
     * <li>"duration", "time" → s</li>
     * <li>"odometer", "distance" → m</li>
     * <li>"co2" → g/kWh</li>
     * </ul>
     * Defaults to {@link Units#ONE} if no match is found.
     *
     * @param key the semantic key to analyze
     * @return the inferred {@link Unit}
     */
    private Unit<?> determineBaseUnitFromKey(String key) {
        String lower = key.toLowerCase();

        if (lower.contains("soc") || lower.contains("percentage")) {
            return Units.PERCENT;
        }
        if (lower.contains("power") || lower.contains("threshold") || lower.contains("tariffsolar")) {
            return Units.WATT;
        }
        if (lower.contains("energy") || lower.contains("capacity") || lower.contains("import")) {
            return Units.WATT_HOUR;
        }
        if (lower.contains("temp") || lower.contains("temperature") || lower.contains("heating")) {
            return SIUnits.CELSIUS;
        }
        if (lower.contains("voltage")) {
            return Units.VOLT;
        }
        if (lower.contains("current")) {
            return Units.AMPERE;
        }
        if (lower.contains("duration") || lower.contains("time") || lower.contains("delay")
                || lower.contains("interval") || lower.contains("remaining") || lower.contains("overrun")
                || lower.contains("precondition")) {
            return Units.SECOND;
        }
        if (lower.contains("odometer") || lower.contains("distance") || lower.contains("range")) {
            return SIUnits.METRE;
        }
        if (lower.contains("co2")) {
            return Units.GRAM_PER_KILOWATT_HOUR;
        }
        return Units.ONE;
    }
}
