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
package org.openhab.binding.ocpp.internal.transport;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.KeyValueType;

/**
 * A charger's OCPP configuration, read via {@code GetConfiguration} and parsed into the facts the
 * binding adapts to (supported feature profiles, accepted charge-limit unit, heartbeat, ...).
 *
 * <p>
 * Immutable and defensive: an absent or unparseable key yields an empty result, never an exception.
 * Only a positive signal — a key present and parseable — changes behaviour, so callers treat "unknown"
 * as "behave as before" and a charger that reports nothing is never worse off.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChargerCapabilities {

    // Standard OCPP 1.6 configuration key names (spec §9); only the key spellings are fixed here, the
    // values are read from the charger.
    private static final String SUPPORTED_FEATURE_PROFILES = "SupportedFeatureProfiles";
    private static final String ALLOWED_CHARGING_RATE_UNIT = "ChargingScheduleAllowedChargingRateUnit";
    private static final String HEARTBEAT_INTERVAL = "HeartbeatInterval";
    private static final String NUMBER_OF_CONNECTORS = "NumberOfConnectors";
    private static final String CONNECTOR_SWITCH_3_TO_1_PHASE = "ConnectorSwitch3to1PhaseSupported";
    private static final String SMART_CHARGING_PROFILE = "SmartCharging";
    private static final String LOCAL_AUTH_LIST_PROFILE = "LocalAuthListManagement";
    // The two ChargingRateUnitType values a charger may allow (OCPP reports them as words, not A/W).
    private static final String RATE_UNIT_CURRENT = "Current";
    private static final String RATE_UNIT_POWER = "Power";

    private final Map<String, String> raw;

    private ChargerCapabilities(Map<String, String> raw) {
        this.raw = raw;
    }

    /** The capabilities of a charger that has not been (successfully) queried: every lookup is empty. */
    public static ChargerCapabilities unknown() {
        return new ChargerCapabilities(Map.of());
    }

    /**
     * Build from a {@code GetConfiguration} response. A null response, a null key array, or entries
     * with a null key/value are skipped; a response with nothing usable yields {@link #unknown()}.
     */
    public static ChargerCapabilities from(@Nullable GetConfigurationConfirmation confirmation) {
        if (confirmation == null) {
            return unknown();
        }
        KeyValueType @Nullable [] keys = confirmation.getConfigurationKey();
        if (keys == null) {
            return unknown();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (@Nullable
        KeyValueType entry : keys) {
            if (entry == null) {
                continue;
            }
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && !key.isBlank() && value != null) {
                map.put(key, value);
            }
        }
        return new ChargerCapabilities(Collections.unmodifiableMap(map));
    }

    /** Every configuration key/value the charger reported, in the order it reported them. */
    public Map<String, String> raw() {
        return raw;
    }

    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /** The parsed {@code SupportedFeatureProfiles}, or empty when the charger did not report the key. */
    public Optional<Set<String>> featureProfiles() {
        return csl(SUPPORTED_FEATURE_PROFILES);
    }

    /**
     * Whether the charger lists a named feature profile (e.g. {@code SmartCharging}). Empty when it did
     * not report {@code SupportedFeatureProfiles} at all — the caller must then NOT assume "no".
     */
    public Optional<Boolean> supportsFeatureProfile(String profile) {
        return featureProfiles().map(set -> set.stream().anyMatch(p -> p.equalsIgnoreCase(profile)));
    }

    public Optional<Boolean> supportsSmartCharging() {
        return supportsFeatureProfile(SMART_CHARGING_PROFILE);
    }

    public Optional<Boolean> supportsLocalAuthList() {
        return supportsFeatureProfile(LOCAL_AUTH_LIST_PROFILE);
    }

    /** The allowed charge-limit units ({@code Current} / {@code Power}), or empty if not reported. */
    public Optional<Set<String>> allowedChargingRateUnits() {
        return csl(ALLOWED_CHARGING_RATE_UNIT);
    }

    /** Whether the charger accepts a charge limit expressed in Amperes, or empty if it did not report. */
    public Optional<Boolean> allowsCurrentUnit() {
        return allowedChargingRateUnits().map(units -> units.stream().anyMatch(RATE_UNIT_CURRENT::equalsIgnoreCase));
    }

    /** Whether the charger accepts a charge limit expressed in Watts, or empty if it did not report. */
    public Optional<Boolean> allowsPowerUnit() {
        return allowedChargingRateUnits().map(units -> units.stream().anyMatch(RATE_UNIT_POWER::equalsIgnoreCase));
    }

    public OptionalInt heartbeatIntervalSeconds() {
        return integer(HEARTBEAT_INTERVAL);
    }

    public OptionalInt numberOfConnectors() {
        return integer(NUMBER_OF_CONNECTORS);
    }

    /** Whether the charger can switch a connector between 3 and 1 phase, or empty if not reported. */
    public Optional<Boolean> phaseSwitchSupported() {
        return bool(CONNECTOR_SWITCH_3_TO_1_PHASE);
    }

    /** The value of {@code key} parsed as an int, or empty when absent or not a number. */
    public OptionalInt integer(String key) {
        String value = raw.get(key);
        if (value == null) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /** The value of {@code key} parsed as a boolean, or empty when absent or not {@code true}/{@code false}. */
    public Optional<Boolean> bool(String key) {
        String value = raw.get(key);
        if (value == null) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }

    /** The value of {@code key} parsed as a comma-separated list (trimmed, de-duplicated), or empty. */
    public Optional<Set<String>> csl(String key) {
        String value = raw.get(key);
        if (value == null) {
            return Optional.empty();
        }
        Set<String> set = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Optional.of(set);
    }

    /** A compact one-line summary for logging; "unknown" where the charger reported nothing. */
    public String summary() {
        return "features=" + featureProfiles().map(Object::toString).orElse("unknown") //
                + ", rateUnit=" + allowedChargingRateUnits().map(Object::toString).orElse("unknown") //
                + ", heartbeat="
                + (heartbeatIntervalSeconds().isPresent() ? heartbeatIntervalSeconds().getAsInt() + "s" : "unknown") //
                + ", connectors="
                + (numberOfConnectors().isPresent() ? Integer.toString(numberOfConnectors().getAsInt()) : "unknown") //
                + ", phaseSwitch=" + phaseSwitchSupported().map(Object::toString).orElse("unknown");
    }
}
