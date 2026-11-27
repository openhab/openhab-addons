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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.handler.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Flattens the evcc {@code grid} object into the individual grid channels.
 *
 * The phase arrays {@code currents}, {@code voltages} and {@code powers} are expanded into indexed members
 * (e.g. {@code gridCurrentL1}), while scalar members are prefixed with {@code grid} (e.g. {@code power} becomes
 * {@code gridPower}). The result contains only the flattened grid members and can be applied by both the
 * initialization and the partial update path.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class GridStateTransformer implements StateTransformer {

    private static final String PREFIX = "grid";

    @Override
    public JsonObject transform(JsonObject source) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            switch (entry.getKey()) {
                case "currents" ->
                    StateTransformer.expandPhases(result, entry.getValue().getAsJsonArray(), PREFIX, "Current");
                case "voltages" ->
                    StateTransformer.expandPhases(result, entry.getValue().getAsJsonArray(), PREFIX, "Voltage");
                case "powers" ->
                    StateTransformer.expandPhases(result, entry.getValue().getAsJsonArray(), PREFIX, "Power");
                default -> result.add(PREFIX + Utils.capitalizeFirstLetter(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }
}
