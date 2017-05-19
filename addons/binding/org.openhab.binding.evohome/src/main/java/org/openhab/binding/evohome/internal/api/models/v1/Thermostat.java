package org.openhab.binding.evohome.internal.api.models.v1;

import java.math.BigDecimal;

public class Thermostat {

    private String units;
    private BigDecimal indoorTemperature;
    private ChangeableValues changeableValues;

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "units[" + units + "] indoorTemperature[" + indoorTemperature + "] changeableValues[" + changeableValues
                + "]";
    }

    public String getUnits() {
        return units;
    }

    public BigDecimal getIndoorTemperature() {
        return indoorTemperature;
    }

    public ChangeableValues getChangeableValues() {
        return changeableValues;
    }
}
