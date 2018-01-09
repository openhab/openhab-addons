/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
