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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.KeyValueType;

/**
 * Tests that {@link ChargerCapabilities} parses a GetConfiguration response and, per its
 * "positive signal only" contract, returns empty rather than a false negative for absent or malformed
 * keys.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ChargerCapabilitiesTest {

    private static KeyValueType kv(String key, String value) {
        KeyValueType entry = new KeyValueType(key, Boolean.TRUE);
        entry.setValue(value);
        return entry;
    }

    private static ChargerCapabilities caps(KeyValueType... entries) {
        GetConfigurationConfirmation confirmation = new GetConfigurationConfirmation();
        confirmation.setConfigurationKey(entries);
        return ChargerCapabilities.from(confirmation);
    }

    @Test
    void unknownHasNothing() {
        ChargerCapabilities caps = ChargerCapabilities.unknown();
        assertTrue(caps.isEmpty());
        assertTrue(caps.featureProfiles().isEmpty());
        assertTrue(caps.supportsSmartCharging().isEmpty());
        assertTrue(caps.allowedChargingRateUnits().isEmpty());
        assertTrue(caps.heartbeatIntervalSeconds().isEmpty());
        assertTrue(caps.phaseSwitchSupported().isEmpty());
    }

    @Test
    void aNullConfirmationYieldsUnknown() {
        assertTrue(ChargerCapabilities.from(null).isEmpty());
    }

    @Test
    void aConfirmationWithNoKeysYieldsUnknown() {
        // getConfigurationKey() is null when the charger reported nothing.
        assertTrue(ChargerCapabilities.from(new GetConfigurationConfirmation()).isEmpty());
    }

    @Test
    void parsesAFullConfiguration() {
        ChargerCapabilities caps = caps(kv("SupportedFeatureProfiles", "Core,SmartCharging,RemoteTrigger"),
                kv("ChargingScheduleAllowedChargingRateUnit", "Current,Power"), kv("HeartbeatInterval", "300"),
                kv("NumberOfConnectors", "2"), kv("ConnectorSwitch3to1PhaseSupported", "true"));

        assertEquals(Set.of("Core", "SmartCharging", "RemoteTrigger"), caps.featureProfiles().orElseThrow());
        assertTrue(caps.supportsSmartCharging().orElseThrow());
        assertTrue(caps.supportsFeatureProfile("RemoteTrigger").orElseThrow());
        assertEquals(Set.of("Current", "Power"), caps.allowedChargingRateUnits().orElseThrow());
        assertEquals(300, caps.heartbeatIntervalSeconds().orElseThrow());
        assertEquals(2, caps.numberOfConnectors().orElseThrow());
        assertTrue(caps.phaseSwitchSupported().orElseThrow());
    }

    @Test
    void featureProfileMatchIsCaseInsensitiveAndTrimmed() {
        ChargerCapabilities caps = caps(kv("SupportedFeatureProfiles", " Core , smartcharging "));
        assertEquals(Set.of("Core", "smartcharging"), caps.featureProfiles().orElseThrow());
        assertTrue(caps.supportsSmartCharging().orElseThrow(), "match must ignore case");
    }

    @Test
    void aChargerWithoutSmartChargingReportsFalseNotUnknown() {
        ChargerCapabilities caps = caps(kv("SupportedFeatureProfiles", "Core,FirmwareManagement"));
        assertFalse(caps.supportsSmartCharging().orElseThrow());
    }

    @Test
    void anAbsentFeatureProfilesKeyIsUnknownNotNo() {
        ChargerCapabilities caps = caps(kv("HeartbeatInterval", "60"));
        assertTrue(caps.supportsSmartCharging().isEmpty());
    }

    @Test
    void aNonNumericIntegerIsEmptyNotZero() {
        ChargerCapabilities caps = caps(kv("HeartbeatInterval", "not-a-number"));
        assertTrue(caps.heartbeatIntervalSeconds().isEmpty());
    }

    @Test
    void booleanParsingIsTolerantAndSafe() {
        assertTrue(caps(kv("ConnectorSwitch3to1PhaseSupported", "TRUE")).phaseSwitchSupported().orElseThrow());
        assertFalse(caps(kv("ConnectorSwitch3to1PhaseSupported", "false")).phaseSwitchSupported().orElseThrow());
        assertTrue(caps(kv("ConnectorSwitch3to1PhaseSupported", "maybe")).phaseSwitchSupported().isEmpty());
    }

    @Test
    void anEntryWithNoValueIsSkipped() {
        // value is optional in a KeyValueType.
        ChargerCapabilities caps = caps(new KeyValueType("HeartbeatInterval", Boolean.TRUE),
                kv("NumberOfConnectors", "1"));
        assertTrue(caps.heartbeatIntervalSeconds().isEmpty());
        assertEquals(1, caps.numberOfConnectors().orElseThrow());
    }

    @Test
    void rawKeepsEveryReportedKey() {
        ChargerCapabilities caps = caps(kv("A", "1"), kv("B", "two"));
        assertEquals("1", caps.raw().get("A"));
        assertEquals("two", caps.raw().get("B"));
        assertEquals(2, caps.raw().size());
    }

    @Test
    void rateUnitFlagsReflectTheAllowedSet() {
        ChargerCapabilities both = caps(kv("ChargingScheduleAllowedChargingRateUnit", "Current, Power"));
        assertTrue(both.allowsCurrentUnit().orElseThrow());
        assertTrue(both.allowsPowerUnit().orElseThrow());

        ChargerCapabilities powerOnly = caps(kv("ChargingScheduleAllowedChargingRateUnit", "Power"));
        assertFalse(powerOnly.allowsCurrentUnit().orElseThrow());
        assertTrue(powerOnly.allowsPowerUnit().orElseThrow());

        assertTrue(caps(kv("HeartbeatInterval", "10")).allowsPowerUnit().isEmpty());
    }
}
