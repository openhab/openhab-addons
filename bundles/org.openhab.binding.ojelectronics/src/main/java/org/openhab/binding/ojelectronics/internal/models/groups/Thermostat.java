
package org.openhab.binding.ojelectronics.internal.models.groups;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Thermostat {

    @SerializedName("Id")
    @Expose
    public Integer id;
    @SerializedName("Action")
    @Expose
    public Integer action;
    @SerializedName("SerialNumber")
    @Expose
    public String serialNumber;
    @SerializedName("GroupName")
    @Expose
    public String groupName;
    @SerializedName("GroupId")
    @Expose
    public Integer groupId;
    @SerializedName("CustomerId")
    @Expose
    public Integer customerId;
    @SerializedName("SWversion")
    @Expose
    public String sWversion;
    @SerializedName("Online")
    @Expose
    public Boolean online;
    @SerializedName("Heating")
    @Expose
    public Boolean heating;
    @SerializedName("RoomTemperature")
    @Expose
    public Integer roomTemperature;
    @SerializedName("FloorTemperature")
    @Expose
    public Integer floorTemperature;
    @SerializedName("RegulationMode")
    @Expose
    public Integer regulationMode;
    @SerializedName("Schedule")
    @Expose
    public Schedule schedule;
    @SerializedName("ComfortSetpoint")
    @Expose
    public Integer comfortSetpoint;
    @SerializedName("ComfortEndTime")
    @Expose
    public String comfortEndTime;
    @SerializedName("ManualModeSetpoint")
    @Expose
    public Integer manualModeSetpoint;
    @SerializedName("VacationEnabled")
    @Expose
    public Boolean vacationEnabled;
    @SerializedName("VacationBeginDay")
    @Expose
    public String vacationBeginDay;
    @SerializedName("VacationEndDay")
    @Expose
    public String vacationEndDay;
    @SerializedName("VacationTemperature")
    @Expose
    public Integer vacationTemperature;
    @SerializedName("LastPrimaryModeIsAuto")
    @Expose
    public Boolean lastPrimaryModeIsAuto;
    @SerializedName("BoostEndTime")
    @Expose
    public String boostEndTime;
    @SerializedName("FrostProtectionTemperature")
    @Expose
    public Integer frostProtectionTemperature;
    @SerializedName("ErrorCode")
    @Expose
    public Integer errorCode;
    @SerializedName("ThermostatName")
    @Expose
    public String thermostatName;
    @SerializedName("OpenWindow")
    @Expose
    public Boolean openWindow;
    @SerializedName("AdaptiveMode")
    @Expose
    public Boolean adaptiveMode;
    @SerializedName("DaylightSaving")
    @Expose
    public Boolean daylightSaving;
    @SerializedName("SensorAppl")
    @Expose
    public Integer sensorAppl;
    @SerializedName("MinSetpoint")
    @Expose
    public Integer minSetpoint;
    @SerializedName("MaxSetpoint")
    @Expose
    public Integer maxSetpoint;
    @SerializedName("TimeZone")
    @Expose
    public Integer timeZone;
    @SerializedName("DaylightSavingActive")
    @Expose
    public Boolean daylightSavingActive;
    @SerializedName("FloorType")
    @Expose
    public Integer floorType;

}
