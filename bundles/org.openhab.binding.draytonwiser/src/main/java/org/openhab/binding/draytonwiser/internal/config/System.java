
package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class System {

    @SerializedName("PairingStatus")
    @Expose
    private String pairingStatus;
    @SerializedName("TimeZoneOffset")
    @Expose
    private Integer timeZoneOffset;
    @SerializedName("AutomaticDaylightSaving")
    @Expose
    private Boolean automaticDaylightSaving;
    @SerializedName("Version")
    @Expose
    private Integer version;
    @SerializedName("FotaEnabled")
    @Expose
    private Boolean fotaEnabled;
    @SerializedName("ValveProtectionEnabled")
    @Expose
    private Boolean valveProtectionEnabled;
    @SerializedName("EcoModeEnabled")
    @Expose
    private Boolean ecoModeEnabled;
    @SerializedName("BoilerSettings")
    @Expose
    private BoilerSettings boilerSettings;
    @SerializedName("UnixTime")
    @Expose
    private Integer unixTime;
    @SerializedName("CloudConnectionStatus")
    @Expose
    private String cloudConnectionStatus;
    @SerializedName("ZigbeeModuleVersion")
    @Expose
    private String zigbeeModuleVersion;
    @SerializedName("ZigbeeEui")
    @Expose
    private String zigbeeEui;
    @SerializedName("LocalDateAndTime")
    @Expose
    private LocalDateAndTime localDateAndTime;
    @SerializedName("HeatingButtonOverrideState")
    @Expose
    private String heatingButtonOverrideState;
    @SerializedName("HotWaterButtonOverrideState")
    @Expose
    private String hotWaterButtonOverrideState;

    public String getPairingStatus() {
        return pairingStatus;
    }

    public void setPairingStatus(String pairingStatus) {
        this.pairingStatus = pairingStatus;
    }

    public Integer getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(Integer timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public Boolean getAutomaticDaylightSaving() {
        return automaticDaylightSaving;
    }

    public void setAutomaticDaylightSaving(Boolean automaticDaylightSaving) {
        this.automaticDaylightSaving = automaticDaylightSaving;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getFotaEnabled() {
        return fotaEnabled;
    }

    public void setFotaEnabled(Boolean fotaEnabled) {
        this.fotaEnabled = fotaEnabled;
    }

    public Boolean getValveProtectionEnabled() {
        return valveProtectionEnabled;
    }

    public void setValveProtectionEnabled(Boolean valveProtectionEnabled) {
        this.valveProtectionEnabled = valveProtectionEnabled;
    }

    public Boolean getEcoModeEnabled() {
        return ecoModeEnabled;
    }

    public void setEcoModeEnabled(Boolean ecoModeEnabled) {
        this.ecoModeEnabled = ecoModeEnabled;
    }

    public BoilerSettings getBoilerSettings() {
        return boilerSettings;
    }

    public void setBoilerSettings(BoilerSettings boilerSettings) {
        this.boilerSettings = boilerSettings;
    }

    public Integer getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(Integer unixTime) {
        this.unixTime = unixTime;
    }

    public String getCloudConnectionStatus() {
        return cloudConnectionStatus;
    }

    public void setCloudConnectionStatus(String cloudConnectionStatus) {
        this.cloudConnectionStatus = cloudConnectionStatus;
    }

    public String getZigbeeModuleVersion() {
        return zigbeeModuleVersion;
    }

    public void setZigbeeModuleVersion(String zigbeeModuleVersion) {
        this.zigbeeModuleVersion = zigbeeModuleVersion;
    }

    public String getZigbeeEui() {
        return zigbeeEui;
    }

    public void setZigbeeEui(String zigbeeEui) {
        this.zigbeeEui = zigbeeEui;
    }

    public LocalDateAndTime getLocalDateAndTime() {
        return localDateAndTime;
    }

    public void setLocalDateAndTime(LocalDateAndTime localDateAndTime) {
        this.localDateAndTime = localDateAndTime;
    }

    public String getHeatingButtonOverrideState() {
        return heatingButtonOverrideState;
    }

    public void setHeatingButtonOverrideState(String heatingButtonOverrideState) {
        this.heatingButtonOverrideState = heatingButtonOverrideState;
    }

    public String getHotWaterButtonOverrideState() {
        return hotWaterButtonOverrideState;
    }

    public void setHotWaterButtonOverrideState(String hotWaterButtonOverrideState) {
        this.hotWaterButtonOverrideState = hotWaterButtonOverrideState;
    }

}
