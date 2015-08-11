package org.openhab.binding.honeywellwifithermostat.internal.data;

public enum HoneywellThermostatFanMode {
    AUTO(0),
    ON(1);

    private final int val;

    private HoneywellThermostatFanMode(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
