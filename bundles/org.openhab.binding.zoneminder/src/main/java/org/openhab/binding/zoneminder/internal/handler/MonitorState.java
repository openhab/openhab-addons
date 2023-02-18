/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MonitorState} represents the possible states of a Zoneminder monitor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum MonitorState {

    @SerializedName("0")
    IDLE("IDLE"),

    @SerializedName("1")
    PREALERT("PREALERT"),

    @SerializedName("2")
    ALARM("ALARM"),

    @SerializedName("3")
    ALERT("ALERT"),

    @SerializedName("4")
    TAPE("TAPE"),

    UNKNOWN("UNKNOWN");

    private final String type;

    private MonitorState(String type) {
        this.type = type;
    }

    public static MonitorState forValue(@Nullable String v) {
        if (v != null) {
            for (MonitorState at : MonitorState.values()) {
                if (at.type.equals(v)) {
                    return at;
                }
            }
        }
        throw new IllegalArgumentException(String.format("Invalid or null monitor state: %s" + v));
    }

    @Override
    public String toString() {
        return this.type;
    }
}
