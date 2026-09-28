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
package org.openhab.binding.dreame.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.util.DreameVacuumDiagnostics;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

/**
 * Covers the observed L50 transitions and rejects unsafe or unrelated property values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumStatusTest {
    @Test
    void mapsObservedCleaningPauseReturnAndCharging() {
        int[][] codes = { { 1, 2 }, { 3, 1 }, { 5, 3 }, { 6, 6 } };
        String[] labels = { "CLEANING", "PAUSED", "RETURNING", "CHARGING" };
        for (int i = 0; i < codes.length; i++) {
            var updates = DreameVacuumStatus.channelUpdates(Map.of("2/1", codes[i][0], "4/1", codes[i][1]));
            assertEquals(new StringType(labels[i]), updates.get("state"));
            assertEquals(new StringType(labels[i]), updates.get("operating-status"));
        }
        assertEquals(new StringType("NOT_CHARGING"),
                DreameVacuumStatus.channelUpdates(Map.of("3/2", 2)).get("charging-status"));
        assertEquals(new StringType("RETURNING"),
                DreameVacuumStatus.channelUpdates(Map.of("3/2", 5)).get("charging-status"));
        assertEquals(new StringType("CHARGING"),
                DreameVacuumStatus.channelUpdates(Map.of("3/2", 1)).get("charging-status"));
    }

    @Test
    void namesNewlyObservedDockStatesWithoutChangingOtherStatusFields() {
        assertEquals(new StringType("CHARGING_COMPLETED"),
                DreameVacuumStatus.channelUpdates(Map.of("2/1", 13)).get("state"));
        assertEquals(new StringType("AUTO_EMPTYING"),
                DreameVacuumStatus.channelUpdates(Map.of("2/1", 22)).get("state"));
        assertEquals(new StringType("UNKNOWN_22"),
                DreameVacuumStatus.channelUpdates(Map.of("4/1", 22)).get("operating-status"));
    }

    @Test
    void distinguishesDryingStateFromSleepingOperatingStatus() {
        var updates = DreameVacuumStatus.channelUpdates(Map.of("2/1", 8, "4/1", 14));
        assertEquals(new StringType("DRYING"), updates.get("state"));
        assertEquals(new StringType("SLEEPING"), updates.get("operating-status"));
        assertEquals(new StringType("UNKNOWN_14"), DreameVacuumStatus.channelUpdates(Map.of("2/1", 14)).get("state"));
        assertEquals(new StringType("UNKNOWN_8"),
                DreameVacuumStatus.channelUpdates(Map.of("4/1", 8)).get("operating-status"));
    }

    @Test
    void preservesUnknownCodesAndDoesNotInventMissingStates() {
        var updates = DreameVacuumStatus.channelUpdates(Map.of("2/1", 255, "3/1", 96, "2/2", 0, "4/7", 1));
        assertEquals(new StringType("UNKNOWN_255"), updates.get("state"));
        assertEquals(new DecimalType(96), updates.get("battery-level"));
        assertEquals(DecimalType.ZERO, updates.get("error-code"));
        assertEquals(new StringType("AUTO_CLEANING"), updates.get("task-status"));
        assertEquals(4, updates.size());
        assertEquals(new StringType("UNKNOWN_99"),
                DreameVacuumStatus.channelUpdates(Map.of("4/1", 99)).get("operating-status"));
        assertEquals(new StringType("UNKNOWN_0"),
                DreameVacuumStatus.channelUpdates(Map.of("3/2", 0)).get("charging-status"));
    }

    @Test
    void readsNestedUpdatesAndIgnoresFailedOrInvalidValues() {
        var values = read("""
                {"data":{"method":"properties_changed","params":[
                {"siid":3,"piid":1,"value":99},
                {"siid":2,"piid":1,"value":1,"code":0},
                {"siid":2,"piid":2,"value":0,"code":-1},
                {"siid":3,"piid":2,"value":"1"},
                {"siid":4,"piid":1,"value":1.5},
                {"siid":4,"piid":7,"value":256},
                {"siid":6,"piid":1,"value":"private"}]}}
                """);
        assertEquals(Map.of("3/1", 99, "2/1", 1), values);
        assertTrue(read("{broken").isEmpty());
        assertTrue(read("{\"method\":\"event_occured\",\"params\":[{\"siid\":3,\"piid\":1,\"value\":99}]}").isEmpty());
        assertTrue(read("{\"method\":\"properties_changed\",\"params\":[{\"siid\":3,\"piid\":1,\"value\":101}]}")
                .isEmpty());
    }

    @Test
    void enforcesModelSizeDepthAndParameterBounds() {
        String property = "{\"siid\":3,\"piid\":1,\"value\":96}";
        String json = "{\"method\":\"properties_changed\",\"params\":[" + property + "]}";
        assertTrue(DreameVacuumDiagnostics.readProperties(json.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.other")
                .isEmpty());
        assertTrue(read(" ".repeat(65537)).isEmpty());
        assertTrue(read("[".repeat(33) + "0" + "]".repeat(33)).isEmpty());
        assertTrue(
                read("{\"method\":\"properties_changed\",\"params\":[" + "{},".repeat(32) + property + "]}").isEmpty());
    }

    private static Map<String, Integer> read(String json) {
        return DreameVacuumDiagnostics.readProperties(json.getBytes(StandardCharsets.UTF_8), "dreame.vacuum.r9445d");
    }

    @Test
    void mapsObservedMopSessionStates() {
        Map.of(9, "WASHING", 12, "SWEEPING_AND_MOPPING", 20, "CLEAN_ADD_WATER").forEach((code, name) -> {
            assertEquals(new StringType(name), DreameVacuumStatus.channelUpdates(Map.of("2/1", code)).get("state"));
            assertEquals(new StringType("UNKNOWN_" + code),
                    DreameVacuumStatus.channelUpdates(Map.of("4/1", code)).get("operating-status"));
        });
    }

    @Test
    void mapsCleaningProgressModesAndBaseStatus() {
        var updates = DreameVacuumStatus
                .channelUpdates(Map.of("4/2", 37, "4/3", 82, "4/4", 3, "4/5", 2, "4/7", 3, "4/23", 2, "4/25", 1));
        assertEquals(new DecimalType(37), updates.get("cleaning-time"));
        assertEquals(new DecimalType(82), updates.get("cleaned-area"));
        assertEquals(new StringType("TURBO"), updates.get("suction-level"));
        assertEquals(new StringType("MEDIUM"), updates.get("water-volume"));
        assertEquals(new StringType("ROOM_CLEANING"), updates.get("task-status"));
        assertEquals(new StringType("SWEEPING"), updates.get("cleaning-mode"));
        assertEquals(new StringType("WASHING"), updates.get("base-status"));
    }

    @Test
    void decodesGroupedL50CleaningMode() {
        assertEquals(new StringType("SWEEPING_AND_MOPPING"),
                DreameVacuumStatus.channelUpdates(Map.of("4/23", 0x1400)).get("cleaning-mode"));
        assertEquals(new StringType("MOPPING"),
                DreameVacuumStatus.channelUpdates(Map.of("4/23", 0x1401)).get("cleaning-mode"));
        assertEquals(new StringType("SWEEPING"),
                DreameVacuumStatus.channelUpdates(Map.of("4/23", 0x1402)).get("cleaning-mode"));
    }

    @Test
    void mapsDryingTimeAndStructuredAutoSwitchSettings() {
        var properties = new DreameVacuumProperties(Map.of("4/40", 3),
                Map.of("4/50", "[{\"k\":\"SmartHost\",\"v\":2},{\"k\":\"CleanRoute\",\"v\":4}]"));
        var updates = DreameVacuumStatus.channelUpdates(properties);
        assertEquals(new StringType("3H"), updates.get("drying-time"));
        assertEquals(new StringType("DEEP"), updates.get("clean-genius"));
        assertEquals(new StringType("QUICK"), updates.get("cleaning-route"));

        updates = DreameVacuumStatus.channelUpdates(
                new DreameVacuumProperties(Map.of("4/40", 2), Map.of("4/50", "{\"k\":\"SmartHost\",\"v\":0}")));
        assertEquals(new StringType("2H"), updates.get("drying-time"));
        assertEquals(new StringType("OFF"), updates.get("clean-genius"));
        assertFalse(updates.containsKey("cleaning-route"));
    }

    @Test
    void ignoresMalformedAutoSwitchWithoutDiscardingNumericProperties() {
        var updates = DreameVacuumStatus
                .channelUpdates(new DreameVacuumProperties(Map.of("4/40", 4), Map.of("4/50", "invalid")));
        assertEquals(Map.of("drying-time", new StringType("4H")), updates);
    }

    @Test
    void mapsConsumablesAndLifetimeStatistics() {
        var updates = DreameVacuumStatus.channelUpdates(Map.ofEntries(Map.entry("9/1", 240), Map.entry("9/2", 80),
                Map.entry("10/1", 120), Map.entry("10/2", 70), Map.entry("11/1", 60), Map.entry("11/2", 180),
                Map.entry("16/1", 50), Map.entry("16/2", 30), Map.entry("18/1", 40), Map.entry("18/2", 90),
                Map.entry("20/1", 30), Map.entry("20/2", 20), Map.entry("12/2", 1234), Map.entry("12/3", 42),
                Map.entry("12/4", 5678)));
        assertEquals(new DecimalType(240), updates.get("main-brush-time-left"));
        assertEquals(new DecimalType(80), updates.get("main-brush-left"));
        assertEquals(new DecimalType(120), updates.get("side-brush-time-left"));
        assertEquals(new DecimalType(70), updates.get("side-brush-left"));
        assertEquals(new DecimalType(60), updates.get("filter-left"));
        assertEquals(new DecimalType(180), updates.get("filter-time-left"));
        assertEquals(new DecimalType(50), updates.get("sensor-dirty-left"));
        assertEquals(new DecimalType(30), updates.get("sensor-dirty-time-left"));
        assertEquals(new DecimalType(40), updates.get("mop-pad-left"));
        assertEquals(new DecimalType(90), updates.get("mop-pad-time-left"));
        assertEquals(new DecimalType(30), updates.get("detergent-left"));
        assertEquals(new DecimalType(20), updates.get("detergent-time-left"));
        assertEquals(new DecimalType(1234), updates.get("total-cleaning-time"));
        assertEquals(new DecimalType(42), updates.get("cleaning-count"));
        assertEquals(new DecimalType(5678), updates.get("total-cleaned-area"));
        assertEquals(15, updates.size());
    }
}
