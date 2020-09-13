package org.openhab.binding.enera.internal.model;

public class DeviceValue {
    private String obis;
    private float value;

    /**
     * @return the obis
     */
    public String getObis() {
        return obis;
    }

    /**
     * @param obis the obis to set
     */
    public void setObis(String obis) {
        this.obis = obis;
    }

    /**
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }

}
