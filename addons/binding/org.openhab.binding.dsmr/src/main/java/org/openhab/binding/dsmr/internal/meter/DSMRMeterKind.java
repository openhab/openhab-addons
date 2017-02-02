/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

/**
 * This class describes the kind of meters the binding supports
 *
 * @author M. Volaart
 * @since 2.1.0
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

    // name of the meterkind
    private String name;

    /**
     * Constructs the meter kind
     * 
     * @param name readable name for this DSMR Meter Kind
     */
    private DSMRMeterKind(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
