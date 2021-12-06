package org.openhab.binding.boschspexor.internal.api.model;

public class Energy {
    public enum EnergyMode {
        EnergySavingOff,
        EnergySavingAllwaysOn,
        EnergySavingOnBattery
    }

    private SensorValue<Integer> stateOfCharge;
    private EnergyMode energyMode;
    private boolean isPowered;

    public SensorValue<Integer> getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(SensorValue<Integer> stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    public EnergyMode getEnergyMode() {
        return energyMode;
    }

    public void setEnergyMode(EnergyMode energyMode) {
        this.energyMode = energyMode;
    }

    public boolean isPowered() {
        return isPowered;
    }

    public void setPowered(boolean isPowered) {
        this.isPowered = isPowered;
    }
}
