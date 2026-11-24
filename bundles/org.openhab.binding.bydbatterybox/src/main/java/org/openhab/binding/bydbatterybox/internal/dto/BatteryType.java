/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bydbatterybox.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Battery module types as reported in the identity block. Only types with a known cell layout support cell details.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public enum BatteryType {
    /** 16 cells of 3.2 V and 54 Ah per module. */
    HVM(1, 16, 8, 2760),
    /** 32 cells of 3.2 V and 25 Ah per module. */
    HVS(2, 32, 12, 2560),
    /** HVL (code 0), LVS and any unknown code: no known cell layout. */
    UNSUPPORTED(-1, 0, 0, 0);

    private final int code;
    private final int cellsPerModule;
    private final int temperatureSensorsPerModule;
    private final int moduleCapacityWh;

    BatteryType(int code, int cellsPerModule, int temperatureSensorsPerModule, int moduleCapacityWh) {
        this.code = code;
        this.cellsPerModule = cellsPerModule;
        this.temperatureSensorsPerModule = temperatureSensorsPerModule;
        this.moduleCapacityWh = moduleCapacityWh;
    }

    public static BatteryType fromCode(int code) {
        for (BatteryType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNSUPPORTED;
    }

    public int getCellsPerModule() {
        return cellsPerModule;
    }

    public int getTemperatureSensorsPerModule() {
        return temperatureSensorsPerModule;
    }

    /**
     * @return the nominal capacity of one module in Wh, or 0 if it is not known
     */
    public int getModuleCapacityWh() {
        return moduleCapacityWh;
    }

    public boolean hasCellDetails() {
        return cellsPerModule > 0;
    }
}
