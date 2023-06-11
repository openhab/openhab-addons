/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for the mode
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class HeatSetPoint {

    /**
     * Constructs an override reset
     */
    HeatSetPoint() {
        heatSetpointValue = 0.0;
        setpointMode = "FollowSchedule";
        timeUntil = null;
    }

    /**
     * Constructs a permanent override with the given temperature
     *
     * @param setPoint The target temperature to set the set point to
     */
    HeatSetPoint(double setPoint) {
        // Make sure that the value is rounded toward the nearest 0.5
        heatSetpointValue = Math.round(setPoint * 2) / 2.0;
        setpointMode = "PermanentOverride";
        timeUntil = null;
    }

    @SerializedName("heatSetpointValue")
    private double heatSetpointValue;

    @SerializedName("setpointMode")
    private String setpointMode;

    @SerializedName("timeUntil")
    private String timeUntil;
}
