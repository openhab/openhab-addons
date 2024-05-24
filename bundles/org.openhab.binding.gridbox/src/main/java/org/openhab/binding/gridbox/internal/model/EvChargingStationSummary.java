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
package org.openhab.binding.gridbox.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * {@link EvChargingStationSummary} is a gson-mapped class that will be used to contain summarized information of all EV
 * charging stations/wallboxes in a GridBox API response
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class EvChargingStationSummary {

    @SerializedName("power")
    @Expose
    private long power;

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public String toString() {
        return "EvChargingStationSummary [power=" + power + "]";
    }
}
