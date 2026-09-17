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

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Motion detection settings of a Tapo camera. Fields are nullable because models differ
 * (digital_sensitivity numeric vs low/medium/high strings).
 *
 * @author Kai Kreuzer - Initial contribution
 */
public record TapoMotionDetection(@Nullable Boolean enabled, @Nullable Integer digitalSensitivity,
        @Nullable String sensitivityLevel) {
    public static TapoMotionDetection fromJson(JsonObject json) {
        Boolean enabled = json.has("enabled") && !json.get("enabled").isJsonNull()
                ? "on".equals(json.get("enabled").getAsString())
                : null;
        Integer digital = null;
        if (json.has("digital_sensitivity")) {
            try {
                digital = json.get("digital_sensitivity").getAsInt();
            } catch (NumberFormatException | UnsupportedOperationException e) {
                digital = null; // some models report non-numeric values like "auto"
            }
        }
        String level = TapoLastAlarmInfo.stringOrEmpty(json, "sensitivity");
        return new TapoMotionDetection(enabled, digital, level.isEmpty() ? null : level);
    }
}
