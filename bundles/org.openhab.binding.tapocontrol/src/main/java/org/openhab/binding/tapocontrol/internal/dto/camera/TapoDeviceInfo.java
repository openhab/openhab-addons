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

import com.google.gson.JsonObject;

/**
 * Basic device information of a Tapo camera.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public record TapoDeviceInfo(String model, String name, String mac, String swVersion) {
    public static TapoDeviceInfo fromJson(JsonObject json) {
        return new TapoDeviceInfo(TapoLastAlarmInfo.stringOrEmpty(json, "device_model"),
                TapoLastAlarmInfo.stringOrEmpty(json, "device_name"), TapoLastAlarmInfo.stringOrEmpty(json, "mac"),
                TapoLastAlarmInfo.stringOrEmpty(json, "sw_version"));
    }
}
