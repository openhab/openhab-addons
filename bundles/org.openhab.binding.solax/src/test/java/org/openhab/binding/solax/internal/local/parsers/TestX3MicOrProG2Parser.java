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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;

/**
 * The {@link TestX3HybridG4Parser} simple test that tests for proper parsing against a real data from the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestX3MicOrProG2Parser {

    String rawData = """
            {
                sn:XYZ,
                ver:3.003.02,
                type:16,Data:[
                    2515,2449,2484,5,5,9,54,44,20,4080,4340,
                    0,1,2,0,67,102,0,4999,4999,4999,2,19035,
                    0,50,8000,5,9,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,
                    0,0,0,0,0,0,0,0,120,40,1,6,5,0,6772,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0
                    ],
                Information:[8.000,16,XY,8,1.15,0.00,1.11,1.01,0.00,1]
             }
            """;

    @Test
    public void testParser() {
        LocalConnectRawDataBean bean = LocalConnectRawDataBean.fromJson(rawData);
        int type = bean.getType();
        InverterType inverterType = InverterType.fromIndex(type);
        assertEquals(InverterType.X3_MIC_OR_PRO_G2, inverterType, "Inverter type not recognized properly");

        RawDataParser parser = inverterType.getParser();
        assertNotNull(parser);

        LocalData data = parser.getData(bean);
        assertEquals("XYZ", data.getWifiSerial());
        assertEquals("3.003.02", data.getWifiVersion());

        assertEquals(251.5, data.getVoltagePhase1()); // [0]
        assertEquals(244.9, data.getVoltagePhase2()); // [1]
        assertEquals(248.4, data.getVoltagePhase3()); // [2]

        assertEquals(0.5, data.getCurrentPhase1()); // [3]
        assertEquals(0.5, data.getCurrentPhase2()); // [4]
        assertEquals(0.9, data.getCurrentPhase3()); // [5]

        assertEquals(54, data.getOutputPowerPhase1()); // [6]
        assertEquals(44, data.getOutputPowerPhase2()); // [7]
        assertEquals(20, data.getOutputPowerPhase3()); // [8]

        assertEquals(408, data.getPV1Voltage()); // [9]
        assertEquals(434, data.getPV2Voltage()); // [10]
        assertEquals(0.1, data.getPV1Current()); // [12]
        assertEquals(0.2, data.getPV2Current()); // [13]
        assertEquals(67, data.getPV1Power()); // [15]
        assertEquals(102, data.getPV2Power()); // [16]

        assertEquals(49.99, data.getFrequencyPhase1()); // [18]
        assertEquals(49.99, data.getFrequencyPhase2()); // [19]
        assertEquals(49.99, data.getFrequencyPhase3()); // [20]

        assertEquals(2, data.getInverterWorkModeCode()); // [21]
        assertEquals("2", data.getInverterWorkMode()); // [21]

        assertEquals(5, data.getInverterTemperature1()); // [26]
        assertEquals(9, data.getInverterTemperature2()); // [27]

        assertEquals(120, data.getTotalOutputPower()); // [78]

        assertEquals(1903.5, data.getTotalEnergy()); // [22]
        assertEquals(5.0, data.getTodayEnergy()); // [24]
    }
}
