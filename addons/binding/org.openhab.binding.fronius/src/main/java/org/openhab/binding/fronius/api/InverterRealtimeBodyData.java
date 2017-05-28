package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class InverterRealtimeBodyData {

    @SerializedName("DAY_ENERGY")
    private ValueUnit dayEnergy;
    @SerializedName("PAC")
    private ValueUnit pac;
    @SerializedName("TOTAL_ENERGY")
    private ValueUnit totalEnergy;
    @SerializedName("YEAR_ENERGY")
    private ValueUnit yearEnergy;
    @SerializedName("DeviceStatus")
    private DeviceStatus deviceStatus;

    public ValueUnit getDayEnergy() {
        return dayEnergy;
    }

    public void setDayEnergy(ValueUnit dayEnergy) {
        this.dayEnergy = dayEnergy;
    }

    public ValueUnit getPac() {
        return pac;
    }

    public void setPac(ValueUnit pac) {
        this.pac = pac;
    }

    public ValueUnit getTotalEnergy() {
        return totalEnergy;
    }

    public void setTotalEnergy(ValueUnit totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    public ValueUnit getYearEnergy() {
        return yearEnergy;
    }

    public void setYearEnergy(ValueUnit yearEnergy) {
        this.yearEnergy = yearEnergy;
    }

    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

}
