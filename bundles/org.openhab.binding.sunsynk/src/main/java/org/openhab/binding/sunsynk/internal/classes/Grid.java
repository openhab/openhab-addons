/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

package org.openhab.binding.sunsynk.internal.classes;

import java.util.List;

/**
 * The {@link Grid} is the internal class for Inverter real time grid information
 * from the SunSynk Account.
 * 
 * 
 * @author Lee Charlton - Initial contribution
 */

// {'vip': [{...}], 'pac': 16, 'qac': 0, 'fac': 49.96, 'pf': 1.0, 'status': 1, 'etodayFrom': '0.5',
// 'etodayTo': '0.0', 'etotalFrom': '1757.6', 'etotalTo': '253.7', 'limiterPowerArr': [16, 0], 'limiterTotalPower': 16}
//
// vip: 0: [{'volt': '242.5', 'current': '2.0', 'power': 16}] ...
//
public class Grid {

    private List<VIP> vip;

    class VIP { // needs checking
        private double volt;
        private double current;
        private double power;
    }

    private int pac;
    private int qac;
    private double fac;
    // private String fac;
    private double pf;
    private String status;
    private double etodayFrom;
    private double etodayTo;
    private double etotalFrom;
    private double etotalTo;
    private String limiterPowerArr;
    private List<Integer> LimiterPowerArr;
    private double power;
    private double voltage;
    private double current;

    private void sumVIP() {
        double l_power = 0.0;
        double l_voltage = 0.0;
        double l_current = 0.0;
        for (VIP x : this.vip) {
            l_power = l_power + x.power;
            l_voltage = l_voltage + x.volt;
            l_current = l_current + x.current;
        }
        this.power = l_power;
        this.voltage = l_voltage;
        this.current = l_current;
    }

    public double getGridPower() {
        sumVIP();
        return this.power;
    }

    public double getGridVoltage() {
        sumVIP();
        return this.voltage;
    }

    public double getGridCurrent() {
        sumVIP();
        return this.current;
    }
}
