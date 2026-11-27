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

import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_ACTIVE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_PLAN;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_REPEATING_PLANS;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_VEHICLES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Extracts the canonical plan payload from either full websocket payloads or already nested plan objects.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class PlanExtraction implements ExtractionStrategy {

    private final String vehicleId;
    private final int index;

    public PlanExtraction(String vehicleId, int index) {
        this.vehicleId = vehicleId;
        this.index = index;
    }

    @Override
    public @Nullable JsonElement extract(JsonElement source) {
        if (index == 0 && source.isJsonArray()) {
            JsonObject plan = new JsonObject();
            plan.addProperty(JSON_KEY_ACTIVE, source.getAsJsonArray().size() > 0);
            return plan;
        }
        if (!source.isJsonObject()) {
            return null;
        }

        JsonObject sourceObject = source.getAsJsonObject();
        if (index == 0 && sourceObject.has(JSON_KEY_ACTIVE)) {
            return sourceObject;
        }
        if (index > 0 && sourceObject.has(JSON_KEY_REPEATING_PLANS)) {
            JsonElement repeatingPlans = sourceObject.get(JSON_KEY_REPEATING_PLANS);
            return extractRepeatingPlan(repeatingPlans);
        }

        JsonObject vehicles = extractObject(sourceObject, JSON_KEY_VEHICLES);
        if (vehicles == null) {
            return null;
        }

        JsonObject vehicleState = extractObject(vehicles, vehicleId);
        if (vehicleState == null) {
            return null;
        }

        if (index == 0) {
            JsonObject plan = extractObject(vehicleState, JSON_KEY_PLAN);
            if (plan != null) {
                plan = plan.deepCopy();
                plan.addProperty(JSON_KEY_ACTIVE, true);
            }
            return plan;
        }

        return extractRepeatingPlan(vehicleState.get(JSON_KEY_REPEATING_PLANS));
    }

    private @Nullable JsonObject extractObject(JsonObject map, String key) {
        return map.has(key) && map.get(key).isJsonObject() ? map.getAsJsonObject(key) : null;
    }

    private @Nullable JsonObject extractRepeatingPlan(@Nullable JsonElement repeatingPlansElement) {
        if (repeatingPlansElement == null || !repeatingPlansElement.isJsonArray()) {
            return null;
        }
        JsonArray repeatingPlans = repeatingPlansElement.getAsJsonArray();
        int repeatingIndex = index - 1;
        if (repeatingIndex < 0 || repeatingIndex >= repeatingPlans.size()
                || !repeatingPlans.get(repeatingIndex).isJsonObject()) {
            return null;
        }
        return repeatingPlans.get(repeatingIndex).getAsJsonObject();
    }

    @Override
    public String describe() {
        return "Plan{" + vehicleId + "," + index + "}";
    }
}
