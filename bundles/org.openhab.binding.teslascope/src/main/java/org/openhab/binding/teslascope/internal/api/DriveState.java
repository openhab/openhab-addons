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
package org.openhab.binding.teslascope.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class DriveState {
    // drive state
    public int heading;
    public float latitude;
    public float longitude;
    public float power;

    @SerializedName("shift_state")
    public String shiftState = "";

    public float speed = 0;

    @SerializedName("self_driving_miles_since_reset")
    public float selfDrivingMilesSinceReset;

    @SerializedName("miles_since_reset")
    public float milesSinceReset;

    @SerializedName("self_driving_percentage")
    public float selfDrivingPercentage;

    private DriveState() {
    }
}
