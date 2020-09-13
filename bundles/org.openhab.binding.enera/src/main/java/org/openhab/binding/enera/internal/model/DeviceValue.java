package org.openhab.binding.enera.internal.model;

public class DeviceValue {
    private String Obis;
    private float Value;

    public String getObis() {
        return Obis;
    }

    public float getValue() {
        return Value;
    }

    public void setValue(float value) {
        this.Value = value;
    }

    public void setObis(String obis) {
        this.Obis = obis;
    }
}
