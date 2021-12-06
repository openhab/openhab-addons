package org.openhab.binding.boschspexor.internal.api.model;

import org.apache.commons.lang3.StringUtils;

public class SensorValue<T> {
    private T value;
    private String unit;
    private T minValue;
    private T maxValue;
    private String timestamp;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasMinValue() {
        return minValue != null;
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public boolean hasMaxValue() {
        return maxValue != null;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }

    public boolean hasTimestamp() {
        return !StringUtils.isEmpty(timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
