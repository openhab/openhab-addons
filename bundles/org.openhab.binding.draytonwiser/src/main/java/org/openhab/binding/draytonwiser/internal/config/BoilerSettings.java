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

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class BoilerSettings {

    @SerializedName("ControlType")
    @Expose
    private String controlType;
    @SerializedName("FuelType")
    @Expose
    private String fuelType;
    @SerializedName("CycleRate")
    @Expose
    private String cycleRate;

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getCycleRate() {
        return cycleRate;
    }

    public void setCycleRate(String cycleRate) {
        this.cycleRate = cycleRate;
    }

}
