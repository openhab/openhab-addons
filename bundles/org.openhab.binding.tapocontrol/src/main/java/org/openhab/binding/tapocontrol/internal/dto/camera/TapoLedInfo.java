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
 * Status LED configuration of a Tapo camera.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public record TapoLedInfo(boolean enabled) {
    public static TapoLedInfo fromJson(JsonObject json) {
        return new TapoLedInfo(TapoLastAlarmInfo.parseOnOff(json, "enabled"));
    }
}
