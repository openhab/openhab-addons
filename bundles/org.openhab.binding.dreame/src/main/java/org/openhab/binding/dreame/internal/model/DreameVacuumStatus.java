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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Maps common Dreame vacuum properties independently of mower status codes.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameVacuumStatus {
    public static final Set<String> CHANNELS = Set.of("battery-level", "state", "operating-status", "charging-status",
            "error-code", "cleaning-time", "cleaned-area", "suction-level", "water-volume", "task-status",
            "cleaning-mode", "base-status", "drying-time", "clean-genius", "cleaning-route", "main-brush-left",
            "main-brush-time-left", "side-brush-left", "side-brush-time-left", "filter-left", "filter-time-left",
            "sensor-dirty-left", "sensor-dirty-time-left", "mop-pad-left", "mop-pad-time-left", "detergent-left",
            "detergent-time-left", "total-cleaning-time", "cleaning-count", "total-cleaned-area");

    private DreameVacuumStatus() {
    }

    public static Map<String, State> channelUpdates(Map<String, Integer> properties) {
        Map<String, State> updates = new LinkedHashMap<>();
        properties.forEach((address, value) -> {
            switch (address) {
                case "3/1" -> updates.put("battery-level", new DecimalType(value));
                case "2/2" -> updates.put("error-code", new DecimalType(value));
                case "4/2" -> updates.put("cleaning-time", new QuantityType<>(value, Units.MINUTE));
                case "4/3" -> updates.put("cleaned-area", new QuantityType<>(value, SIUnits.SQUARE_METRE));
                case "4/4" -> updates.put("suction-level", named(value, "QUIET", "STANDARD", "STRONG", "TURBO"));
                case "4/5" -> updates.put("water-volume", switch (value) {
                    case 1 -> new StringType("LOW");
                    case 2 -> new StringType("MEDIUM");
                    case 3 -> new StringType("HIGH");
                    default -> unknown(value);
                });
                case "4/7" -> updates.put("task-status", switch (value) {
                    case 0 -> new StringType("COMPLETED");
                    case 1 -> new StringType("AUTO_CLEANING");
                    case 2 -> new StringType("ZONE_CLEANING");
                    case 3 -> new StringType("ROOM_CLEANING");
                    case 4 -> new StringType("SPOT_CLEANING");
                    case 5 -> new StringType("FAST_MAPPING");
                    case 6 -> new StringType("AUTO_CLEANING_PAUSED");
                    case 7 -> new StringType("ZONE_CLEANING_PAUSED");
                    case 8 -> new StringType("ROOM_CLEANING_PAUSED");
                    case 9 -> new StringType("SPOT_CLEANING_PAUSED");
                    case 10 -> new StringType("MAP_CLEANING_PAUSED");
                    case 11 -> new StringType("DOCKING_PAUSED");
                    case 12 -> new StringType("MOPPING_PAUSED");
                    case 13 -> new StringType("ROOM_MOPPING_PAUSED");
                    case 14 -> new StringType("ZONE_MOPPING_PAUSED");
                    case 15 -> new StringType("AUTO_MOPPING_PAUSED");
                    case 16 -> new StringType("AUTO_DOCKING_PAUSED");
                    case 17 -> new StringType("ROOM_DOCKING_PAUSED");
                    case 18 -> new StringType("ZONE_DOCKING_PAUSED");
                    case 20 -> new StringType("CRUISING_PATH");
                    case 21 -> new StringType("CRUISING_PATH_PAUSED");
                    case 22 -> new StringType("CRUISING_POINT");
                    case 23 -> new StringType("CRUISING_POINT_PAUSED");
                    case 24 -> new StringType("SUMMON_CLEAN_PAUSED");
                    case 25 -> new StringType("RETURNING_INSTALL_MOP");
                    case 26 -> new StringType("RETURNING_REMOVE_MOP");
                    case 27 -> new StringType("STATION_CLEANING");
                    case 30 -> new StringType("PET_FINDING");
                    case 31 -> new StringType("AUTO_CLEANING_WASHING_PAUSED");
                    case 32 -> new StringType("AREA_CLEANING_WASHING_PAUSED");
                    case 33 -> new StringType("CUSTOM_CLEANING_WASHING_PAUSED");
                    case 34 -> new StringType("PICKING_UP_ITEM");
                    case 35 -> new StringType("PICKING_UP_ITEM_PAUSED");
                    case 36 -> new StringType("PICKING_UP_ITEM_SUCCESS");
                    case 37 -> new StringType("REMOTE_PICKUP_INITIALIZING");
                    case 38 -> new StringType("REMOTE_PICKUP_IDENTIFYING");
                    case 39 -> new StringType("MANUAL_REMOTE_PICKUP");
                    case 40 -> new StringType("AUTOMATIC_REMOTE_PICKUP");
                    case 41 -> new StringType("REMOTE_PICKUP_IN_PROGRESS");
                    case 42 -> new StringType("REMOTE_PICKUP_PAUSED");
                    case 43 -> new StringType("PLACING_ITEM");
                    case 44 -> new StringType("PLACING_ITEM_PAUSED");
                    default -> unknown(value);
                });
                case "4/23" -> updates.put("cleaning-mode", switch (value & 0x03) {
                    case 0 -> new StringType("SWEEPING_AND_MOPPING");
                    case 1 -> new StringType("MOPPING");
                    case 2 -> new StringType("SWEEPING");
                    default -> unknown(value);
                });
                case "4/25" -> updates.put("base-status", switch (value) {
                    case 0 -> new StringType("IDLE");
                    case 1 -> new StringType("WASHING");
                    case 2 -> new StringType("DRYING");
                    case 3 -> new StringType("RETURNING");
                    case 4 -> new StringType("PAUSED");
                    case 5 -> new StringType("CLEAN_ADD_WATER");
                    case 6 -> new StringType("ADDING_WATER");
                    case 7 -> new StringType("RETURNING_FOR_DRY_MOP");
                    default -> unknown(value);
                });
                case "4/40" -> updates.put("drying-time", switch (value) {
                    case 2 -> new StringType("2H");
                    case 3 -> new StringType("3H");
                    case 4 -> new StringType("4H");
                    default -> unknown(value);
                });
                case "9/1" -> updates.put("main-brush-time-left", new QuantityType<>(value, Units.HOUR));
                case "9/2" -> updates.put("main-brush-left", new QuantityType<>(value, Units.PERCENT));
                case "10/1" -> updates.put("side-brush-time-left", new QuantityType<>(value, Units.HOUR));
                case "10/2" -> updates.put("side-brush-left", new QuantityType<>(value, Units.PERCENT));
                case "11/1" -> updates.put("filter-left", new QuantityType<>(value, Units.PERCENT));
                case "11/2" -> updates.put("filter-time-left", new QuantityType<>(value, Units.HOUR));
                case "12/2" -> updates.put("total-cleaning-time", new QuantityType<>(value, Units.MINUTE));
                case "12/3" -> updates.put("cleaning-count", new DecimalType(value));
                case "12/4" -> updates.put("total-cleaned-area", new QuantityType<>(value, SIUnits.SQUARE_METRE));
                case "16/1" -> updates.put("sensor-dirty-left", new QuantityType<>(value, Units.PERCENT));
                case "16/2" -> updates.put("sensor-dirty-time-left", new QuantityType<>(value, Units.HOUR));
                case "18/1" -> updates.put("mop-pad-left", new QuantityType<>(value, Units.PERCENT));
                case "18/2" -> updates.put("mop-pad-time-left", new QuantityType<>(value, Units.HOUR));
                case "20/1" -> updates.put("detergent-left", new QuantityType<>(value, Units.PERCENT));
                case "20/2" -> updates.put("detergent-time-left", new QuantityType<>(value, Units.DAY));
                case "2/1" -> updates.put("state", new StringType(switch (value) {
                    case 1 -> "CLEANING";
                    case 2 -> "IDLE";
                    case 3 -> "PAUSED";
                    case 4 -> "ERROR";
                    case 5 -> "RETURNING";
                    case 6 -> "CHARGING";
                    case 7 -> "MOPPING";
                    case 8 -> "DRYING";
                    case 9 -> "WASHING";
                    case 10 -> "RETURNING_TO_WASHING";
                    case 11 -> "BUILDING";
                    case 12 -> "SWEEPING_AND_MOPPING";
                    case 13 -> "CHARGING_COMPLETED";
                    case 14 -> "UPGRADING";
                    case 15 -> "CLEAN_SUMMON";
                    case 16 -> "STATION_RESET";
                    case 17 -> "RETURNING_INSTALL_MOP";
                    case 18 -> "RETURNING_REMOVE_MOP";
                    case 20 -> "CLEAN_ADD_WATER";
                    case 21 -> "WASHING_PAUSED";
                    case 22 -> "AUTO_EMPTYING";
                    default -> "UNKNOWN_" + value;
                }));
                case "4/1" -> updates.put("operating-status", new StringType(switch (value) {
                    case 0 -> "IDLE";
                    case 1 -> "PAUSED";
                    case 2 -> "CLEANING";
                    case 3 -> "RETURNING";
                    case 4 -> "PARTIAL_CLEANING";
                    case 5 -> "FOLLOW_WALL";
                    case 6 -> "CHARGING";
                    case 7 -> "OTA";
                    case 8 -> "FCT";
                    case 9 -> "WIFI_SETUP";
                    case 10 -> "POWER_OFF";
                    case 11 -> "FACTORY";
                    case 12 -> "ERROR";
                    case 13 -> "REMOTE_CONTROL";
                    case 14 -> "SLEEPING";
                    case 15 -> "SELF_REPAIR";
                    case 16 -> "FACTORY_FUNCTION_TEST";
                    case 17 -> "STANDBY";
                    case 18 -> "ROOM_CLEANING";
                    case 19 -> "ZONE_CLEANING";
                    case 20 -> "SPOT_CLEANING";
                    case 21 -> "FAST_MAPPING";
                    case 22 -> "CRUISING_PATH";
                    case 23 -> "CRUISING_POINT";
                    case 24 -> "SUMMON_CLEAN";
                    case 25 -> "SHORTCUT";
                    case 26 -> "PERSON_FOLLOW";
                    case 27 -> "PET_GUARDING";
                    case 28 -> "AUTO_ARRANGEMENT";
                    case 29 -> "SMART_ARRANGEMENT";
                    case 30 -> "ZONED_ARRANGEMENT";
                    case 1501 -> "WATER_CHECK";
                    default -> "UNKNOWN_" + value;
                }));
                case "3/2" -> updates.put("charging-status", new StringType(switch (value) {
                    case 1 -> "CHARGING";
                    case 2 -> "NOT_CHARGING";
                    case 3 -> "CHARGING_COMPLETED";
                    case 5 -> "RETURNING";
                    default -> "UNKNOWN_" + value;
                }));
                default -> {
                    // Unconfirmed properties, including task status, remain diagnostic only.
                }
            }
        });
        return updates;
    }

    public static Map<String, State> channelUpdates(DreameVacuumProperties properties) {
        Map<String, State> updates = new LinkedHashMap<>(channelUpdates(properties.numeric()));
        String autoSwitch = properties.text().get("4/50");
        if (autoSwitch == null) {
            return updates;
        }
        try {
            JsonElement root = JsonParser.parseString(autoSwitch);
            JsonArray entries = root.isJsonArray() ? root.getAsJsonArray() : singleEntry(root);
            for (JsonElement entry : entries) {
                if (!entry.isJsonObject()) {
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                JsonElement key = object.get("k");
                JsonElement value = object.get("v");
                if (key == null || value == null || !key.isJsonPrimitive() || !value.isJsonPrimitive()) {
                    continue;
                }
                switch (key.getAsString()) {
                    case "SmartHost" -> updates.put("clean-genius", switch (value.getAsInt()) {
                        case 0 -> new StringType("OFF");
                        case 1 -> new StringType("ROUTINE");
                        case 2 -> new StringType("DEEP");
                        default -> unknown(value.getAsInt());
                    });
                    case "CleanRoute" -> updates.put("cleaning-route", switch (value.getAsInt()) {
                        case 1 -> new StringType("STANDARD");
                        case 4 -> new StringType("QUICK");
                        default -> unknown(value.getAsInt());
                    });
                    default -> {
                    }
                }
            }
        } catch (RuntimeException e) {
            // Optional firmware-specific settings must not discard the remaining status update.
        }
        return updates;
    }

    private static JsonArray singleEntry(JsonElement element) {
        JsonArray result = new JsonArray();
        result.add(element);
        return result;
    }

    private static StringType named(int value, String... names) {
        return value >= 0 && value < names.length ? new StringType(names[value]) : unknown(value);
    }

    private static StringType unknown(int value) {
        return new StringType("UNKNOWN_" + value);
    }
}
