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
package org.openhab.binding.orbitbhyve.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OrbitBhyveDeviceStatus} holds information about a B-Hyve
 * device status.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveDeviceStatus {
    @SerializedName("run_mode")
    String mode = "";

    @SerializedName("next_start_time")
    String nextStartTime = "";

    @SerializedName("rain_delay")
    int delay = 0;

    @SerializedName("rain_delay_started_at")
    String rainDelayStartedAt = "";

    public String getMode() {
        return mode;
    }

    public String getNextStartTime() {
        return nextStartTime;
    }

    public int getDelay() {
        return delay;
    }
}
