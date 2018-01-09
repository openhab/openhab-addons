
package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetectedAccessPoint {

    @SerializedName("SSID")
    @Expose
    private String sSID;
    @SerializedName("Channel")
    @Expose
    private Integer channel;
    @SerializedName("SecurityMode")
    @Expose
    private String securityMode;
    @SerializedName("RSSI")
    @Expose
    private Integer rSSI;

    public String getSSID() {
        return sSID;
    }

    public void setSSID(String sSID) {
        this.sSID = sSID;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
    }

    public Integer getRSSI() {
        return rSSI;
    }

    public void setRSSI(Integer rSSI) {
        this.rSSI = rSSI;
    }

}
