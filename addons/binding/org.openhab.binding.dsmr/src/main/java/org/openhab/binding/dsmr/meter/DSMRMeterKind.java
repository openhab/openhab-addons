package org.openhab.binding.dsmr.meter;

/**
 * This class describes the kind of meters the binding supports
 *
 * @author Marcel Volaart
 * @since 2.0.0
 */
public enum DSMRMeterKind {
    INVALID("Invalid meter"),
    DEVICE("Generic DSMR device"),
    MAIN_ELECTRICITY("Main electricity meter"),
    GAS("Gas meter"),
    HEATING("Heating meter"),
    COOLING("Cooling meter"),
    WATER("Water meter"),
    GENERIC("Generic meter"),
    GJ("GJ meter"),
    M3("M3 meter"),
    SLAVE_ELECTRICITY1("Slave electricity meter"),
    SLAVE_ELECTRICITY2("Slave electricity meter 2");

    private String name;

    private DSMRMeterKind(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
