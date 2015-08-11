package org.openhab.binding.honeywellwifithermostat.internal.data;

public class HoneywellThermostatData {
    private int CurrentTemperature = 70;
    private HoneywellThermostatSystemMode CurrentSystemMode = HoneywellThermostatSystemMode.OFF;
    private int HeatSetPoint = 70;
    private int CoolSetPoint = 70;
    private HoneywellThermostatFanMode CurrentFanMode = HoneywellThermostatFanMode.AUTO;

    public int getCurrentTemperature() {
        return CurrentTemperature;
    }

    public void setCurrentTemperature(int currentTemperature) {
        this.CurrentTemperature = currentTemperature;
    }

    public HoneywellThermostatSystemMode getCurrentSystemMode() {
        return CurrentSystemMode;
    }

    public void setCurrentSystemMode(HoneywellThermostatSystemMode currentSystemMode) {
        this.CurrentSystemMode = currentSystemMode;
    }

    public int getHeatSetPoint() {
        return HeatSetPoint;
    }

    public void setHeatSetPoint(int heatSetPoint) {
        this.HeatSetPoint = heatSetPoint;
    }

    public int getCoolSetPoint() {
        return CoolSetPoint;
    }

    public void setCoolSetPoint(int coolSetPoint) {
        this.CoolSetPoint = coolSetPoint;
    }

    public HoneywellThermostatFanMode getCurrentFanMode() {
        return CurrentFanMode;
    }

    public void setCurrentFanMode(HoneywellThermostatFanMode currentFanMode) {
        this.CurrentFanMode = currentFanMode;
    }

}
