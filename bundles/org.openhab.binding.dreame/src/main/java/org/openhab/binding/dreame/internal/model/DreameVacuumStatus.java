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
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Maps the L50 Ultra Pro's observed properties independently of mower status codes.
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
                case "4/2" -> updates.put("cleaning-time", new DecimalType(value));
                case "4/3" -> updates.put("cleaned-area", new DecimalType(value));
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
                    default -> unknown(value);
                });
                case "4/40" -> updates.put("drying-time", switch (value) {
                    case 2 -> new StringType("2H");
                    case 3 -> new StringType("3H");
                    case 4 -> new StringType("4H");
                    default -> unknown(value);
                });
                case "9/1" -> updates.put("main-brush-time-left", new DecimalType(value));
                case "9/2" -> updates.put("main-brush-left", new DecimalType(value));
                case "10/1" -> updates.put("side-brush-time-left", new DecimalType(value));
                case "10/2" -> updates.put("side-brush-left", new DecimalType(value));
                case "11/1" -> updates.put("filter-left", new DecimalType(value));
                case "11/2" -> updates.put("filter-time-left", new DecimalType(value));
                case "12/2" -> updates.put("total-cleaning-time", new DecimalType(value));
                case "12/3" -> updates.put("cleaning-count", new DecimalType(value));
                case "12/4" -> updates.put("total-cleaned-area", new DecimalType(value));
                case "16/1" -> updates.put("sensor-dirty-left", new DecimalType(value));
                case "16/2" -> updates.put("sensor-dirty-time-left", new DecimalType(value));
                case "18/1" -> updates.put("mop-pad-left", new DecimalType(value));
                case "18/2" -> updates.put("mop-pad-time-left", new DecimalType(value));
                case "20/1" -> updates.put("detergent-left", new DecimalType(value));
                case "20/2" -> updates.put("detergent-time-left", new DecimalType(value));
                case "2/1" -> updates.put("state", new StringType(switch (value) {
                    case 1 -> "CLEANING";
                    case 3 -> "PAUSED";
                    case 5 -> "RETURNING";
                    case 6 -> "CHARGING";
                    case 8 -> "DRYING";
                    case 9 -> "WASHING";
                    case 12 -> "SWEEPING_AND_MOPPING";
                    case 13 -> "CHARGING_COMPLETED";
                    case 20 -> "CLEAN_ADD_WATER";
                    case 22 -> "AUTO_EMPTYING";
                    default -> "UNKNOWN_" + value;
                }));
                case "4/1" -> updates.put("operating-status", new StringType(switch (value) {
                    case 2 -> "CLEANING";
                    case 1 -> "PAUSED";
                    case 3 -> "RETURNING";
                    case 6 -> "CHARGING";
                    case 14 -> "SLEEPING";
                    default -> "UNKNOWN_" + value;
                }));
                case "3/2" -> updates.put("charging-status", new StringType(switch (value) {
                    case 2 -> "NOT_CHARGING";
                    case 5 -> "RETURNING";
                    case 1 -> "CHARGING";
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
