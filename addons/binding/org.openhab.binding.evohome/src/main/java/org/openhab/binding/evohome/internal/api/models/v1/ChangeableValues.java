package org.openhab.binding.evohome.internal.api.models.v1;

public class ChangeableValues {

    private String mode;
    private HeatSetPoint heatSetpoint;

    @Override
    public String toString() {
        return "mode[" + mode + "] heatSetPoint[" + heatSetpoint + "]";
    }

    public String getMode() {
        return mode;
    }

    public HeatSetPoint getHeatSetpoint() {
        return heatSetpoint;
    }
}
