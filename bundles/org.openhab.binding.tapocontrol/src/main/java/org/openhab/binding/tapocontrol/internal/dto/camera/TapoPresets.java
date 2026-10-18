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
package org.openhab.binding.tapocontrol.internal.dto.camera;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * Stored preset positions of a Tapo camera, parsed from parallel id/name arrays.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public record TapoPresets(List<Integer> ids, List<String> names) {
    public static TapoPresets fromJson(JsonObject json) {
        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (json.has("id") && json.get("id").isJsonArray()) {
            json.getAsJsonArray("id").forEach(e -> ids.add(e.getAsInt()));
            if (json.has("name") && json.get("name").isJsonArray()) {
                json.getAsJsonArray("name").forEach(e -> names.add(e.getAsString()));
            }
            while (names.size() < ids.size()) {
                names.add("");
            }
        }
        return new TapoPresets(ids, names);
    }
}
