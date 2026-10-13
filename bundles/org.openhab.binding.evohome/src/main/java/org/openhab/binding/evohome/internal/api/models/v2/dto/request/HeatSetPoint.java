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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for the mode
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Temporary overrides and capability-based temperatures
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
        heatSetpointValue = setPoint;
        setpointMode = "PermanentOverride";
        timeUntil = null;
    }

    HeatSetPoint(double setPoint, Instant until) {
        heatSetpointValue = setPoint;
        setpointMode = "TemporaryOverride";
        timeUntil = until.truncatedTo(ChronoUnit.SECONDS).toString();
    }

    @SuppressWarnings("unused")
    @SerializedName("heatSetpointValue")
    private double heatSetpointValue;

    @SuppressWarnings("unused")
    @SerializedName("setpointMode")
    private String setpointMode;

    @SuppressWarnings("unused")
    @SerializedName("timeUntil")
    private String timeUntil;
}
