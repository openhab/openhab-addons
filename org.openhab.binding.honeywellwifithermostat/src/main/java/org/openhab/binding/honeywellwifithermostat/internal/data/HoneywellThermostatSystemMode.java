package org.openhab.binding.honeywellwifithermostat.internal.data;

public enum HoneywellThermostatSystemMode {
    HEAT(1),
    OFF(2),
    COOL(3);

    private final int val;

    private HoneywellThermostatSystemMode(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
