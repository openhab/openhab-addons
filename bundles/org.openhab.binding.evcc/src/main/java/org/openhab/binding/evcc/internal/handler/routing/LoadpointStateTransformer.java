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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Normalizes a single evcc loadpoint object into the flat channel structure used by the loadpoint handler.
 *
 * A few API fields are renamed to more descriptive channel keys and the {@code chargeCurrents}/{@code chargeVoltages}
 * phase arrays are expanded into indexed members (e.g. {@code chargeCurrentL1}). All other members are preserved.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LoadpointStateTransformer implements StateTransformer {

    private static final String PREFIX = "charge";

    // Maps API field names to the normalized channel keys used by the handler.
    private static final Map<String, String> RENAMED_KEYS = Map.of(JSON_KEY_CHARGE_CURRENT, JSON_KEY_OFFERED_CURRENT,
            JSON_KEY_VEHICLE_PRESENT, JSON_KEY_CONNECTED, JSON_KEY_PHASES, JSON_KEY_PHASES_CONFIGURED);

    @Override
    public JsonObject transform(JsonObject source) {
        JsonObject result = source.deepCopy();
        RENAMED_KEYS.forEach((oldKey, newKey) -> {
            if (result.has(oldKey)) {
                result.add(newKey, result.get(oldKey));
                result.remove(oldKey);
            }
        });
        if (result.has(JSON_KEY_CHARGE_CURRENTS)) {
            StateTransformer.expandPhases(result, result.getAsJsonArray(JSON_KEY_CHARGE_CURRENTS), PREFIX, "Current");
            result.remove(JSON_KEY_CHARGE_CURRENTS);
        }
        if (result.has(JSON_KEY_CHARGE_VOLTAGES)) {
            StateTransformer.expandPhases(result, result.getAsJsonArray(JSON_KEY_CHARGE_VOLTAGES), PREFIX, "Voltage");
            result.remove(JSON_KEY_CHARGE_VOLTAGES);
        }
        return result;
    }
}
