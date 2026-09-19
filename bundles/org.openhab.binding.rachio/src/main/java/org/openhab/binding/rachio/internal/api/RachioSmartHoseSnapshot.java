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
package org.openhab.binding.rachio.internal.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Consistent view of the Smart Hose Timer resources returned by one successful cloud refresh.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public record RachioSmartHoseSnapshot(Map<String, RachioBaseStation> baseStations, Map<String, RachioValve> valves,
        Map<String, RachioValveProgram> programs, Instant retrievedAt) {

    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    public static final RachioSmartHoseSnapshot EMPTY = new RachioSmartHoseSnapshot(Map.of(), Map.of(), Map.of(),
            Instant.EPOCH);

    public RachioSmartHoseSnapshot {
        baseStations = Map.copyOf(baseStations);
        valves = Map.copyOf(valves);
        programs = Map.copyOf(programs);
    }

    public boolean hasSameContent(RachioSmartHoseSnapshot other) {
        return mapsEqual(baseStations, other.baseStations) && mapsEqual(valves, other.valves)
                && mapsEqual(programs, other.programs);
    }

    private static boolean mapsEqual(Map<String, ?> first, Map<String, ?> second) {
        if (!first.keySet().equals(second.keySet())) {
            return false;
        }
        return first.entrySet().stream()
                .allMatch(entry -> GSON.toJson(entry.getValue()).equals(GSON.toJson(second.get(entry.getKey()))));
    }
}
