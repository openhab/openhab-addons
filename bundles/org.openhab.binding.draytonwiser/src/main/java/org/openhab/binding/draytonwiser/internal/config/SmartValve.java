
package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SmartValve {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("SetPoint")
    @Expose
    private Integer setPoint;
    @SerializedName("MeasuredTemperature")
    @Expose
    private Integer measuredTemperature;
    @SerializedName("MeasuredDemand")
    @Expose
    private Integer measuredDemand;
    @SerializedName("WindowState")
    @Expose
    private String windowState;
    @SerializedName("ExternalRoomStatTemperature")
    @Expose
    private Integer externalRoomStatTemperature;
    @SerializedName("MountingOrientation")
    @Expose
    private String mountingOrientation;

    public Integer getId() {
        return id;
    }

    public Integer getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(Integer setPoint) {
        this.setPoint = setPoint;
    }

    public Integer getMeasuredTemperature() {
        return measuredTemperature;
    }

    public Integer getMeasuredDemand() {
        return measuredDemand;
    }

    public String getWindowState() {
        return windowState;
    }

    public Integer getExternalRoomStatTemperature() {
        return externalRoomStatTemperature;
    }

    public String getMountingOrientation() {
        return mountingOrientation;
    }

}