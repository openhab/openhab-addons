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
package org.openhab.binding.veluxactive.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Supported module types and their VELUX API codes.
 * Unknown codes deserialize to null, allowing discovery to skip unsupported modules.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum ModuleType {
    @SerializedName("NXG")
    GATEWAY("VELUX Gateway"),
    @SerializedName("NXS")
    INDOOR_SENSOR("VELUX Indoor Climate Sensor"),
    @SerializedName("NXD")
    DEPARTURE_SWITCH("VELUX Departure Switch"),
    @SerializedName("NXO")
    OPENING("VELUX Window/Cover");

    private final String defaultName;

    ModuleType(String defaultName) {
        this.defaultName = defaultName;
    }

    public String defaultName() {
        return defaultName;
    }
}
