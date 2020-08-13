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
 * The {@link IammeterWEM3080Channel} Enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Yang Bo - Initial contribution
 */
public enum IammeterWEM3080Channel {

    CHANNEL_VOLTAGE("voltage_a", 0, SmartHomeUnits.VOLT),
    CHANNEL_CURRENT("current_a", 1, SmartHomeUnits.AMPERE),
    CHANNEL_POWER("power_a", 2, SmartHomeUnits.WATT),
    CHANNEL_IMPORTENERGY("importenergy_a", 3, SmartHomeUnits.KILOWATT_HOUR),
    CHANNEL_EXPORTGRID("exportgrid_a", 4, SmartHomeUnits.KILOWATT_HOUR);

    private final String id;
    private final int index;
    private final Unit<?> unit;

    IammeterWEM3080Channel(String id, int index, Unit<?> unit) {
        this.id = id;
        this.index = index;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public Unit<?> getUnit() {
        return unit;
    }
}
