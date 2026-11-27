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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * Alarm configuration of a Tapo camera.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public record TapoMsgAlarmInfo(boolean enabled, List<String> alarmModes) {
    public static TapoMsgAlarmInfo fromJson(JsonObject json) {
        List<String> modes = new ArrayList<>();
        if (json.has("alarm_mode") && json.get("alarm_mode").isJsonArray()) {
            json.getAsJsonArray("alarm_mode").forEach(e -> modes.add(e.getAsString()));
        }
        return new TapoMsgAlarmInfo(TapoLastAlarmInfo.parseOnOff(json, "enabled"), modes);
    }
}
