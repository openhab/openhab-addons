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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Last recorded alarm event of a Tapo camera.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public record TapoLastAlarmInfo(String type, long timeEpochSeconds) {

    public static TapoLastAlarmInfo fromJson(JsonObject json) {
        return new TapoLastAlarmInfo(stringOrEmpty(json, "last_alarm_type"),
                json.has("last_alarm_time") ? json.get("last_alarm_time").getAsLong() : 0);
    }

    static String stringOrEmpty(JsonObject json, String member) {
        return json.has(member) && !json.get(member).isJsonNull() ? json.get(member).getAsString() : "";
    }

    static boolean parseOnOff(JsonObject json, String member) {
        return json.has(member) && !json.get(member).isJsonNull() && "on".equals(json.get(member).getAsString());
    }
}
