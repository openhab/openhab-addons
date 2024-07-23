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
package org.openhab.binding.solax.internal.local.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.local.EvChargerData;

/**
 * The {@link TestEVChargerParser} Simple test that tests for proper parsing against a real data from the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestEVChargerParser {

    private static final String RAW_DATA = """
            {
                "SN":"SQBLABLA",
                "ver":"3.004.11",
                "type":1,
                "Data":[
                    2,2,23914,23991,23895,1517,1513,1519,3654,3657,
                    3656,10968,44,0,346,0,65434,35463,65459,65508,
                    65513,27,402,0,43,0,2,15,0,0,
                    0,0,0,5004,5000,4996,10518,1547,6150,4,
                    0,0,0,0,0,0,0,0,1,100,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,
                    1717,0,3114,1547,6150,0,1,1,1,0,
                    0,121,584,266,0,50,0,0,1,1,0],
                "Information":[11.000,1,"CXXXXXXXXXX",1,1.13,1.01,0.00,0.00,0.00,1],
                "OCPPServer":"",
                "OCPPChargerId":""
            }
            """;

    @Test
    public void testParser() {
        LocalConnectRawDataBean bean = LocalConnectRawDataBean.fromJson(RAW_DATA);
        assertNotNull(bean);

        EvChargerData data = new EvChargerData(bean);
        assertEquals("SQBLABLA", data.getWifiSerial()); // 0
        assertEquals("3.004.11", data.getWifiVersion()); // 1

        assertEquals(239.14, data.getVoltagePhase1()); // 2
        assertEquals(239.91, data.getVoltagePhase2()); // 3
        assertEquals(238.95, data.getVoltagePhase3()); // 4

        assertEquals(15.17, data.getCurrentPhase1()); // 5
        assertEquals(15.13, data.getCurrentPhase2()); // 6
        assertEquals(15.19, data.getCurrentPhase3()); // 7

        assertEquals(3654, data.getOutputPowerPhase1()); // 8
        assertEquals(3657, data.getOutputPowerPhase2()); // 9
        assertEquals(3656, data.getOutputPowerPhase3()); // 10

        assertEquals(10968, data.getTotalChargePower()); // 11

        assertEquals(4.4, data.getEqSingle()); // 12
        assertEquals(34.6, data.getEqTotal()); // 14 and 15

        assertEquals(-1.02, data.getExternalCurrentPhase1()); // 16
        assertEquals(-300.73, data.getExternalCurrentPhase2()); // 17
        assertEquals(-0.77, data.getExternalCurrentPhase3()); // 18

        assertEquals(-28, data.getExternalPowerPhase1()); // 19
        assertEquals(-23, data.getExternalPowerPhase2()); // 20
        assertEquals(27, data.getExternalPowerPhase3()); // 21
        assertEquals(402, data.getExternalTotalPower()); // 22

        assertEquals(0, data.getPlugTemperature()); // 23
        assertEquals(43, data.getInternalTemperature()); // 24

        assertEquals(2, data.getCPState()); // 26

        assertEquals(1717, data.getChargingDuration()); // 80 and 81

        assertEquals(0, data.getOccpOfflineMode()); // 85

        assertEquals(1, data.getTypePower()); // 87
        assertEquals(1, data.getTypePhase()); // 88
        assertEquals(0, data.getTypeCharger()); // 89
    }
}
