
package org.openhab.binding.ojelectronics.internal.models.groups;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupContent {

    @SerializedName("Action")
    @Expose
    public Integer action;
    @SerializedName("GroupId")
    @Expose
    public Integer groupId;
    @SerializedName("GroupName")
    @Expose
    public String groupName;
    @SerializedName("Thermostats")
    @Expose
    public List<Thermostat> thermostats = new ArrayList<Thermostat>();
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

}
