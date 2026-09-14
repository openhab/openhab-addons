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
 * A charger's OCPP configuration, read via {@code GetConfiguration}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChargerCapabilities {

    private static final String SUPPORTED_FEATURE_PROFILES = "SupportedFeatureProfiles";
    private static final String ALLOWED_CHARGING_RATE_UNIT = "ChargingScheduleAllowedChargingRateUnit";
    private static final String HEARTBEAT_INTERVAL = "HeartbeatInterval";
    private static final String NUMBER_OF_CONNECTORS = "NumberOfConnectors";
    private static final String CONNECTOR_SWITCH_3_TO_1_PHASE = "ConnectorSwitch3to1PhaseSupported";
    private static final String SMART_CHARGING_PROFILE = "SmartCharging";
    private static final String LOCAL_AUTH_LIST_PROFILE = "LocalAuthListManagement";
    private static final String RATE_UNIT_CURRENT = "Current";
    private static final String RATE_UNIT_POWER = "Power";

    private final Map<String, String> raw;

    private ChargerCapabilities(Map<String, String> raw) {
        this.raw = raw;
    }

    /** The capabilities of a charger that has not been successfully queried. */
    public static ChargerCapabilities unknown() {
        return new ChargerCapabilities(Map.of());
    }

    /** Build from a {@code GetConfiguration} response. */
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

    public Map<String, String> raw() {
        return raw;
    }

    public boolean isEmpty() {
        return raw.isEmpty();
    }

    public Optional<Set<String>> featureProfiles() {
        return csl(SUPPORTED_FEATURE_PROFILES);
    }

    public Optional<Boolean> supportsFeatureProfile(String profile) {
        return featureProfiles().map(set -> set.stream().anyMatch(p -> p.equalsIgnoreCase(profile)));
    }

    public Optional<Boolean> supportsSmartCharging() {
        return supportsFeatureProfile(SMART_CHARGING_PROFILE);
    }

    public Optional<Boolean> supportsLocalAuthList() {
        return supportsFeatureProfile(LOCAL_AUTH_LIST_PROFILE);
    }

    public Optional<Set<String>> allowedChargingRateUnits() {
        return csl(ALLOWED_CHARGING_RATE_UNIT);
    }

    public Optional<Boolean> allowsCurrentUnit() {
        return allowedChargingRateUnits().map(units -> units.stream().anyMatch(RATE_UNIT_CURRENT::equalsIgnoreCase));
    }

    public Optional<Boolean> allowsPowerUnit() {
        return allowedChargingRateUnits().map(units -> units.stream().anyMatch(RATE_UNIT_POWER::equalsIgnoreCase));
    }

    public OptionalInt heartbeatIntervalSeconds() {
        return integer(HEARTBEAT_INTERVAL);
    }

    public OptionalInt numberOfConnectors() {
        return integer(NUMBER_OF_CONNECTORS);
    }

    public Optional<Boolean> phaseSwitchSupported() {
        return bool(CONNECTOR_SWITCH_3_TO_1_PHASE);
    }

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

    public Optional<Set<String>> csl(String key) {
        String value = raw.get(key);
        if (value == null) {
            return Optional.empty();
        }
        Set<String> set = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Optional.of(set);
    }

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
