
package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
