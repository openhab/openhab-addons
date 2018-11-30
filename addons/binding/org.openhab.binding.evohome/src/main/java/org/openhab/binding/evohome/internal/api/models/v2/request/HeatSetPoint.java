/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
