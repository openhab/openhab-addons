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
 * The {@link PlugState} defines the possible plug states.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum PlugState {

    /**
     * Sets the plug into the on state for the start/end period specified.
     * Creates a plug hold in the on state.
     */
    @SerializedName("on")
    ON("on"),

    /**
     * Sets the plug into the off state for the start/end period specified.
     * Creates a plug hold in the off state.
     */
    @SerializedName("off")
    OFF("off"),

    /**
     * Causes the plug to resume its regular program and to follow it. Removes
     * the currently active plug hold, if no hold is currently running, nothing
     * happens. No other optional properties are used.
     */
    @SerializedName("resume")
    RESUME("resume");

    private final String state;

    private PlugState(String state) {
        this.state = state;
    }

    public static PlugState forValue(@Nullable String v) {
        if (v != null) {
            for (PlugState ps : PlugState.values()) {
                if (ps.state.equals(v)) {
                    return ps;
                }
            }
        }
        throw new IllegalArgumentException("Invalid plug state: " + v);
    }

    @Override
    public String toString() {
        return this.state;
    }
}
