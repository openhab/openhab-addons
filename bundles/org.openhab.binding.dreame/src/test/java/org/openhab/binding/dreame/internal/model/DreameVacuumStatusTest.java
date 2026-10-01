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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

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
        assertEquals(new StringType("RETURNING_TO_WASHING"),
                DreameVacuumStatus.channelUpdates(Map.of("2/1", 10)).get("state"));
        assertEquals(new StringType("CHARGING_COMPLETED"),
                DreameVacuumStatus.channelUpdates(Map.of("2/1", 13)).get("state"));
        assertEquals(new StringType("AUTO_EMPTYING"),
                DreameVacuumStatus.channelUpdates(Map.of("2/1", 22)).get("state"));
        assertEquals(new StringType("CRUISING_PATH"),
                DreameVacuumStatus.channelUpdates(Map.of("4/1", 22)).get("operating-status"));
    }

    @Test
    void mapsCommonDeviceStatesWithoutGuessingModelSpecificCodes() {
        Map.ofEntries(Map.entry(2, "IDLE"), Map.entry(4, "ERROR"), Map.entry(7, "MOPPING"), Map.entry(11, "BUILDING"),
                Map.entry(14, "UPGRADING"), Map.entry(15, "CLEAN_SUMMON"), Map.entry(16, "STATION_RESET"),
                Map.entry(17, "RETURNING_INSTALL_MOP"), Map.entry(18, "RETURNING_REMOVE_MOP"))
                .forEach((code, name) -> assertEquals(new StringType(name),
                        DreameVacuumStatus.channelUpdates(Map.of("2/1", code)).get("state")));
        assertEquals(new StringType("UNKNOWN_19"), DreameVacuumStatus.channelUpdates(Map.of("2/1", 19)).get("state"));
    }

    @Test
    void mapsReferenceOperatingStatuses() {
        Map.ofEntries(Map.entry(0, "IDLE"), Map.entry(4, "PARTIAL_CLEANING"), Map.entry(5, "FOLLOW_WALL"),
                Map.entry(7, "OTA"), Map.entry(8, "FCT"), Map.entry(9, "WIFI_SETUP"), Map.entry(10, "POWER_OFF"),
                Map.entry(11, "FACTORY"), Map.entry(12, "ERROR"), Map.entry(13, "REMOTE_CONTROL"),
                Map.entry(15, "SELF_REPAIR"), Map.entry(16, "FACTORY_FUNCTION_TEST"), Map.entry(17, "STANDBY"),
                Map.entry(19, "ZONE_CLEANING"), Map.entry(20, "SPOT_CLEANING"), Map.entry(21, "FAST_MAPPING"),
                Map.entry(22, "CRUISING_PATH"), Map.entry(23, "CRUISING_POINT"), Map.entry(24, "SUMMON_CLEAN"),
                Map.entry(25, "SHORTCUT"), Map.entry(26, "PERSON_FOLLOW"), Map.entry(27, "PET_GUARDING"),
                Map.entry(28, "AUTO_ARRANGEMENT"), Map.entry(29, "SMART_ARRANGEMENT"),
                Map.entry(30, "ZONED_ARRANGEMENT"), Map.entry(1501, "WATER_CHECK"))
                .forEach((code, name) -> assertEquals(new StringType(name),
                        DreameVacuumStatus.channelUpdates(Map.of("4/1", code)).get("operating-status")));
    }

    @Test
    void mapsPausedAndExtendedTaskStatuses() {
        Map.ofEntries(Map.entry(6, "AUTO_CLEANING_PAUSED"), Map.entry(8, "ROOM_CLEANING_PAUSED"),
                Map.entry(11, "DOCKING_PAUSED"), Map.entry(18, "ZONE_DOCKING_PAUSED"), Map.entry(20, "CRUISING_PATH"),
                Map.entry(27, "STATION_CLEANING"), Map.entry(30, "PET_FINDING"),
                Map.entry(31, "AUTO_CLEANING_WASHING_PAUSED"), Map.entry(34, "PICKING_UP_ITEM"),
                Map.entry(38, "REMOTE_PICKUP_IDENTIFYING"), Map.entry(41, "REMOTE_PICKUP_IN_PROGRESS"),
                Map.entry(44, "PLACING_ITEM_PAUSED"))
                .forEach((code, name) -> assertEquals(new StringType(name),
                        DreameVacuumStatus.channelUpdates(Map.of("4/7", code)).get("task-status")));
    }

    @Test
    void mapsCompletedChargingAndDryMopReturn() {
        assertEquals(new StringType("CHARGING_COMPLETED"),
                DreameVacuumStatus.channelUpdates(Map.of("3/2", 3)).get("charging-status"));
        assertEquals(new StringType("RETURNING_FOR_DRY_MOP"),
                DreameVacuumStatus.channelUpdates(Map.of("4/25", 7)).get("base-status"));
    }

    @Test
    void mapsSegmentCleaningOperatingStatusToRoomCleaning() {
        assertEquals(new StringType("ROOM_CLEANING"),
                DreameVacuumStatus.channelUpdates(Map.of("4/1", 18)).get("operating-status"));
    }

    @Test
    void distinguishesDryingStateFromSleepingOperatingStatus() {
        var updates = DreameVacuumStatus.channelUpdates(Map.of("2/1", 8, "4/1", 14));
        assertEquals(new StringType("DRYING"), updates.get("state"));
        assertEquals(new StringType("SLEEPING"), updates.get("operating-status"));
        assertEquals(new StringType("UPGRADING"), DreameVacuumStatus.channelUpdates(Map.of("2/1", 14)).get("state"));
        assertEquals(new StringType("FCT"),
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
        });
    }

    @Test
    void mapsCleaningProgressModesAndBaseStatus() {
        var updates = DreameVacuumStatus
                .channelUpdates(Map.of("4/2", 37, "4/3", 82, "4/4", 3, "4/5", 2, "4/7", 3, "4/23", 2, "4/25", 1));
        assertEquals(new QuantityType<>(37, Units.MINUTE), updates.get("cleaning-time"));
        assertEquals(new QuantityType<>(82, SIUnits.SQUARE_METRE), updates.get("cleaned-area"));
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
        assertEquals(new QuantityType<>(240, Units.HOUR), updates.get("main-brush-time-left"));
        assertEquals(new QuantityType<>(80, Units.PERCENT), updates.get("main-brush-left"));
        assertEquals(new QuantityType<>(120, Units.HOUR), updates.get("side-brush-time-left"));
        assertEquals(new QuantityType<>(70, Units.PERCENT), updates.get("side-brush-left"));
        assertEquals(new QuantityType<>(60, Units.PERCENT), updates.get("filter-left"));
        assertEquals(new QuantityType<>(180, Units.HOUR), updates.get("filter-time-left"));
        assertEquals(new QuantityType<>(50, Units.PERCENT), updates.get("sensor-dirty-left"));
        assertEquals(new QuantityType<>(30, Units.HOUR), updates.get("sensor-dirty-time-left"));
        assertEquals(new QuantityType<>(40, Units.PERCENT), updates.get("mop-pad-left"));
        assertEquals(new QuantityType<>(90, Units.HOUR), updates.get("mop-pad-time-left"));
        assertEquals(new QuantityType<>(30, Units.PERCENT), updates.get("detergent-left"));
        assertEquals(new QuantityType<>(20, Units.DAY), updates.get("detergent-time-left"));
        assertEquals(new QuantityType<>(1234, Units.MINUTE), updates.get("total-cleaning-time"));
        assertEquals(new DecimalType(42), updates.get("cleaning-count"));
        assertEquals(new QuantityType<>(5678, SIUnits.SQUARE_METRE), updates.get("total-cleaned-area"));
        assertEquals(15, updates.size());
    }
}
