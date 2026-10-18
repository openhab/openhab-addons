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
package org.openhab.binding.evcc.internal.handler.routing;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Normalizes a heating loadpoint object by mapping the temperature-based API fields onto the SoC channel keys
 * reused from the loadpoint model.
 *
 * A {@link SortedMap} is used so the replacement order is stable, which the unit tests rely on.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class HeatingStateTransformer implements StateTransformer {

    private static final SortedMap<String, String> RENAMED_KEYS = new TreeMap<>(
            Map.ofEntries(Map.entry("effectiveLimitTemperature", JSON_KEY_EFFECTIVE_LIMIT_SOC),
                    Map.entry("effectivePlanTemperature", JSON_KEY_EFFECTIVE_PLAN_SOC),
                    Map.entry("limitTemperature", JSON_KEY_LIMIT_SOC),
                    Map.entry("vehicleLimitTemperature", JSON_KEY_VEHICLE_LIMIT_SOC),
                    Map.entry("vehicleTemperature", JSON_KEY_VEHICLE_SOC)));

    @Override
    public JsonObject transform(JsonObject source) {
        JsonObject result = source.deepCopy();
        RENAMED_KEYS.forEach((newKey, oldKey) -> {
            if (result.has(oldKey)) {
                result.add(newKey, result.get(oldKey));
                result.remove(oldKey);
            }
        });
        return result;
    }

    /**
     * Resolve the original evcc API key for a heating channel key, used to address the correct API endpoint when
     * sending commands.
     *
     * @param channelKey The normalized channel key
     * @return The original API key, or {@code null} when the channel key is not a remapped heating key
     */
    public static @Nullable String toApiKey(String channelKey) {
        return RENAMED_KEYS.get(channelKey);
    }
}
