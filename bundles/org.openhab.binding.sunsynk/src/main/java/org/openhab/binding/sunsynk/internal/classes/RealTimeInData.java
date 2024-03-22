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
 * The {@link Settings} is the internal class for Inverter real time information
 * from the SunSynk Account.
 * 
 * 
 * @author Lee Charlton - Initial contribution
 */

// {'code': 0, 'msg': 'Success', 'data': {'pac': 0, 'grid_tip_power': '--', 'pvIV': [...], 'mpptIV': [...], 'etoday':
// 0.0, 'etotal': 0.0}, 'success': True}//
// pVIV: [{'id': None, 'pvNo': 1, 'vpv': '1.9', 'ipv': '0.0', 'ppv': '0.0', 'todayPv': '0.0', 'sn': '2211229948',
// 'time': '2024-01-03 11:55:26'}, {'id': None, 'pvNo': 2, 'vpv': '2.1', 'ipv': '0.0', 'ppv': '0.0', 'todayPv': '0.0',
// 'sn': '2211229948', 'time': '2024-01-03 11:55:26'}]
// mpptIV: []

public class RealTimeInData {

    private int code;
    private String msg;
    private Data data;
    private double solar_power;

    class Data {

        private int pac;
        private String grid_tip_power;
        private double etoday;
        private double etotal;
        private List<PVIV> pvIV;
        private List<MPPTIV> mpptIV;
    }

    private class PVIV {
        private String id;
        private int pvNo;
        private double vpv;
        private double ipv;
        private double ppv; // sum for all power
        private double todayPv;
        private String sn;
        private String time;
    }

    class MPPTIV { // Empty; no solar panels
    }

    public double getetoday() {
        return this.data.etoday;
    }

    public double getetotal() {
        return this.data.etotal;
    }

    public void sumPVIV() {
        double solar_power = 0.0;
        for (PVIV x : this.data.pvIV) {
            this.solar_power = solar_power + x.ppv;
        }
    }

    public double getPVIV() {
        return this.solar_power;
    }
}
