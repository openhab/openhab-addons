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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Strategy for reshaping raw evcc JSON into the flat, channel-ready structure expected by the handlers.
 *
 * Transformation is the second stage of the routing pipeline after extraction has located the relevant data:
 * incoming message → extraction → transformation → handler dispatch. Keeping the reshaping here allows the same
 * normalization to be reused by both the full-state initialization path and the partial websocket update path.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public interface StateTransformer {

    /**
     * Reshape the given source object into the normalized channel-ready structure.
     *
     * @param source The raw JSON object to transform
     * @return A normalized JSON object whose members map directly to channel keys
     */
    JsonObject transform(JsonObject source);

    /**
     * Expand a phase array such as {@code [L1, L2, L3]} into individual, indexed members
     * (e.g. {@code gridCurrentL1}, {@code gridCurrentL2}, {@code gridCurrentL3}).
     *
     * @param target The object to add the expanded members to
     * @param values The phase array
     * @param prefix The channel prefix (e.g. "grid", "charge")
     * @param datapoint The measurement type (e.g. "Current", "Voltage", "Power")
     */
    static void expandPhases(JsonObject target, JsonArray values, String prefix, String datapoint) {
        int phase = 1;
        for (JsonElement value : values) {
            target.add(prefix + datapoint + "L" + phase, value);
            phase++;
        }
    }
}
