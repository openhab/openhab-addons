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
package org.openhab.binding.solaredge.internal.model;

import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;

/**
 * common interface for all data response classes
 *
 * @author Alexander Friese - initial contribution
 */
public interface DataResponse {
    static final String UNIT_WH = "Wh";
    static final String UNIT_KWH = "kWh";
    static final String UNIT_MWH = "MWh";
    static final String UNIT_W = "W";
    static final String UNIT_KW = "kW";
    static final String UNIT_MW = "MW";

    Map<Channel, State> getValues();

    /**
     * determines the unit, also handles wrong spelling of kWh (which is spelled with capital K by API)
     *
     * @param unit
     * @return
     */
    default Unit<Energy> determineEnergyUnit(String unit) {
        if (unit != null) {
            if (unit.equals(UNIT_WH)) {
                return SmartHomeUnits.WATT_HOUR;
            } else if (unit.toLowerCase().equals(UNIT_KWH.toLowerCase())) {
                return MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR);
            } else if (unit.equals(UNIT_MWH)) {
                return MetricPrefix.MEGA(SmartHomeUnits.WATT_HOUR);
            }
        }
        return null;
    }

    /**
     * determines the unit, also handles wrong spelling of kW (which is spelled with capital K by API)
     *
     * @param unit
     * @return
     */
    default Unit<Power> determinePowerUnit(String unit) {
        if (unit != null) {
            if (unit.equals(UNIT_W)) {
                return SmartHomeUnits.WATT;
            } else if (unit.toLowerCase().equals(UNIT_KW.toLowerCase())) {
                return MetricPrefix.KILO(SmartHomeUnits.WATT);
            } else if (unit.equals(UNIT_MW)) {
                return MetricPrefix.MEGA(SmartHomeUnits.WATT);
            }
        }
        return null;
    }
}
