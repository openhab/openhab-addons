/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.iammeter.internal;

import javax.measure.Unit;

import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * The {@link IammeterWEM3080TChannel} Enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Yang Bo - Initial contribution
 */
public enum IammeterWEM3080TChannel {

    CHANNEL_VOLTAGE_A("voltage_a", 0, 0, SmartHomeUnits.VOLT),
    CHANNEL_CURRENT_A("current_a", 0, 1, SmartHomeUnits.AMPERE),
    CHANNEL_POWER_A("power_a", 0, 2, SmartHomeUnits.WATT),
    CHANNEL_IMPORTENERGY_A("importenergy_a", 0, 3, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_EXPORTGRID_A("exportgrid_a", 0, 4, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_FREQUENCY_A("frequency_a", 0, 5, SmartHomeUnits.HERTZ),
    CHANNEL_PF_A("pf_a", 0, 6, SmartHomeUnits.HERTZ),
    CHANNEL_VOLTAGE_B("voltage_b", 1, 0, SmartHomeUnits.VOLT),
    CHANNEL_CURRENT_B("current_b", 1, 1, SmartHomeUnits.AMPERE),
    CHANNEL_POWER_B("power_b", 1, 2, SmartHomeUnits.WATT),
    CHANNEL_IMPORTENERGY_B("importenergy_b", 1, 3, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_EXPORTGRID_B("exportgrid_b", 1, 4, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_FREQUENCY_B("frequency_b", 1, 5, SmartHomeUnits.HERTZ),
    CHANNEL_PF_B("pf_b", 1, 6, SmartHomeUnits.HERTZ),
    CHANNEL_VOLTAGE_C("voltage_c", 2, 0, SmartHomeUnits.VOLT),
    CHANNEL_CURRENT_C("current_c", 2, 1, SmartHomeUnits.AMPERE),
    CHANNEL_POWER_C("power_c", 2, 2, SmartHomeUnits.WATT),
    CHANNEL_IMPORTENERGY_C("importenergy_c", 2, 3, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_EXPORTGRID_C("exportgrid_c", 2, 4, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_FREQUENCY_C("frequency_c", 2, 5, SmartHomeUnits.HERTZ),
    CHANNEL_PF_C("pf_c", 2, 6, SmartHomeUnits.HERTZ);

    private final String id;
    private final int row;
    private final int col;
    private final Unit<?> unit;

    IammeterWEM3080TChannel(String id, int r, int c, Unit<?> unit) {
        this.id = id;
        this.row = r;
        this.col = c;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Unit<?> getUnit() {
        return unit;
    }
}
