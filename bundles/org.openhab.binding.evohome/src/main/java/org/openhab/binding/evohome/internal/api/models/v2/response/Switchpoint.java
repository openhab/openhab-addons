/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.time.LocalTime;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the daily schedule
 *
 * @author James Kinsman - Initial Contribution
 *
 */

public class Switchpoint {

    @SerializedName("heatSetpoint")
    private double heatSetpoint;

    @SerializedName("timeOfDay")
    private String timeOfDay;

    public Switchpoint() {
        heatSetpoint = 0.0;
        timeOfDay = "";
    }

    public double getHeatSetpoint() {
        return heatSetpoint;
    }

    public LocalTime getTimeOfDay() {
        return LocalTime.parse(timeOfDay);
    }

}
