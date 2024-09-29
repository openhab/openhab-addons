/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VentilatorMode} defines the possible ventilator modes.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum VentilatorMode {

    @SerializedName("auto")
    AUTO("auto"),

    @SerializedName("minontime")
    MIN_ON_TIME("minontime"),

    @SerializedName("on")
    ON("on"),

    @SerializedName("off")
    OFF("off");

    private final String mode;

    private VentilatorMode(String mode) {
        this.mode = mode;
    }

    public String value() {
        return mode;
    }

    public static VentilatorMode forValue(@Nullable String v) {
        if (v != null) {
            for (VentilatorMode vm : VentilatorMode.values()) {
                if (vm.mode.equals(v)) {
                    return vm;
                }
            }
        }
        throw new IllegalArgumentException("Invalid vent: " + v);
    }

    @Override
    public String toString() {
        return this.mode;
    }
}
